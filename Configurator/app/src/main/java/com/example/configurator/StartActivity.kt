package com.example.configurator

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.example.configurator.databinding.ActivityStartBinding

class StartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStartBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = resources.getColor(android.R.color.white, theme)
        window.navigationBarColor = resources.getColor(android.R.color.white, theme)

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        binding.mainAllComp.setOnClickListener {
            val intent = Intent(this@StartActivity, CompListActivity::class.java)
            startActivity(intent)
        }
        binding.mainMyConfig.setOnClickListener {
            val intent = Intent(this@StartActivity, MyConfigActivity::class.java)
            startActivity(intent)
        }
    }
}