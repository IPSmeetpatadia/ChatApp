package com.ipsmeet.chatapp.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.ipsmeet.chatapp.R
import com.ipsmeet.chatapp.databinding.ActivityProfileBinding
import com.ipsmeet.chatapp.dataclasses.SignInDataClass
import dmax.dialog.SpotsDialog

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var imgURI: Uri? = null
    private lateinit var progress: SpotsDialog

    private var databaseReference = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val phoneNumber = intent.getStringExtra("phoneNum").toString()

        progress = SpotsDialog(this, R.style.Custom)
        progress.setTitle("Please Wait!!")

        binding.profileEdtxtContact.setText(phoneNumber)

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

        val storageReference = FirebaseStorage.getInstance().getReference("Images/*${ FirebaseAuth.getInstance().currentUser!!.uid }")

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
            imgURI?.buildUpon()?.build().toString(),
            binding.profileEdtxtName.text.toString(),
            binding.profileEdtxtContact.text.toString()
        )

        val userID = FirebaseAuth.getInstance().currentUser!!.uid
        databaseReference.getReference("Users/$userID").setValue(addUser)
        /**
        database.child("Users").child(userID).child(phoneNumber.toString()).setValue(addUser)
        */
    }

}