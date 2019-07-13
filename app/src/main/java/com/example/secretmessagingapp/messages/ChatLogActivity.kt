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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        // Connecting the recycler view layout to pre-made adapater class
        recyclerview_chat_log.adapter = adapter

        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        supportActionBar?.title = user.username

//        setupDummyData()
        listenForMessages()
        button_chat_log.setOnClickListener {
            Log.d(TAG,"Attempt to send message")
            performSendMessage()


        }


    }

    private fun listenForMessages(){
        // Reference to messages log in database
        val ref = FirebaseDatabase.getInstance().getReference("/messages")

        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
//              Get the text message from new entry that gets added to database
                val chatMessage = p0.getValue(ChatMessage::class.java)
                Log.d(TAG,chatMessage?.text)
                if(chatMessage != null){

                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid)
                        adapter.add(ChatFromItem(chatMessage.text))
                    else
                        adapter.add(ChatToItem(chatMessage.text))

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
        val user =  intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user.uid

        if(fromId == null) return

        val reference = FirebaseDatabase
                        .getInstance()
                        .getReference("/messages").push()

        val chatMessage = ChatMessage(reference.key!!,text,fromId!!,toId,System.currentTimeMillis()/1000)
        reference.setValue(chatMessage).addOnSuccessListener {
            Log.d(TAG, "Saved cour chat message: ${reference.key}")
            edittext_chat_log.setText("")

        }
    }

    private fun setupDummyData(){
        val adapter = GroupAdapter<ViewHolder>()
        adapter.add(ChatFromItem("This is from message"))
        adapter.add(ChatToItem("This your message"))
        adapter.add(ChatFromItem("This is from message"))
        adapter.add(ChatToItem("This your message"))
        adapter.add(ChatFromItem("This is from message"))
        adapter.add(ChatToItem("This your message"))


        recyclerview_chat_log.adapter = adapter
    }

}



class ChatFromItem(val text: String): Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textview_from_row.text = text
    }

}

class ChatToItem(val text: String): Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textview_to_row.text=text
    }

}