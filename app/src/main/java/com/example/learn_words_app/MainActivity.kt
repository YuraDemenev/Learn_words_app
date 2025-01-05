package com.example.learn_words_app

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.learn_words_app.dataBase.Levels
import com.example.learn_words_app.dataBase.MainDB
import com.example.learn_words_app.dataBase.Words
import com.example.learn_words_app.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        //Получаем/создаем БД
        val db = MainDB.getDB(this)
        val scope = CoroutineScope(Dispatchers.Default)

        //Auto generated
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Only for DEV
        //Listener нажатия на текст UpDB
        binding.upDataBase.setOnClickListener {
            //Получаем названия всех файлов в папке
            val files = assets.list("develop_db") ?: arrayOf()
            //Объявляем переменную word
            lateinit var word: Words
            val levelsHashMap = HashMap<Int, String>()

            // Список корутинных задач
            val jobs = mutableListOf<Job>()

            scope.launch {
                var i = 1
                files.forEach { filename ->
                    val job = launch {
                        val level = Levels(null, filename)
                        db.getDao().insertLevel(level)
                    }
                    jobs.add(job)
                    levelsHashMap[i] = filename
                    i += 1
                }
                jobs.forEach { it.join() }
            }


            //Проходимся по всем файлам
            files.forEach { fileName ->
                try {
                    // Считываем файлы из папки
                    val myInputStream = assets.open("develop_db/$fileName")
                    val size: Int = myInputStream.available()
                    val buffer = ByteArray(size)
                    myInputStream.read(buffer)
                    val myOutput = String(buffer)

                    // Разделяем файл по строкам
                    val strings = myOutput.split("\r\n")

                    // Проходим всем строкам
                    strings.forEach { line ->
                        //Разделяем строку по ";"
                        val wordsInString = line.split(";")
                        if (wordsInString.size > 2) {
                            Log.e("Two ';'", word.englishWord)
                        } else {
                            word = Words(null, wordsInString[0], wordsInString[1], 0, false, "", 1)
                        }
                        // Вызываем асинхронную функцию
                        CoroutineScope(Dispatchers.IO).launch {
                            db.getDao().insertWord(word)
                        }
                    }
                } catch (ex: Exception) {
                    Log.e("Text read", ex.cause.toString() + "$word")
                }

            }
            Log.i("Created database", "Created database")
        }
        //Listener нажатия на текст Down DB
        binding.downDataBase.setOnClickListener {
            // Вызываем асинхронную функцию
            CoroutineScope(Dispatchers.IO).launch {
                db.getDao().deleteDataFromWordsTable()
                db.getDao().deleteDataFromLevelsTable()
                db.getDao().deletePrimaryKeys()
            }
        }
    }
}