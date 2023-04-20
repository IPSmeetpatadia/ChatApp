package com.ipsmeet.chatapp.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.ipsmeet.chatapp.adapters.MessagesAdapter
import com.ipsmeet.chatapp.databinding.ActivityChatBinding
import com.ipsmeet.chatapp.dataclasses.MessagesDataClass
import com.ipsmeet.chatapp.dataclasses.UserDataClass
import java.io.File
import java.util.Date

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var chatReference: DatabaseReference
    private lateinit var firebaseDatabase: FirebaseDatabase

    private lateinit var senderRoom: String
    private lateinit var receiverRoom: String
    var chats = arrayListOf<MessagesDataClass>()
    lateinit var messagesAdapter: MessagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseDatabase = FirebaseDatabase.getInstance()

        val senderID = FirebaseAuth.getInstance().currentUser!!.uid
        val receiverID = intent.getStringExtra("userID")
        Log.d("senderID", senderID)
        Log.d("receiverID", receiverID.toString())

        senderRoom = senderID + receiverID
        receiverRoom = receiverID + senderID
        Log.d("senderRoom", senderRoom)
        Log.d("receiverRoom", receiverRoom)

        Log.d("timestamp", Date().time.toString())

        binding.commsBack.setOnClickListener {
            updateUI()
        }

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        messagesAdapter = MessagesAdapter(this@ChatActivity, chats)

        binding.commsRecyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = messagesAdapter
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("Users/$receiverID")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val data = snapshot.getValue(UserDataClass::class.java)
                    data!!.key = snapshot.key.toString()
                    Log.d("snapshot.key", snapshot.key.toString())
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

        chatReference = FirebaseDatabase.getInstance().getReference("Chats/$senderRoom/Messages")
        chatReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chats.clear()
                if (snapshot.exists()) {
                    for (msgs in snapshot.children) {
                        val comms = msgs.getValue(MessagesDataClass::class.java)
                        chats.add(comms!!)
                    }
                    messagesAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Failed to load chats", error.message)
            }
        })

        binding.commsTypeMsg.addTextChangedListener(messageSend)

        binding.btnSendMsg.setOnClickListener {
            chats.clear()
            val msg = MessagesDataClass(
                message = binding.commsTypeMsg.text.toString(),
                senderID = senderID,
                timeStamp = Date().time.toFloat()
            )

            firebaseDatabase.getReference("Chats")
                .child(senderRoom)
                .child("Messages")
                .push()
                .setValue(msg)
                .addOnCompleteListener {
                    firebaseDatabase.getReference("Chats")
                        .child(receiverRoom)
                        .child("Messages")
                        .push()
                        .setValue(msg)
                }
            binding.commsTypeMsg.setText("")
        }

    }

    private var messageSend: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            binding.btnSendMsg.isEnabled = false
        }

        override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            binding.btnSendMsg.isEnabled = binding.commsTypeMsg.text.isNotEmpty()
        }

        override fun afterTextChanged(s: Editable?) {
            binding.btnSendMsg.isEnabled = binding.commsTypeMsg.text.isNotEmpty()
        }
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