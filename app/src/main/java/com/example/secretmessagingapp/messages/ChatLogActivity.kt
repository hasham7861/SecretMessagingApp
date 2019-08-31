package com.example.secretmessagingapp.messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.secretmessagingapp.R
import com.example.secretmessagingapp.models.ChatMessage
import com.example.secretmessagingapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*




class ChatLogActivity : AppCompatActivity() {

    companion object{
        val TAG = "CHATLOG"
    }
    val adapter = GroupAdapter<ViewHolder>()
    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        // Connecting the recycler view layout to pre-made adapater class
        recyclerview_chat_log.adapter = adapter

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)


        supportActionBar?.title = toUser?.username

        listenForMessages()
        button_chat_log.setOnClickListener {
            Log.d(TAG,"Attempt to send message")
            performSendMessage()

        }


    }

    private fun listenForMessages(){


        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        // Reference to messages log in database
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
//              Get the text message from new entry that gets added to database
                val chatMessage = p0.getValue(ChatMessage::class.java)
                Log.d(TAG,chatMessage?.text)
                if(chatMessage != null){

                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
                        val currentUser = LatestMessagesActivity.currentUser ?: return
                        adapter.add(ChatFromItem(chatMessage.text,currentUser))
                    }

                    else{
                        adapter.add(ChatToItem(chatMessage.text,toUser!!))
                    }

                    // Scroll the to end of the recycler view after adding messages
                    recyclerview_chat_log.scrollToPosition(adapter.itemCount-1)

                }

            }
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }



            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })
    }

    private fun performSendMessage(){
        // send message to database

        val text = edittext_chat_log.text.toString()
        val fromId = FirebaseAuth.getInstance().uid

        val toId = toUser?.uid

        if(fromId == null) return


        // Adding reference of your current message from you to receiver
        val reference = FirebaseDatabase
            .getInstance()
            .getReference("/user-messages/$fromId/$toId").push()
        // Adding reference of your message on the other end, so from receiver perspective to sender
        val toReference = FirebaseDatabase
            .getInstance()
            .getReference("/user-messages/$toId/$fromId").push()

        val chatMessage = ChatMessage(reference.key!!,text,fromId!!,toId!!,System.currentTimeMillis()/1000)
        reference.setValue(chatMessage).addOnSuccessListener {
            Log.d(TAG, "Saved cour chat message: ${reference.key}")

            // Clear text after sending message
            edittext_chat_log.text.clear()
            // Scroll the the bottom of the messages after sending message
            recyclerview_chat_log.scrollToPosition(adapter.itemCount-1)

        }

        toReference.setValue(chatMessage)

        // Created an entry into latest messages for keeping track of recent messages sent
        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        // keeping track of messages on the other side for receiver
        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)
    }



}



class ChatFromItem(val text: String, val user: User): Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textview_from_row.text = text
        val uri = user.profileImageUrl
        Picasso.get().load(uri).into(viewHolder.itemView.imageView_from_row)
    }

}

class ChatToItem(val text: String, val user: User): Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textview_to_row.text=text
        // Using the image caching library named Picasso to populate text into each chat row
        val uri = user.profileImageUrl
        Picasso.get().load(uri).into(viewHolder.itemView.imageView_to_row)
    }

}