package com.example.configurator

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.configurator.databinding.ActivityMyConfigDetailBinding
import com.google.firebase.database.*

class MyConfigDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyConfigDetailBinding
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyConfigDetailBinding.inflate(layoutInflater)
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

        // Получаем название сборки из Intent
        val configName = intent.getStringExtra("CONFIG_NAME")

        if (configName == null) {
            Toast.makeText(this, "Ошибка передачи данных", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Устанавливаем название сборки в заголовок
        binding.configName.text = configName

        // Указатель на сборку в Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Configurations/$configName")

        // Загружаем данные о сборке
        loadConfigDetails()
    }

    private fun loadConfigDetails() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Получение данных о сборке
                    val processor = snapshot.child("processor").getValue(String::class.java) ?: "Не указано"
                    val graphicsCard = snapshot.child("graphicsCard").getValue(String::class.java) ?: "Не указано"
                    val ram = snapshot.child("ram").getValue(String::class.java) ?: "Не указано"
                    val motherboard = snapshot.child("motherboard").getValue(String::class.java) ?: "Не указано"
                    val storage = snapshot.child("storage").getValue(String::class.java) ?: "Не указано"
                    val cooler = snapshot.child("cooler").getValue(String::class.java) ?: "Не указано"
                    val tower = snapshot.child("tower").getValue(String::class.java) ?: "Не указано"

                    // Заполняем данные на экране
                    binding.processorValue.text = processor
                    binding.graphicsCardValue.text = graphicsCard
                    binding.ramValue.text = ram
                    binding.motherboardValue.text = motherboard
                    binding.storageValue.text = storage
                    binding.coolerValue.text = cooler
                    binding.towerValue.text = tower

                    // Загружаем картинку корпуса
                    if (tower != "Не указано") {
                        loadTowerPic(tower)
                    }
                } else {
                    Toast.makeText(this@MyConfigDetailActivity, "Сборка не найдена", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MyConfigDetailActivity, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun loadTowerPic(towerName: String) {
        // Указатель на ветку корпуса в Firebase
        val towerRef = FirebaseDatabase.getInstance().getReference("Components/Корпус/$towerName")

        towerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Получаем URL картинки корпуса
                    val towerImageUrl = snapshot.child("pic").getValue(String::class.java) ?: ""

                    // Загрузка картинки корпуса с помощью Glide
                    Glide.with(this@MyConfigDetailActivity)
                        .load(towerImageUrl)
                        .placeholder(R.color.placeholder_color) // Плейсхолдер
                        .error(R.color.error_color) // Картинка ошибки
                        .into(binding.towerImage) // Привязка к ImageView корпуса
                } else {
                    Toast.makeText(this@MyConfigDetailActivity, "Корпус не найден", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MyConfigDetailActivity, "Ошибка загрузки данных корпуса", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
