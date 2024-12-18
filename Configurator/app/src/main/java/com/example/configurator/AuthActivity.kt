package com.example.configurator

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import com.example.configurator.databinding.ActivityAuthBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var passInput: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = resources.getColor(android.R.color.white, theme)
        window.navigationBarColor = resources.getColor(android.R.color.white, theme)

        window.decorView.systemUiVisibility = (
                window.decorView.systemUiVisibility or
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                        View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                )

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        binding.signInButton.setOnClickListener {
            val loginInput = binding.login.text.toString()
            passInput = binding.pass.text.toString()
            if (loginInput.isNotEmpty() && passInput.isNotEmpty())
            {
                signIn(loginInput, passInput)
            } else {
                Toast.makeText(this, "Введите логин и пароль",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signIn(login: String, passInput: String) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Authorization")
        databaseReference.child(login).get().addOnSuccessListener{
            if (it.exists()) {
                val password = it.child("password").value
                if (password.toString() == passInput) {
                    val intent = Intent(this@AuthActivity, AdminActivity::class.java)
                    startActivity(intent)
                    finish()
                    Toast.makeText(this, "Вы успешно авторизовались", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Неверный пароль",
                        Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Неверный логин",
                    Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Ошибка",
                Toast.LENGTH_SHORT).show()
        }
    }
}