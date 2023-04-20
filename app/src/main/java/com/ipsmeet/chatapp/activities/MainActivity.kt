package com.ipsmeet.chatapp.activities

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.ipsmeet.chatapp.R
import com.ipsmeet.chatapp.adapters.ChatAdapter
import com.ipsmeet.chatapp.databinding.ActivityMainBinding
import com.ipsmeet.chatapp.databinding.LayoutDialogBinding
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

        //  DISPLAYING ALL USERS IN AS CHAT, BUT AVOIDING LOGGED-IN USER
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (item in snapshot.children) {
                        //  to avoid logged-in user
                        if (item.key.toString() != FirebaseAuth.getInstance().currentUser!!.uid) {
                            val showData = item.getValue(UserDataClass::class.java)
                            showData!!.key = item.key.toString()
                            chatData.add(showData)
                            binding.mainRecyclerView.apply {
                                layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
                                adapter = ChatAdapter(this@MainActivity, chatData,
                                object : ChatAdapter.OnClick {
                                    override fun openChat(key: String) {    //  open chat
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
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
            }

            R.id.menu_signOut -> {
                val bindDialog :LayoutDialogBinding = LayoutDialogBinding.inflate(LayoutInflater.from(this))

                val dialog = Dialog(this)
                dialog.setContentView(bindDialog.root)
                dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()

                bindDialog.signOutYes.setOnClickListener {
                    Firebase.auth.signOut()
                    updateUI()
                    dialog.dismiss()
                }

                bindDialog.signOutNo.setOnClickListener {
                    dialog.dismiss()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateUI() {
        startActivity(
            Intent(this, SignInActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)   // all of the other activities on top of it will be closed
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)    // activity will become the start of a new task on this history stack
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)  // activity becomes the new root of an otherwise empty task, and any old activities are finished
        )
        finish()
    }

}