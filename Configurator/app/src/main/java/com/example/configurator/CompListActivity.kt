package com.example.configurator

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.configurator.databinding.ActivityCompListBinding

class CompListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCompListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompListBinding.inflate(layoutInflater)
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

        // Устанавливаем обработчики кликов для каждой категории
        binding.processorCategory.setOnClickListener {
            navigateToListActivity("Процессор")
        }

        binding.graphicsCardCategory.setOnClickListener {
            navigateToListActivity("Видеокарта")
        }

        binding.ramCategory.setOnClickListener {
            navigateToListActivity("Оперативная память")
        }

        binding.motherboardCategory.setOnClickListener {
            navigateToListActivity("Материнская плата")
        }

        binding.storageCategory.setOnClickListener {
            navigateToListActivity("Диск")
        }

        binding.coolerCategory.setOnClickListener {
            navigateToListActivity("Кулер")
        }

        binding.towerCategory.setOnClickListener {
            navigateToListActivity("Корпус")
        }
    }

    private fun navigateToListActivity(categoryName: String) {
        val intent = Intent(this@CompListActivity, ListActivity::class.java)
        intent.putExtra("CATEGORY_NAME", categoryName) // Здесь добавляются корректные названия
        startActivity(intent)
    }
}
