package com.example.configurator

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.configurator.databinding.ActivityUploadBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UploadActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadBinding
    private lateinit var databaseReference: DatabaseReference
    private var selectedCategory: String = "Процессор" // Категория по умолчанию

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
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

        // Начальные параметры
        val defaultMinLines = 1
        val defaultMaxLines = 1
        val expandedMaxLines = 5
        val editText = findViewById<EditText>(R.id.uploadDesc)

        // Установка слушателя фокуса
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Если поле получает фокус, увеличиваем его
                editText.minLines = expandedMaxLines
                editText.maxLines = expandedMaxLines
            } else {
                // Если фокус теряется, возвращаем начальные размеры
                editText.minLines = defaultMinLines
                editText.maxLines = defaultMaxLines
            }
        }

        // Настройка Spinner для выбора категории
        val categories = listOf("Процессор", "Видеокарта", "Оперативная память", "Материнская плата", "Диск", "Кулер", "Корпус")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = adapter

        // Обработчик выбора категории
        binding.categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                selectedCategory = categories[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Сохранение данных
        binding.saveButton.setOnClickListener {
            val name = binding.uploadName.text.toString()
            val desc = binding.uploadDesc.text.toString()
            val price = binding.uploadPrice.text.toString()
            val pic = binding.uploadPic.text.toString()

            if (name.isEmpty() || desc.isEmpty() || price.isEmpty() || pic.isEmpty()) {
                Toast.makeText(this, "Пожалуйста заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!price.matches("^\\d+(\\.\\d+)?$".toRegex())) {
                Toast.makeText(this, "Пожалуйста введите правильную цену (только цифры)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Получаем ссылку на категорию в Firebase
            databaseReference = FirebaseDatabase.getInstance().getReference("Components/$selectedCategory")

            // Проверяем, существует ли объект с таким названием
            databaseReference.child(name).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Объект с таким именем уже существует
                    Toast.makeText(this, "Такой объект уже имеется", Toast.LENGTH_SHORT).show()
                } else {
                    // Объекта нет, выполняем сохранение
                    val compData = CompData(name, desc, price, pic)
                    databaseReference.child(name).setValue(compData).addOnSuccessListener {
                        // Очистка полей после успешного сохранения
                        binding.uploadName.text.clear()
                        binding.uploadDesc.text.clear()
                        binding.uploadPrice.text.clear()
                        binding.uploadPic.text.clear()
                        Toast.makeText(this, "Успешно сохранено в $selectedCategory", Toast.LENGTH_SHORT).show()
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Ошибка сохранения данных: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Ошибка проверки данных: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

    }
}
