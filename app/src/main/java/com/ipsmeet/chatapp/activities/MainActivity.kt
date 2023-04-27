package com.ipsmeet.chatapp.activities

import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.ipsmeet.chatapp.R
import com.ipsmeet.chatapp.adapters.ChatAdapter
import com.ipsmeet.chatapp.adapters.FoundUserAdapter
import com.ipsmeet.chatapp.databinding.ActivityMainBinding
import com.ipsmeet.chatapp.databinding.LayoutAddFriendsBinding
import com.ipsmeet.chatapp.databinding.LayoutDialogBinding
import com.ipsmeet.chatapp.databinding.PopupViewProfileBinding
import com.ipsmeet.chatapp.dataclasses.UserDataClass
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bindingDialog: LayoutAddFriendsBinding

    lateinit var userID: String
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    var listPhoneNumbers = arrayListOf<String>()
    var chatData = arrayListOf<UserDataClass>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        userID = auth.currentUser!!.uid

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener {
                val userToken = HashMap<String, Any>()
                userToken["token"] = it.toString()  // userToken.put("token", it.toString())

                FirebaseDatabase.getInstance().getReference("Users/$userID")
                    .updateChildren(userToken)
            }

        //  DISPLAYING FRIENDS AS CHAT
        databaseReference = FirebaseDatabase.getInstance().getReference("Users/$userID/Friend List")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatData.clear()
                if (snapshot.exists()) {
                    for (item in snapshot.children) {
                        Log.d("item", item.value.toString())

                        FirebaseDatabase.getInstance().getReference("Users").orderByChild("userName")
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        for (i in snapshot.children) {
                                            if (i.key.toString() == item.value.toString()) {
                                                val showData = i.getValue(UserDataClass::class.java)
                                                showData!!.key = i.key.toString()
                                                Log.d("showData.phoneNumber", showData.phoneNumber)
                                                chatData.add(showData)

                                                binding.mainRecyclerView.apply {
                                                    layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL,false)
                                                    adapter = ChatAdapter(this@MainActivity, chatData,
                                                            object : ChatAdapter.OnClick {
                                                                //  open chat
                                                                override fun openChat(key: String, token: String) {
                                                                    startActivity(
                                                                        Intent(this@MainActivity, ChatActivity::class.java)
                                                                            .putExtra("userID", key)
                                                                            .putExtra("token", token)
                                                                    )
                                                                }

                                                                //  open dialog-box to show profile image
                                                                override fun viewProfilePopup(key: String, token: String) {
                                                                    Log.d("key", key)
                                                                    Log.d("token", token)

                                                                    val bindDialog: PopupViewProfileBinding = PopupViewProfileBinding.inflate(LayoutInflater.from(this@MainActivity))

                                                                    val dialog = Dialog(this@MainActivity)
                                                                    dialog.setContentView(bindDialog.root)
                                                                    dialog.show()

                                                                    FirebaseDatabase.getInstance().getReference("Users/$key")
                                                                        .addValueEventListener(object : ValueEventListener {
                                                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                                                if (snapshot.exists()) {

                                                                                    Log.d("snapshot", snapshot.toString())

                                                                                    val data = snapshot.getValue(UserDataClass::class.java)
                                                                                    data!!.key = snapshot.key.toString()
                                                                                    bindDialog.popupViewName.text = data.userName
                                                                                    Log.d("data.userName", data.userName)

                                                                                    //  FETCHING USER PROFILE FROM FIREBASE-STORAGE
                                                                                    val localFile = File.createTempFile("tempfile", "jpeg")
                                                                                    FirebaseStorage.getInstance()
                                                                                        .getReference("Images/*${key}")
                                                                                        .getFile(localFile)
                                                                                        .addOnSuccessListener {
                                                                                            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                                                                                            Glide.with(applicationContext).load(bitmap).into(bindDialog.popupViewProfile)
                                                                                        }
                                                                                        .addOnFailureListener {
                                                                                            Log.d("Fail to load user profiles", it.message.toString())
                                                                                        }
                                                                                }
                                                                            }

                                                                            override fun onCancelled(error: DatabaseError) {
                                                                                Log.d("popup failed", error.message)
                                                                            }
                                                                        })

                                                                    bindDialog.popupSendMsg.setOnClickListener {
                                                                        startActivity(
                                                                            Intent(this@MainActivity, ChatActivity::class.java)
                                                                                .putExtra("userID", key)
                                                                                .putExtra("token", token)
                                                                        )
                                                                        dialog.dismiss()
                                                                    }

                                                                    bindDialog.popupInfo.setOnClickListener {
                                                                        startActivity(
                                                                            Intent(this@MainActivity, ViewProfileActivity::class.java)
                                                                                .putExtra("userID", key)
                                                                                .putExtra("token", token)
                                                                        )
                                                                        dialog.dismiss()
                                                                    }
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
        when (item.itemId) {
            R.id.menu_addFriend -> {
                Log.d("phone numbers", listPhoneNumbers.toString())
                bindingDialog = LayoutAddFriendsBinding.inflate(LayoutInflater.from(this))

                val dialog = Dialog(this)
                dialog.setContentView(bindingDialog.root)
                dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()

                bindingDialog.layoutSearch.setOnClickListener {
                    var matchedNumber = ""

                    FirebaseDatabase.getInstance().getReference("Users")
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                chatData.clear()
                                if (snapshot.exists()) {
                                    for (i in snapshot.children) {
                                        val user = i.getValue(UserDataClass::class.java)
                                        user!!.key = i.key.toString()
                                        listPhoneNumbers.add(user.phoneNumber)
                                        Log.d("listPhoneNumbers", listPhoneNumbers.toString())

                                        for (num in listPhoneNumbers) {
                                            if (bindingDialog.edtxtFindPhone.text.toString() == num) {
                                                Toast.makeText(this@MainActivity, "User Found", Toast.LENGTH_SHORT).show()
                                                Log.d("matched number", num)
                                                matchedNumber = num
                                            }
                                        }

                                        if (matchedNumber == user.phoneNumber) {
                                            Log.d("user.userName", user.key)
                                            bindingDialog.recyclerViewFoundUSer.apply {
                                                layoutManager = LinearLayoutManager(dialog.context, LinearLayoutManager.VERTICAL,false)
                                                adapter = FoundUserAdapter(dialog.context, user,
                                                        object : FoundUserAdapter.OnClick {
                                                            override fun clickListener(key: String) {
                                                                FirebaseDatabase.getInstance().getReference("Users/$userID")
                                                                    .child("Friend List")
                                                                    .push()
                                                                    .setValue(key)
                                                                dialog.dismiss()
                                                            }
                                                        })
                                            }
                                        }
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.d("Failed to find user", error.message)
                            }
                        })
                }

            }

            R.id.menu_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
            }

            R.id.menu_signOut -> {
                val bindDialog: LayoutDialogBinding =
                    LayoutDialogBinding.inflate(LayoutInflater.from(this))

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

    override fun onResume() {
        super.onResume()
        chatData.clear()
    }

}