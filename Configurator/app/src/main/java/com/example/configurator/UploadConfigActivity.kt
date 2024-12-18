package com.example.configurator

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.configurator.databinding.ActivityUploadConfigBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UploadConfigActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadConfigBinding
    private lateinit var databaseReference: DatabaseReference
    private var selectedProcessor: String = "" // Выбранный процессор
    private var selectedGraphicsCard: String = "" // Выбранная видеокарта
    private var selectedRam: String = "" // Выбранная память
    private var selectedMotherboard: String = "" // Выбранная материнская плата
    private var selectedStorage: String = "" // Выбранное хранилище
    private var selectedCooler: String = "" // Выбранный кулер
    private var selectedTower: String = "" // Выбранная башня

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadConfigBinding.inflate(layoutInflater)
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

        // Настройка Spinner для выбора комплектующих
        val categories = listOf("Процессор", "Видеокарта", "Оперативная память", "Материнская плата", "Диск", "Кулер", "Корпус")
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Заполнение спиннеров для каждой категории
        setupSpinner(binding.processorSpinner, "Процессор", categoryAdapter)
        setupSpinner(binding.graphicsCardSpinner, "Видеокарта", categoryAdapter)
        setupSpinner(binding.ramSpinner, "Оперативная память", categoryAdapter)
        setupSpinner(binding.motherboardSpinner, "Материнская плата", categoryAdapter)
        setupSpinner(binding.storageSpinner, "Диск", categoryAdapter)
        setupSpinner(binding.coolerSpinner, "Кулер", categoryAdapter)
        setupSpinner(binding.towerSpinner, "Корпус", categoryAdapter)

        // Сохранение конфигурации
        binding.saveButton.setOnClickListener {
            val configName = binding.configName.text.toString()

            if (configName.isEmpty() || selectedProcessor.isEmpty() || selectedGraphicsCard.isEmpty() ||
                selectedRam.isEmpty() || selectedMotherboard.isEmpty() || selectedStorage.isEmpty() ||
                selectedCooler.isEmpty() || selectedTower.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, выберите все комплектующие и укажите название сборки", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Получаем ссылку на узел "Configurations" в Firebase
            databaseReference = FirebaseDatabase.getInstance().getReference("Configurations")

            // Проверяем, существует ли сборка с таким названием
            databaseReference.child(configName).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Если сборка уже существует, выводим сообщение
                    Toast.makeText(this, "Сборка с таким названием уже существует", Toast.LENGTH_SHORT).show()
                } else {
                    // Создаем объект конфигурации
                    val configData = ConfigurationData(
                        configName,
                        selectedProcessor,
                        selectedGraphicsCard,
                        selectedRam,
                        selectedMotherboard,
                        selectedStorage,
                        selectedCooler,
                        selectedTower
                    )

                    // Сохраняем данные в Firebase
                    databaseReference.child(configName).setValue(configData).addOnSuccessListener {
                        // Очистка полей после успешного сохранения
                        binding.configName.text.clear()

                        Toast.makeText(this, "Сборка успешно сохранена", Toast.LENGTH_SHORT).show()
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

    private fun setupSpinner(spinner: Spinner, category: String, adapter: ArrayAdapter<String>) {
        val componentList = mutableListOf<String>()

        FirebaseDatabase.getInstance().getReference("Components/$category")
            .get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    for (childSnapshot in snapshot.children) {
                        componentList.add(childSnapshot.key.toString()) // Добавляем компоненты в список
                    }

                    // Настроить спиннер для выбора компонента
                    val componentAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, componentList)
                    componentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = componentAdapter
                }
            }

        // Обработчик выбора компонента
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                when (category) {
                    "Процессор" -> selectedProcessor = parent.getItemAtPosition(position).toString()
                    "Видеокарта" -> selectedGraphicsCard = parent.getItemAtPosition(position).toString()
                    "Оперативная память" -> selectedRam = parent.getItemAtPosition(position).toString()
                    "Материнская плата" -> selectedMotherboard = parent.getItemAtPosition(position).toString()
                    "Диск" -> selectedStorage = parent.getItemAtPosition(position).toString()
                    "Кулер" -> selectedCooler = parent.getItemAtPosition(position).toString()
                    "Корпус" -> selectedTower = parent.getItemAtPosition(position).toString()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}
