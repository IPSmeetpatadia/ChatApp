package com.ipsmeet.chatapp.activities

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.ipsmeet.chatapp.R
import com.ipsmeet.chatapp.databinding.ActivityViewProfileBinding
import com.ipsmeet.chatapp.dataclasses.UserDataClass
import java.io.File

class ViewProfileActivity : AppCompatActivity() {

    lateinit var binding: ActivityViewProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar!!.hide()

        val personID = intent.getStringExtra("userID")   // ID of other person
        val personToken = intent.getStringExtra("token").toString()   // token of other person

        binding.viewProfileBack.setOnClickListener {
            updateUI()
        }

        FirebaseDatabase.getInstance().getReference("Users/$personID")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val data = snapshot.getValue(UserDataClass::class.java)
                        data!!.key = snapshot.key.toString()

                        binding.viewProfileName.text = data.userName
                        binding.viewProfileNumber.text = data.phoneNumber
                        binding.viewProfileBlockName.text = data.userName

                         if (data.about.isNotEmpty()) {
                             binding.viewProfileAbout.text = data.about
                         }
                         else {
                            binding.viewProfileAbout.text = getString(R.string.default_about)
                         }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("Failed to load user details", error.message)
                }
            })

        val localFile = File.createTempFile("tempfile", "jpeg")
        FirebaseStorage.getInstance().getReference("Images/*$personID").getFile(localFile)
            .addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                Glide.with(applicationContext).load(bitmap).into(binding.viewProfileImg)
            }
            .addOnFailureListener {
                Log.d("Fail to load user profiles", it.message.toString())
            }

        binding.viewProfileOpenChat.setOnClickListener {
            startActivity(
                Intent(this@ViewProfileActivity, ChatActivity::class.java)
                    .putExtra("userID", personID)
                    .putExtra("token", personToken)
            )
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        updateUI()
    }

    private fun updateUI() {
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)   // all of the other activities on top of it will be closed
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)    // activity will become the start of a new task on this history stack
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)  // activity becomes the new root of an otherwise empty task, and any old activities are finished
        )
        finish()
    }
}