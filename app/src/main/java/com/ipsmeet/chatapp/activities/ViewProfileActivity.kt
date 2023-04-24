package com.ipsmeet.chatapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ipsmeet.chatapp.databinding.ActivityViewProfileBinding

class ViewProfileActivity : AppCompatActivity() {

    lateinit var binding: ActivityViewProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}