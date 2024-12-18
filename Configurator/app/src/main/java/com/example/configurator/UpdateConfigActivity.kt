package com.example.configurator

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.configurator.databinding.ActivityUpdateConfigBinding
import com.google.firebase.database.*

class UpdateConfigActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateConfigBinding
    private lateinit var databaseReference: DatabaseReference

    private var originalConfigName: String = "" // Оригинальное имя конфигурации
    private var selectedProcessor: String = ""
    private var selectedGraphicsCard: String = ""
    private var selectedRam: String = ""
    private var selectedMotherboard: String = ""
    private var selectedStorage: String = ""
    private var selectedCooler: String = ""
    private var selectedTower: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateConfigBinding.inflate(layoutInflater)
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

        databaseReference = FirebaseDatabase.getInstance().getReference("Configurations")

        loadConfigurations()

        binding.updateButton.setOnClickListener {
            updateConfiguration()
        }
    }

    private fun loadConfigurations() {
        val configList = mutableListOf<String>()
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (configSnapshot in snapshot.children) {
                        configList.add(configSnapshot.key.toString())
                    }
                    setupConfigSpinner(configList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UpdateConfigActivity, "Ошибка загрузки конфигураций", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupConfigSpinner(configList: List<String>) {
        val configAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, configList)
        configAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.configSelectionSpinner.adapter = configAdapter

        binding.configSelectionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                originalConfigName = parent.getItemAtPosition(position).toString()
                binding.configName.setText(originalConfigName) // Устанавливаем имя в EditText
                loadConfigDetails(originalConfigName)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun loadConfigDetails(configName: String) {
        databaseReference.child(configName).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val config = snapshot.getValue(ConfigurationData::class.java)
                    if (config != null) {
                        setupComponentSpinners(config)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UpdateConfigActivity, "Ошибка загрузки данных конфигурации", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupComponentSpinners(config: ConfigurationData) {
        setupSpinner(binding.processorSpinner, "Процессор", config.processor)
        setupSpinner(binding.graphicsCardSpinner, "Видеокарта", config.graphicsCard)
        setupSpinner(binding.ramSpinner, "Оперативная память", config.ram)
        setupSpinner(binding.motherboardSpinner, "Материнская плата", config.motherboard)
        setupSpinner(binding.storageSpinner, "Диск", config.storage)
        setupSpinner(binding.coolerSpinner, "Кулер", config.cooler)
        setupSpinner(binding.towerSpinner, "Корпус", config.tower)
    }

    private fun setupSpinner(spinner: Spinner, category: String, selectedValue: String) {
        val componentList = mutableListOf<String>()
        FirebaseDatabase.getInstance().getReference("Components/$category")
            .get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    for (childSnapshot in snapshot.children) {
                        componentList.add(childSnapshot.key.toString())
                    }

                    val componentAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, componentList)
                    componentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = componentAdapter

                    val selectedIndex = componentList.indexOf(selectedValue)
                    if (selectedIndex >= 0) spinner.setSelection(selectedIndex)

                    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
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
    }

    private fun updateConfiguration() {
        val newConfigName = binding.configName.text.toString().trim()

        if (newConfigName.isEmpty()) {
            Toast.makeText(this, "Введите имя сборки", Toast.LENGTH_SHORT).show()
            return
        }

        if (newConfigName != originalConfigName) {
            // Удаляем старую запись и создаем новую
            databaseReference.child(originalConfigName).removeValue().addOnSuccessListener {
                saveConfiguration(newConfigName)
            }.addOnFailureListener {
                Toast.makeText(this, "Ошибка обновления ключа: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            saveConfiguration(originalConfigName)
        }
    }

    private fun saveConfiguration(configName: String) {
        val updatedConfig = ConfigurationData(
            configName,
            selectedProcessor,
            selectedGraphicsCard,
            selectedRam,
            selectedMotherboard,
            selectedStorage,
            selectedCooler,
            selectedTower
        )

        databaseReference.child(configName).setValue(updatedConfig).addOnSuccessListener {
            Toast.makeText(this, "Сборка успешно обновлена", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Ошибка сохранения данных: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
