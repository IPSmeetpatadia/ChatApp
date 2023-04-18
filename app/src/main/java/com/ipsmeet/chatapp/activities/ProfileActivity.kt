package com.ipsmeet.chatapp.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.ipsmeet.chatapp.R
import com.ipsmeet.chatapp.databinding.ActivityProfileBinding
import com.ipsmeet.chatapp.dataclasses.SignInDataClass
import com.ipsmeet.chatapp.dataclasses.UserDataClass
import dmax.dialog.SpotsDialog
import java.io.File

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var imgURI: Uri? = null
    private lateinit var progress: SpotsDialog

    private lateinit var userID: String
    private var firebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userID = FirebaseAuth.getInstance().currentUser!!.uid

        val phoneNumber = intent.getStringExtra("phoneNum").toString()
        binding.profileEdtxtContact.setText(phoneNumber)

        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (item in snapshot.children) {
                        val data = item.getValue(UserDataClass::class.java)
                        data!!.key = item.key.toString()
                        if (data.key == userID) {
                            /** TO FETCH USER PROFILE

                            val localFile = File.createTempFile("tempfile", "jpeg")
                            FirebaseStorage.getInstance()
                                .getReference("Images/*${data.key}").getFile(localFile)
                                .addOnSuccessListener {
                                    val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                                    Glide.with(this@ProfileActivity).load(bitmap).into(binding.profileImg)
                                }
                                .addOnFailureListener {
                                    Log.d("Fail to load user profiles", it.message.toString())
                                }
                            */*/
                            binding.profileEdtxtName.setText(data.userName)

                            binding.btnSaveProfile.setOnClickListener {
                                updateUser()
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Profile ~ Failed to fetch snapshot", error.message)
            }
        })

        progress = SpotsDialog(this, R.style.Custom)
        progress.setTitle("Please Wait!!")

        binding.profileImg.setOnClickListener {
            selectImage()
        }

        binding.btnSaveProfile.setOnClickListener {
            progress.show()
            addUser()
            uploadImage()
            updateUI()
        }
    }

    private fun updateUI() {
        progress.dismiss()
        startActivity(
            Intent(this, MainActivity::class.java)
        )
    }

    private fun updateUser() {
        FirebaseDatabase.getInstance().getReference("Users/$userID").apply {
            child("userName").setValue(binding.profileEdtxtName.text.toString())
            child("phoneNumber").setValue(binding.profileEdtxtContact.text.toString())
        }
        uploadImage()
        updateUI()
    }

    private fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        imagePicker.launch(intent)
    }

    private val imagePicker: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            imgURI = result.data?.data!!
            binding.profileImg.setImageURI(imgURI)
        }

    private fun uploadImage() {
        progress.show()

        val storageReference = FirebaseStorage.getInstance().getReference("Images/*$userID")

        storageReference.putFile(imgURI!!)
            .addOnSuccessListener {
                progress.dismiss()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Fail", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addUser() {
        val addUser = SignInDataClass(
            binding.profileEdtxtName.text.toString(),
            binding.profileEdtxtContact.text.toString()
        )

        val userID = FirebaseAuth.getInstance().currentUser!!.uid
        firebaseDatabase.getReference("Users/$userID").setValue(addUser)
        /**
        database.child("Users").child(userID).child(phoneNumber.toString()).setValue(addUser)
        */
    }

}