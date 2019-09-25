package com.example.secretmessagingapp.messages

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.secretmessagingapp.R
import com.example.secretmessagingapp.jobscheduler.MyJobService
import com.example.secretmessagingapp.messages.NewMessageActivity.Companion.USER_KEY
import com.example.secretmessagingapp.models.ChatMessage
import com.example.secretmessagingapp.models.User
import com.example.secretmessagingapp.registerlogin.LoginActivity
import com.example.secretmessagingapp.views.LatestMessageRow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*


class LatestMessagesActivity : AppCompatActivity (){


    companion object{
        var currentUser: User? = null
        var TAG = "LATESTMESSAGES"
        var jobCreated = false
    }
    private var jobId = 0


    override fun onCreate(savedInstantState: Bundle?){
        super.onCreate(savedInstantState)
        setContentView(R.layout.activity_latest_messages)

        recyclerview_latest_messages.adapter = adapter

        // setting a click listener to each item in recycle view
        adapter.setOnItemClickListener {item, view->
            val intent = Intent(this,ChatLogActivity::class.java)
            // pass the current user to
            val row = item as LatestMessageRow
            intent.putExtra(USER_KEY,row.chatPartnerUser)
            startActivity(intent)
        }

        // perform the job scheduler here to delete all the messages only from db and logout
        if(!jobCreated){
            val serviceComponent = ComponentName(this, MyJobService::class.java)
            scheduleJob(serviceComponent)
            jobCreated = true
        }

        fetchCurrentUser()
        // load in messages once logged in
        if(verifyLogin())
            listenForLatestMessages()

    }


    private fun scheduleJob(serviceComponent: ComponentName){
        var info = JobInfo.Builder(jobId++,serviceComponent)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
            .setPersisted(true)
            .setPeriodic(24 * 60 * 60 * 1000)
            .build()
        (getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler).schedule(info)
        Log.d(TAG,"Scheduling Job")
    }

    private val latestMessagesMap = HashMap<String,ChatMessage>()

    // Refresh the whole recycler view and update it to recent messages
    private fun refreshRecyclerViewMessages(){
        adapter.clear()
        latestMessagesMap.values.forEach{
            adapter.add(LatestMessageRow(it))
        }
    }

    private fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid;
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")

        ref.addChildEventListener(object: ChildEventListener{

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()

            }
            // Update the changes once new message comes in
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onCancelled(p0: DatabaseError) {}

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

            override fun onChildRemoved(p0: DataSnapshot) {}

        })


    }

    private val adapter = GroupAdapter<ViewHolder>()

    private fun fetchCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                Log.d("LatestMessages", "Current user ${currentUser?.profileImageUrl}")
            }

            override fun onCancelled(p0: DatabaseError) {}

        })
    }

    private fun verifyLogin(): Boolean {

        val uid = FirebaseAuth.getInstance()?.uid
        // Meaning user is not logged in
        if(uid == null){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            Log.d("LatestMessagesActivity","Unable to login")
            return false
        }else{
            Log.d("LatestMessagesActivity","Log in success")
            return true
        }

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                Log.d("New Message", "Clicked")
                startActivity(intent)
            }

            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()

                val intent = Intent (this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}