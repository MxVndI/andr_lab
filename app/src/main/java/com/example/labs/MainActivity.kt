package com.example.labs

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var buttonStart: Button
    private lateinit var buttonNew: Button
    private lateinit var buttonBack: Button
    private lateinit var buttonCheck: Button
    private lateinit var buttonHint: Button
    private lateinit var buttonClear: Button
    private lateinit var buttonFillWord: Button
    private lateinit var buttonSave: Button
    private lateinit var buttonHorizontalClues: Button
    private lateinit var buttonVerticalClues: Button
    private lateinit var spinnerDifficulty: Spinner
    private lateinit var crosswordView: TextView
    private lateinit var cluesContent: TextView
    private lateinit var statusText: TextView
    private lateinit var timerText: TextView
    private lateinit var progressText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupDifficultySpinner()
        setupButtonListeners()
        setupCluesButtons()
    }

    private fun initializeViews() {
        buttonStart = findViewById(R.id.buttonStart)
        buttonNew = findViewById(R.id.buttonNew)
        buttonBack = findViewById(R.id.buttonBack)
        buttonCheck = findViewById(R.id.buttonCheck)

        buttonHint = findViewById(R.id.buttonHint)
        buttonClear = findViewById(R.id.buttonClear)
        buttonFillWord = findViewById(R.id.buttonFillWord)
        buttonSave = findViewById(R.id.buttonSave)

        buttonHorizontalClues = findViewById(R.id.buttonHorizontalClues)
        buttonVerticalClues = findViewById(R.id.buttonVerticalClues)

        spinnerDifficulty = findViewById(R.id.spinnerDifficulty)
        crosswordView = findViewById(R.id.crosswordView)
        cluesContent = findViewById(R.id.cluesContent)
        statusText = findViewById(R.id.statusText)
        timerText = findViewById(R.id.timerText)
        progressText = findViewById(R.id.progressText)
    }

    private fun setupDifficultySpinner() {
        val difficulties = arrayOf(
            getString(R.string.difficulty_easy),
            getString(R.string.difficulty_medium),
            getString(R.string.difficulty_hard),
            getString(R.string.difficulty_custom)
        )

        val adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, difficulties)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDifficulty.adapter = adapter
    }

    private fun setupButtonListeners() {
        buttonStart.setOnClickListener {
            statusText.text = getString(R.string.status_playing)
            crosswordView.text = "Игра началась!\nНажмите 'Новый' для генерации кроссворда"
        }

        buttonNew.setOnClickListener {
            crosswordView.text = "Генерируется новый кроссворд...\n\nСетка будет здесь"
            statusText.text = getString(R.string.status_ready)
            progressText.text = "Слов: 5/10"
        }

        buttonBack.setOnClickListener {
            crosswordView.text = getString(R.string.crossword_area)
            statusText.text = getString(R.string.status_ready)
            progressText.text = "Слов: 0/0"
        }

        buttonCheck.setOnClickListener {
            crosswordView.text = "Проверка кроссворда...\n\nРезультат будет здесь"
        }

        buttonHint.setOnClickListener {
            cluesContent.text = "Подсказка: Выберите направление подсказок для получения помощи"
        }

        buttonClear.setOnClickListener {
            crosswordView.text = getString(R.string.crossword_area)
            cluesContent.text = "Выберите направление подсказок"
        }

        buttonFillWord.setOnClickListener {
            crosswordView.text = "Автозаполнение слова...\n\nФункция в разработке"
        }

        buttonSave.setOnClickListener {
            statusText.text = "Кроссворд сохранен"
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