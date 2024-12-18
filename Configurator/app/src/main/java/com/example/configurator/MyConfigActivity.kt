package com.example.configurator

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.configurator.databinding.ActivityMyConfigBinding
import com.google.firebase.database.*

class MyConfigActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyConfigBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var configList: MutableList<String>
    private lateinit var fullConfigList: MutableList<String>
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyConfigBinding.inflate(layoutInflater)
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

        // Инициализация списка и адаптера
        configList = mutableListOf()
        fullConfigList = mutableListOf()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, configList)
        binding.listView.adapter = adapter

        // Получаем ссылку на Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Configurations")

        // Загружаем список сборок
        loadConfigurations()

        // Настройка поиска
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                val query = charSequence.toString().trim()
                if (query.isNotEmpty()) {
                    filterConfigurations(query)
                } else {
                    resetFilter()
                }
            }

            override fun afterTextChanged(editable: Editable?) {}
        })

        // Обработка клика на элемент списка
        binding.listView.setOnItemClickListener { _, _, position, _ ->
            val selectedConfig = configList[position]
            navigateToConfigDetail(selectedConfig)
        }

        binding.createConfigButton.setOnClickListener {
            val intent = Intent(this@MyConfigActivity, UploadConfigActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadConfigurations() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    configList.clear()
                    fullConfigList.clear()
                    for (childSnapshot in snapshot.children) {
                        val configName = childSnapshot.key.toString()
                        configList.add(configName)
                        fullConfigList.add(configName)
                    }
                    adapter.notifyDataSetChanged()
                    showListView()
                } else {
                    showNoResultsMessage()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MyConfigActivity, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterConfigurations(query: String) {
        val filteredList = fullConfigList.filter { it.contains(query, ignoreCase = true) }
        if (filteredList.isNotEmpty()) {
            configList.clear()
            configList.addAll(filteredList)
            adapter.notifyDataSetChanged()
            showListView()
        } else {
            showNoResultsMessage()
        }
    }

    private fun resetFilter() {
        configList.clear()
        configList.addAll(fullConfigList)
        adapter.notifyDataSetChanged()
        showListView()
    }

    private fun showListView() {
        binding.listView.visibility = android.view.View.VISIBLE
        binding.noResultsTextView.visibility = android.view.View.GONE
    }

    private fun showNoResultsMessage() {
        binding.listView.visibility = android.view.View.GONE
        binding.noResultsTextView.visibility = android.view.View.VISIBLE
    }

    private fun navigateToConfigDetail(configName: String) {
        val intent = Intent(this@MyConfigActivity, MyConfigDetailActivity::class.java)
        intent.putExtra("CONFIG_NAME", configName)
        startActivity(intent)
    }
}
