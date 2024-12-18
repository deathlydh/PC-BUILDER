package com.example.configurator

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.configurator.databinding.ActivityDeleteBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DeleteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeleteBinding
    private lateinit var databaseReference: DatabaseReference
    private var selectedCategory: String = "Процессор" // Категория по умолчанию
    private var selectedObjectKey: String = "" // Ключ выбранного объекта для удаления

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteBinding.inflate(layoutInflater)
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

        // Настройка Spinner для выбора категории
        val categories = listOf("Процессор", "Видеокарта", "Оперативная память", "Материнская плата", "Диск", "Кулер", "Корпус")
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = categoryAdapter

        // Обработчик выбора категории
        binding.categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                selectedCategory = categories[position]
                loadObjectsForCategory(selectedCategory) // Загружаем объекты для выбранной категории
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Обработчик выбора объекта для удаления
        binding.objectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                selectedObjectKey = parent.getItemAtPosition(position).toString() // Получаем ключ объекта
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Обработчик нажатия на кнопку для удаления
        binding.deleteButton.setOnClickListener {
            if (selectedObjectKey.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, выберите объект для удаления", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Удаление выбранного объекта
            databaseReference = FirebaseDatabase.getInstance().getReference("Components/$selectedCategory/$selectedObjectKey")

            databaseReference.removeValue().addOnSuccessListener {
                Toast.makeText(this, "Объект удален", Toast.LENGTH_SHORT).show()
                loadObjectsForCategory(selectedCategory) // Обновляем список объектов
                deleteConfigurationsWithDeletedObject(selectedCategory, selectedObjectKey)
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Ошибка удаления: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteConfigurationsWithDeletedObject(category: String, deletedObjectKey: String) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("Configurations")

        databaseRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                for (childSnapshot in snapshot.children) {
                    val configName = childSnapshot.key.toString()
                    val configData = childSnapshot.getValue(ConfigurationData::class.java)

                    configData?.let {
                        // Проверяем, используется ли удаленный объект в сборке
                        val shouldDelete = when (category) {
                            "Процессор" -> it.processor == deletedObjectKey
                            "Видеокарта" -> it.graphicsCard == deletedObjectKey
                            "Оперативная память" -> it.ram == deletedObjectKey
                            "Материнская плата" -> it.motherboard == deletedObjectKey
                            "Диск" -> it.storage == deletedObjectKey
                            "Кулер" -> it.cooler == deletedObjectKey
                            "Корпус" -> it.tower == deletedObjectKey
                            else -> false
                        }

                        // Если объект найден, удаляем сборку
                        if (shouldDelete) {
                            databaseRef.child(configName).removeValue()
                                .addOnSuccessListener {
                                    Log.d("DeleteActivity", "Config $configName deleted successfully")
                                }
                                .addOnFailureListener { error ->
                                    Log.e("DeleteActivity", "Error deleting config $configName: ${error.message}")
                                }
                        }
                    }
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Ошибка удаления сборок: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Метод для загрузки объектов по выбранной категории
    private fun loadObjectsForCategory(category: String) {
        val objects = mutableListOf<String>()
        FirebaseDatabase.getInstance().getReference("Components/$category")
            .get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    for (childSnapshot in snapshot.children) {
                        // Добавляем ключи объектов в список
                        objects.add(childSnapshot.key.toString())
                    }

                    // Заполняем второй Spinner объектами
                    val objectAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, objects)
                    objectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.objectSpinner.adapter = objectAdapter
                } else {
                    Toast.makeText(this, "Нет объектов в этой категории", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Ошибка загрузки объектов: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
