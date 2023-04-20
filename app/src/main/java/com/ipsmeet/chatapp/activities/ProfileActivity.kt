package com.ipsmeet.chatapp.activities

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.ipsmeet.chatapp.databinding.ActivityProfileBinding
import com.ipsmeet.chatapp.dataclasses.SignInDataClass
import java.io.File

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    lateinit var userID: String

    lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userID = FirebaseAuth.getInstance().currentUser!!.uid

        binding.userBack.setOnClickListener {
            updateUI()
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("Users/$userID")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("snapshot", snapshot.key.toString())
                Log.d("userID", userID)

                val localFile = File.createTempFile("tempfile", "jpeg")
                FirebaseStorage.getInstance()
                    .getReference("Images/*${snapshot.key}").getFile(localFile)
                    .addOnSuccessListener {
                        val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                        Glide.with(this@ProfileActivity).load(bitmap).into(binding.userProfileImg)
                    }
                    .addOnFailureListener {
                        Log.d("Fail to load user profiles", it.message.toString())
                    }

                val userProfile = snapshot.getValue(SignInDataClass::class.java)
                binding.userTxtName.text = userProfile!!.userName
                binding.userTxtPhone.text = userProfile.phoneNumber
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ProfileActivity ~ failed to load", error.message)
            }
        })
    }

    private fun updateUI() {
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        updateUI()
    }

}