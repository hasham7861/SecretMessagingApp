package com.example.messengerapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        register_button_register.setOnClickListener {
            performRegister()
        }

        already_have_account_text_view.setOnClickListener {
            Log.d("MainActivity", "Try to show login activity")

            // Launch teh login activity somehow
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performRegister(){

        val username = username_edittext_register.text.toString()
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()

        Log.d("MainActivity", "Email is: $username")
        Log.d("MainActivity", "Email is: $email")
        Log.d("MainActivity", "Password: $password")

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this,"Please enter text in email with password", Toast.LENGTH_SHORT).show()
            return
        }
        // Create new user at Firebase
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                // else if successful
                Log.d("Main","Successfully created user with uid: ${it.result?.user?.uid}")
                Toast.makeText(this,"Successfully created account ", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener{
                Log.d("Main","Failed to create user: ${it.message}")
                Toast.makeText(this,"Email / Password is incorrect ", Toast.LENGTH_SHORT).show()
            }

    }
}
