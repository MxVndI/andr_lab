package com.example.labs

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : AppCompatActivity() {

    private lateinit var buttonStart: Button
    private lateinit var buttonNew: Button
    private lateinit var buttonCheck: Button
    private lateinit var buttonClear: Button
    private lateinit var buttonSave: Button
    private lateinit var buttonHorizontalClues: Button
    private lateinit var buttonVerticalClues: Button
    private lateinit var spinnerDifficulty: Spinner
    private lateinit var crosswordView: TextView
    private lateinit var cluesContent: TextView

    private val sharedPreferences by lazy {
        getSharedPreferences("user_prefs", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // Check if user is logged in
        if (!isUserLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        initializeViews()
        setupDifficultySpinner()
        setupButtonListeners()
        setupCluesButtons()
    }

    private fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("is_logged_in", false)
    }

    private fun initializeViews() {
        buttonStart = findViewById(R.id.buttonStart)
        buttonNew = findViewById(R.id.buttonNew)
        buttonCheck = findViewById(R.id.buttonCheck)
        buttonClear = findViewById(R.id.buttonClear)
        buttonSave = findViewById(R.id.buttonSave)
        buttonHorizontalClues = findViewById(R.id.buttonHorizontalClues)
        buttonVerticalClues = findViewById(R.id.buttonVerticalClues)
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty)
        crosswordView = findViewById(R.id.crosswordView)
        cluesContent = findViewById(R.id.cluesContent)
    }

    private fun setupDifficultySpinner() {
        val difficulties = arrayOf(
            "Легкий",
            "Средний",
            "Сложный",
            "Пользовательский"
        )

        val adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, difficulties)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDifficulty.adapter = adapter
    }

    private fun setupButtonListeners() {
        buttonStart.setOnClickListener {
            crosswordView.text = "Игра началась!\nНажмите 'Новый' для генерации кроссворда"
        }

        buttonNew.setOnClickListener {
            crosswordView.text = "Генерируется новый кроссворд...\n\nСетка будет здесь"
        }

        buttonCheck.setOnClickListener {
            crosswordView.text = "Проверка кроссворда...\n\nРезультат будет здесь"
        }

        buttonClear.setOnClickListener {
            crosswordView.text = "Область для кроссворда"
            cluesContent.text = "Выберите направление подсказок"
        }

        buttonSave.setOnClickListener {
            Toast.makeText(this, "Кроссворд сохранен", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCluesButtons() {
        buttonHorizontalClues.setOnClickListener {
            cluesContent.text = "ПОДСКАЗКИ ПО ГОРИЗОНТАЛИ:\n\n" +
                    "1. Столица России (5 букв)\n" +
                    "2. Домашнее животное (4 буквы)\n" +
                    "3. Время года (3 буквы)\n" +
                    "4. Напиток (3 буквы)\n" +
                    "5. Цветок (4 буквы)"
        }

        buttonVerticalClues.setOnClickListener {
            cluesContent.text = "ПОДСКАЗКИ ПО ВЕРТИКАЛИ:\n\n" +
                    "1. Планета (4 буквы)\n" +
                    "2. Фрукт (5 букв)\n" +
                    "3. Транспорт (5 букв)\n" +
                    "4. Животное (3 буквы)\n" +
                    "5. Цвет (5 букв)"
        }
    }
}