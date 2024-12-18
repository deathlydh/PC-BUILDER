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
import com.example.configurator.databinding.ActivityUpdateBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UpdateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateBinding
    private lateinit var databaseReference: DatabaseReference
    private var selectedCategory: String = "Процессор" // Категория по умолчанию
    private var selectedObjectKey: String = "" // Ключ выбранного объекта для обновления

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateBinding.inflate(layoutInflater)
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

        // Обработчик выбора объекта для обновления
        binding.objectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                selectedObjectKey = parent.getItemAtPosition(position).toString() // Получаем ключ объекта
                loadObjectData(selectedObjectKey) // Загружаем данные для выбранного объекта
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Сохранение изменений
        binding.saveButton.setOnClickListener {
            val newName = binding.uploadName.text.toString().trim()
            val desc = binding.uploadDesc.text.toString().trim()
            val price = binding.uploadPrice.text.toString().trim()
            val pic = binding.uploadPic.text.toString().trim()

            if (newName.isEmpty() || desc.isEmpty() || price.isEmpty() || pic.isEmpty()) {
                Toast.makeText(this, "Пожалуйста заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!price.matches("^\\d+(\\.\\d+)?$".toRegex())) {
                Toast.makeText(this, "Пожалуйста введите правильную цену (только цифры)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Проверяем, изменилось ли имя
            val isKeyChanged = selectedObjectKey != newName

            // Если ключ изменился, создаем новую запись и удаляем старую
            if (isKeyChanged) {
                val newRef = FirebaseDatabase.getInstance().getReference("Components/$selectedCategory/$newName")
                val oldRef = FirebaseDatabase.getInstance().getReference("Components/$selectedCategory/$selectedObjectKey")

                // Создаем данные под новым ключом
                val compData = CompData(newName, desc, price, pic)
                newRef.setValue(compData).addOnSuccessListener {
                    // Удаляем старую запись
                    oldRef.removeValue().addOnSuccessListener {
                        Toast.makeText(this, "Данные успешно обновлены с новым именем", Toast.LENGTH_SHORT).show()
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Ошибка удаления старой записи: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Ошибка обновления данных: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Обновляем данные под старым ключом
                val currentRef = FirebaseDatabase.getInstance().getReference("Components/$selectedCategory/$selectedObjectKey")
                val compData = CompData(newName, desc, price, pic)
                currentRef.setValue(compData).addOnSuccessListener {
                    Toast.makeText(this, "Данные успешно обновлены", Toast.LENGTH_SHORT).show()
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, "Ошибка обновления данных: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Метод для загрузки объектов по выбранной категории
    private fun loadObjectsForCategory(category: String) {
        // Здесь вы должны извлечь данные из Firebase и подставить их в Spinner
        // Для примера, предполагается, что объекты имеют уникальные ключи, которые можно использовать в Spinner.

        // Пример: список объектов для категории "Processor"
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
                }
            }
    }

    // Метод для загрузки данных объекта по ключу
    private fun loadObjectData(objectKey: String) {
        FirebaseDatabase.getInstance().getReference("Components/$selectedCategory/$objectKey")
            .get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val compData = snapshot.getValue(CompData::class.java)
                    if (compData != null) {
                        // Подставляем данные в EditText
                        binding.uploadName.setText(compData.name)
                        binding.uploadDesc.setText(compData.desc)
                        binding.uploadPrice.setText(compData.price)
                        binding.uploadPic.setText(compData.pic)
                    }
                } else {
                    Toast.makeText(this, "Объект не найден", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Ошибка загрузки данных: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
