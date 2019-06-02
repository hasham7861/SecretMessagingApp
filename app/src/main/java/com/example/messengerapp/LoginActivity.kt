package com.example.messengerapp
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login);

        login_button.setOnClickListener {
            val email = email_edittext_login.text.toString()
            val password = password_edittext_login.text.toString()

            Log.d("LoginActivity","Email: $email")
            Log.d("LoginActivity","Password: $password")
        }


        go_to_register_textview.setOnClickListener {

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

        }



    }
}