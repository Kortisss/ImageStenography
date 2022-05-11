package com.example.imagestenography

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.imagestenography.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        binding.btnEncodeActivity.setOnClickListener {
            val intent = Intent(this, Encode::class.java)
            startActivity(intent)
        }
        binding.btnDecodeActivity.setOnClickListener {
            val intent = Intent(this, Decode::class.java)
            startActivity(intent)
        }

        setContentView(binding.root)

    }
}