package com.ipsmeet.chatapp.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.ipsmeet.chatapp.R
import com.ipsmeet.chatapp.databinding.ActivityProfileBinding
import com.ipsmeet.chatapp.dataclasses.SignInDataClass
import dmax.dialog.SpotsDialog
import java.io.ByteArrayOutputStream
import java.io.File


class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    lateinit var userID: String
    private lateinit var progress: SpotsDialog
    private lateinit var photo: Bitmap
    lateinit var byteArray: ByteArray
    private lateinit var imgURI: Uri

    lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progress = SpotsDialog(this, R.style.Custom2)

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

        binding.userEditImg.setOnClickListener {
            showBottomSheet()
        }

        binding.userEditName.setOnClickListener {

        }

        binding.userEditAbout.setOnClickListener {

        }

    }

    private fun showBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.bsn_edit_img)
        bottomSheetDialog.show()

        //  SELECT IMAGE FROM CAMERA
        bottomSheetDialog.findViewById<ConstraintLayout>(R.id.updateUser_layoutCam)
            ?.setOnClickListener {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,  MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())
                if (cameraIntent.resolveActivity(this.packageManager) != null) {
                    startActivityForResult(cameraIntent, 1888)
                }

                bottomSheetDialog.dismiss()
            }

        //  SELECT IMAGE FROM GALLERY
        bottomSheetDialog.findViewById<ConstraintLayout>(R.id.updateUser_layoutGallery)
            ?.setOnClickListener {
                selectImage()
                bottomSheetDialog.dismiss()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1888 && resultCode == RESULT_OK) {
            photo = data!!.extras!!["data"] as Bitmap
            binding.userProfileImg.setImageBitmap(photo)

            binding.userProfileImg.isDrawingCacheEnabled = true
            binding.userProfileImg.buildDrawingCache()
            val bitmap = (binding.userProfileImg.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            byteArray = baos.toByteArray()
            uploadImageBITMAP()
        }
    }

    private fun uploadImageBITMAP() {
        progress.show()

        val storageReference = FirebaseStorage.getInstance().getReference("Images/*$userID")

        storageReference.putBytes(byteArray)
            .addOnSuccessListener {
                progress.dismiss()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Fail", Toast.LENGTH_SHORT).show()
            }
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
            binding.userProfileImg.setImageURI(imgURI)

            progress.show()

            val storageReference = FirebaseStorage.getInstance().getReference("Images/*$userID")

            storageReference.putFile(imgURI)
                .addOnSuccessListener {
                    progress.dismiss()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Fail", Toast.LENGTH_SHORT).show()
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