package com.example.configurator

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.configurator.databinding.ActivityDeleteConfigBinding
import com.google.firebase.database.*

class DeleteConfigActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeleteConfigBinding
    private lateinit var databaseReference: DatabaseReference
    private var selectedConfigName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteConfigBinding.inflate(layoutInflater)
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

        binding.deleteButton.setOnClickListener {
            deleteConfiguration()
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
                Toast.makeText(this@DeleteConfigActivity, "Ошибка загрузки конфигураций", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupConfigSpinner(configList: List<String>) {
        val configAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, configList)
        configAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.configSelectionSpinner.adapter = configAdapter

        binding.configSelectionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedConfigName = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun deleteConfiguration() {
        if (selectedConfigName.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, выберите сборку для удаления", Toast.LENGTH_SHORT).show()
            return
        }

        databaseReference.child(selectedConfigName).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Сборка успешно удалена", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Ошибка удаления: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
