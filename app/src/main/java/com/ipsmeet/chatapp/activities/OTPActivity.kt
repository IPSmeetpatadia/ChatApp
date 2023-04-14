package com.ipsmeet.chatapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.ipsmeet.chatapp.R
import com.ipsmeet.chatapp.databinding.ActivityOtpBinding
import dmax.dialog.SpotsDialog

class OTPActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpBinding
    private lateinit var progress: SpotsDialog
    private lateinit var phoneNumber: String

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        phoneNumber = intent.getStringExtra("phoneNumber").toString()

        progress = SpotsDialog(this, R.style.Custom)
        progress.setTitle("Please Wait!!")

        val storedVerificationID = intent.getStringExtra("storedVerificationID")

        binding.btnVerify.setOnClickListener {
            progress.show()

            val otp = binding.otpEnterOTP.text.toString()

            if (otp.isNotEmpty()) {
                val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(storedVerificationID.toString(), otp)

                auth.signInWithCredential(credential)
                    .addOnCompleteListener(this) {
                        if (it.isSuccessful) {
                            updateUI()
                        }
                        else {
                            Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private fun updateUI() {
        progress.dismiss()
        startActivity(
            Intent(this@OTPActivity, ProfileActivity::class.java)
                .putExtra("phoneNum", "+91 $phoneNumber")
        )
    }

}