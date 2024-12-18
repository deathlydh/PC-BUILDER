package com.example.configurator

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.configurator.databinding.ActivityListBinding
import com.google.firebase.database.*

class ListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var componentList: MutableList<String>
    private lateinit var fullComponentList: MutableList<String> // Сохраняем полный список компонентов
    private lateinit var adapter: ArrayAdapter<String>

    private var categoryName: String = ""
    private lateinit var searchEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
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

        // Получаем название категории из Intent
        categoryName = intent.getStringExtra("CATEGORY_NAME") ?: ""
        binding.categoryTitle.text = categoryName

        // Инициализация списка и адаптера
        componentList = mutableListOf()
        fullComponentList = mutableListOf() // Инициализация для хранения полного списка
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, componentList)
        binding.listView.adapter = adapter

        // Получаем ссылку на Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Components/$categoryName")

        // Загружаем данные
        loadComponents()

        // Инициализация компонента поиска
        searchEditText = findViewById(R.id.searchEditText)

        // Добавляем TextWatcher для поиска
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                val query = charSequence.toString().trim()
                if (query.isNotEmpty()) {
                    filterList(query)
                } else {
                    resetFilter() // Сбрасываем фильтрацию, если поле пустое
                }
            }

            override fun afterTextChanged(editable: Editable?) {}
        })

        // Обработка кликов на элементы списка
        binding.listView.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = componentList[position]
            navigateToDetailActivity(selectedItem)
        }
    }

    private fun loadComponents() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    componentList.clear()
                    fullComponentList.clear() // Очищаем полный список при загрузке
                    for (childSnapshot in snapshot.children) {
                        val componentName = childSnapshot.key.toString()
                        componentList.add(componentName)
                        fullComponentList.add(componentName) // Добавляем в полный список
                    }
                    adapter.notifyDataSetChanged()
                    showListView() // Показываем ListView после загрузки данных
                } else {
                    Toast.makeText(this@ListActivity, "Нет данных для этой категории", Toast.LENGTH_SHORT).show()
                    showNoResultsMessage() // Показать сообщение, если данных нет
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ListActivity, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Функция для фильтрации списка на основе введенного текста
    private fun filterList(query: String) {
        val filteredList = fullComponentList.filter { it.contains(query, ignoreCase = true) }
        if (filteredList.isNotEmpty()) {
            componentList.clear()
            componentList.addAll(filteredList)
            adapter.notifyDataSetChanged()
            showListView() // Показываем ListView, если есть результаты
        } else {
            showNoResultsMessage() // Показываем "Не найдено", если нет результатов
        }
    }

    // Сброс фильтрации, когда строка поиска очищена
    private fun resetFilter() {
        componentList.clear()
        componentList.addAll(fullComponentList) // Восстанавливаем весь список
        adapter.notifyDataSetChanged()
        showListView() // Показываем ListView, если список не пустой
    }

    // Показать ListView и скрыть "Не найдено"
    private fun showListView() {
        binding.listView.visibility = android.view.View.VISIBLE
        binding.noResultsTextView.visibility = android.view.View.GONE
    }

    // Показать сообщение "Не найдено" и скрыть ListView
    private fun showNoResultsMessage() {
        binding.listView.visibility = android.view.View.GONE
        binding.noResultsTextView.visibility = android.view.View.VISIBLE
    }

    // Функция для перехода к ListDetailActivity с передачей данных
    private fun navigateToDetailActivity(componentName: String) {
        val intent = Intent(this@ListActivity, ListDetailActivity::class.java)
        intent.putExtra("CATEGORY_NAME", categoryName)  // Передача категории
        intent.putExtra("ITEM_NAME", componentName)     // Передача названия компонента
        startActivity(intent)
    }
}
