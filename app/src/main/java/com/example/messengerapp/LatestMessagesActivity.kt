package com.example.messengerapp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth


class LatestMessagesActivity : AppCompatActivity (){

    override fun onCreate(savedInstantState: Bundle?){
        super.onCreate(savedInstantState);
        setContentView(R.layout.activity_lastest_messages)
        verifyLogin()

    }

    private fun verifyLogin(){

        val uid = FirebaseAuth.getInstance().uid
        // Meaning user is not logged in
        if(uid == null){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
       when(item?.itemId){
           R.id.menu_new_message -> {
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