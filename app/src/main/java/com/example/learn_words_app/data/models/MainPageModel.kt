package com.example.learn_words_app.data.models

import android.content.Context
import android.util.Log
import com.example.learn_words_app.data.dataBase.MainDB
import com.example.learn_words_app.data.dataBase.Words
import com.example.learn_words_app.data.interfaces.MainPageContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainPageModel : MainPageContract.Model {
    override fun upDB(c: Context, db: MainDB) {
        //Получаем названия всех файлов в папке
        val files = c.assets.list("develop_db") ?: arrayOf()
        //Объявляем переменную word
        lateinit var word: Words
        val levelsHashMap = HashMap<Int, String>()

        // Список корутинных задач
        val jobs = mutableListOf<Job>()

//        scope.launch {
//            var i = 1
//            files.forEach { filename ->
//                val job = launch {
//                    val level = Levels(null, filename)
//                    db.getDao().insertLevel(level)
//                }
//                jobs.add(job)
//                levelsHashMap[i] = filename
//                i += 1
//            }
//            jobs.forEach { it.join() }
//        }


        //Проходимся по всем файлам
        files.forEach { fileName ->
            try {
                // Считываем файлы из папки
                val myInputStream = c.assets.open("develop_db/$fileName")
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

    override fun downDB() {
        TODO("Not yet implemented")
    }
}