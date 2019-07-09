package com.example.secretmessagingapp.messages

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.example.secretmessagingapp.R
import com.example.secretmessagingapp.models.User

import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*



class NewMessageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)
        supportActionBar?.title = "Select User"

        fetchUsers()
    }

    private fun fetchUsers(){
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        // Listen to updates on database and only then update the add user screen
        ref.addListenerForSingleValueEvent(object: ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {

                val adapter = GroupAdapter<ViewHolder>()
                p0.children.forEach{
                    Log.d("NewMessage",it.toString())
                    val user = it.getValue(User::class.java)
                    if(user != null)
                        adapter.add(UserItem(user))
                }
                
                
                adapter.setOnItemClickListener { _, view ->
                    val intent = Intent (view.context,ChatLogActivity::class.java)
                    startActivity(intent)
                    // Save resources by closing down previous activity
                    finish()
                }
                
                
                
                recyclerview_newmessage.adapter = adapter
            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

}

class UserItem(val user: User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        // bind each list object inside the recycler view
        viewHolder.itemView.username_textview_newmessage.text = user.username
        // Load images using image caching library
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.image_imageview_newmessage)
    }

    override fun getLayout() : Int{
        return R.layout.user_row_new_message
    }
}

