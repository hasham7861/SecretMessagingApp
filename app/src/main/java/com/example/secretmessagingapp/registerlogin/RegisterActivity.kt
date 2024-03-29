package com.example.secretmessagingapp.registerlogin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.secretmessagingapp.messages.LatestMessagesActivity
import com.example.secretmessagingapp.R
import com.example.secretmessagingapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*


class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        register_button_register.setOnClickListener {
            performRegister()
        }

        already_have_account_text_view.setOnClickListener {
            Log.d("RegisterActivity", "Try to show login activity")

            finish()
        }

        selectphoto_button_register.setOnClickListener{
            Log.d("RegisterActivity", "Try to show photo selector")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type="image/*"
            startActivityForResult(intent,0)
        }
    }

    var selectedPhotoUri : Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d("RegisterActivity", "Photo was selected")

            selectedPhotoUri = data.data;
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
            selectphoto_imageview_register.setImageBitmap(bitmap)

            selectphoto_button_register.alpha = 0f
        }
        else{
            Log.d("RegisterActivity", "Photo not selected")
        }


    }

    private fun performRegister(){

        val username = username_edittext_register.text.toString()
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()
        val image = selectphoto_imageview_register.drawable;


        if(username.isEmpty()){
            Toast.makeText(this,"Please enter username", Toast.LENGTH_SHORT).show()
            return
        }

        if(email.isEmpty()){
            Toast.makeText(this,"Please enter email ", Toast.LENGTH_SHORT).show()
            return
        }

        if(password.isEmpty()){
            Toast.makeText(this,"Please enter a valid password", Toast.LENGTH_SHORT).show()
            return
        }

        if(image == null){
            Toast.makeText(this,"Please select an image", Toast.LENGTH_SHORT).show()
            return
        }

        // Create new user at Firebase
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                // Open up Messages
                val intent = Intent(this, LatestMessagesActivity::class.java)
                // clears all the old activities from the context
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)


                // else if successful
                Log.d("Main","Successfully created user with uid: ${it.result?.user?.uid}")


            }
            .addOnFailureListener{
                Log.d("Main","Failed to create user: ${it.message}")
                Toast.makeText(this,"Email / Password is incorrect ", Toast.LENGTH_SHORT).show()
            }

        uploadImageToFirebaseStorage()

    }

    private fun uploadImageToFirebaseStorage(){

        if(selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()

        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")


        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("RegisterActivityImage","Uploaded Image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {

                    Log.d("RegisterActivity","File Location: $it")
                    saveUserToFirebase(it.toString())
                }
            }
            .addOnFailureListener{

            }
    }

    private fun saveUserToFirebase(profileImageUrl: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(
            uid,
            username_edittext_register.text.toString(),
            profileImageUrl
        )
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Finally we saved the user to Firebase Database")
            }
            .addOnFailureListener{
                Log.d("RegisterActivity", "Failed to save register info to database")
            }
    }
}
