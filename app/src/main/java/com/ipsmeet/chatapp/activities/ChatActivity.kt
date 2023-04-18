package com.ipsmeet.chatapp.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.ipsmeet.chatapp.databinding.ActivityChatBinding
import com.ipsmeet.chatapp.dataclasses.UserDataClass
import java.io.File

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userID = intent.getStringExtra("userID")

        binding.commsProfile.setOnClickListener {
            startActivity(
                Intent(this, MainActivity::class.java)
            )
            finish()
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("Users/$userID")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val data = snapshot.getValue(UserDataClass::class.java)
                    data!!.key = snapshot.key.toString()
                    binding.commsName.text = data.userName

                    val localFile = File.createTempFile("tempfile", "jpeg")
                    FirebaseStorage.getInstance()
                        .getReference("Images/*${snapshot.key}").getFile(localFile)
                        .addOnSuccessListener {
                            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                            Glide.with(applicationContext).load(bitmap).into(binding.commsProfile)
                        }
                        .addOnFailureListener {
                            Log.d("Fail to load user profiles", it.message.toString())
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ChatActivity ~ Failed", error.message)
            }
        })
    }

}