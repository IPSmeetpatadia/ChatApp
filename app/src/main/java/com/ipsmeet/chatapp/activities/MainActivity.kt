package com.ipsmeet.chatapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ipsmeet.chatapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}