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
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var imgURI: Uri
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
            addUser(phoneNumber)
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

        val fileName = SimpleDateFormat("yyyy_MM_dd_HH_mm", Locale.getDefault()).format(Date())
        val storageReference = FirebaseStorage.getInstance().getReference("Images/*$fileName")

        storageReference.putFile(imgURI)
            .addOnSuccessListener {
                progress.dismiss()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Fail", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addUser(phoneNumber: String?) {
        val addUser = SignInDataClass(
            imgURI.buildUpon().build().toString(),
            binding.profileEdtxtName.text.toString(),
            binding.profileEdtxtContact.text.toString()
        )

        val userID = FirebaseAuth.getInstance().currentUser!!.uid
        databaseReference.getReference("Users/$userID").child(phoneNumber.toString()).setValue(addUser)
        /**
        database.child("Users").child(userID).child(phoneNumber.toString()).setValue(addUser)
        */
    }
}