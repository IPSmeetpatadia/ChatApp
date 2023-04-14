package com.ipsmeet.chatapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.ipsmeet.chatapp.databinding.ActivitySignInBinding
import java.util.concurrent.TimeUnit

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding

    private lateinit var phoneNo: String
    private lateinit var auth: FirebaseAuth

    lateinit var storedVerificationID:String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnSignIn.setOnClickListener {
            startLogin(binding.signInPhoneNumber.text.toString())
        }

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                startActivity(
                    Intent(this@SignInActivity, MainActivity::class.java)
                )
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(this@SignInActivity, e.message.toString(), Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(verificationID: String, token: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(verificationID, token)

                storedVerificationID = verificationID
                resendToken = token

                startActivity(
                    Intent(this@SignInActivity, OTPActivity::class.java)
                        .putExtra("storedVerificationID", storedVerificationID)
                )
            }
        }
    }

    private fun startLogin(number: String) {
        if (number.isNotEmpty()) {
            phoneNo = "+91$number"
            sendOTP(phoneNo)
        }
        else {
            Toast.makeText(this, "Enter phone number!!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendOTP(number: String) {
        val option = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number)
            .setTimeout(30L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(option)
    }

}