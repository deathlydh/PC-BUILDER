package com.example.configurator

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.configurator.databinding.ActivityListDetailBinding
import com.google.firebase.database.*

class ListDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListDetailBinding
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListDetailBinding.inflate(layoutInflater)
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

        // Получение названия выбранного объекта из Intent
        val itemName = intent.getStringExtra("ITEM_NAME")  // Используй "ITEM_NAME", а не "COMPONENT_NAME"
        val categoryName = intent.getStringExtra("CATEGORY_NAME")

        if (itemName == null || categoryName == null) {
            Toast.makeText(this, "Ошибка передачи данных", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Указатель на категорию и объект в Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Components/$categoryName/$itemName")

        loadItemDetails()
    }

    private fun loadItemDetails() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java) ?: "Нет названия"
                    val description = snapshot.child("desc").getValue(String::class.java) ?: "Нет описания"
                    val price = snapshot.child("price").getValue(String::class.java) ?: "Нет цены"
                    val imageUrl = snapshot.child("pic").getValue(String::class.java) ?: ""

                    // Заполнение данных на экране
                    binding.itemName.text = name
                    binding.itemName2.text = name
                    binding.itemDescription.text = description
                    binding.itemPrice.text = "Цена: $price ₽"

                    // Загрузка картинки с помощью Glide
                    Glide.with(this@ListDetailActivity)
                        .load(imageUrl)
                        .placeholder(R.color.placeholder_color) // Плейсхолдер
                        .error(R.color.error_color) // Картинка ошибки
                        .into(binding.itemImage)
                } else {
                    Toast.makeText(this@ListDetailActivity, "Объект не найден", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ListDetailActivity, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
