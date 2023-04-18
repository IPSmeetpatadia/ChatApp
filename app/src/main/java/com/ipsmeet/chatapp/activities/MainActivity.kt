package com.ipsmeet.chatapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.ipsmeet.chatapp.R
import com.ipsmeet.chatapp.adapters.ChatAdapter
import com.ipsmeet.chatapp.databinding.ActivityMainBinding
import com.ipsmeet.chatapp.dataclasses.UserDataClass

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    var chatData = arrayListOf<UserDataClass>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (item in snapshot.children) {
                        if (item.key.toString() != FirebaseAuth.getInstance().currentUser!!.uid) {
                            val showData = item.getValue(UserDataClass::class.java)
                            showData!!.key = item.key.toString()
                            chatData.add(showData)
                            binding.mainRecyclerView.apply {
                                layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
                                adapter = ChatAdapter(this@MainActivity, chatData,
                                object : ChatAdapter.OnClick {
                                    override fun openChat(key: String) {
                                        startActivity(
                                            Intent(this@MainActivity, ChatActivity::class.java)
                                                .putExtra("userID", key)
                                        )
                                    }
                                })
                                addItemDecoration(DividerItemDecoration(this@MainActivity, DividerItemDecoration.VERTICAL))
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("MainActivity ~ Database Error", error.message)
            }
        })

        FirebaseStorage.getInstance()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_signOut -> {
                Firebase.auth.signOut()

                startActivity(
                    Intent(this, SignInActivity::class.java)
                )
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}