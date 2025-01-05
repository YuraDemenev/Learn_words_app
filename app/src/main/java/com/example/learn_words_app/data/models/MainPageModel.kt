package com.example.learn_words_app.data.models

import android.content.Context
import android.util.Log
import com.example.learn_words_app.data.dataBase.Levels
import com.example.learn_words_app.data.dataBase.MainDB
import com.example.learn_words_app.data.dataBase.Words
import com.example.learn_words_app.data.interfaces.MainPageContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class MainPageModel : MainPageContract.Model {
    override fun upDB(c: Context, db: MainDB) {
        //Получаем названия всех файлов в папке
        val files = c.assets.list("develop_db") ?: arrayOf()
//        //Объявляем переменную word
//        lateinit var word: Words

        //HashMap для работы с Levels, чтобы id уровня было по порядку
        val levelsMap = ConcurrentHashMap<String, Int>()
        files.forEachIndexed { index, fileName ->
            levelsMap[fileName] = index + 1
        }

        //Создаем Scope для запуска корутин
        val myScope = CoroutineScope(Dispatchers.IO)

        //Запускаем асинхронное заполнение таблицы Levels
        myScope.launch {
            val deferredList = files.map { fileName ->
                async(Dispatchers.IO) {
                    //Получаем из Map id по названию файла
                    val id = levelsMap.getValue(fileName)
                    val level = Levels(id, fileName)
                    db.getDao().insertLevel(level)
                }
            }
            deferredList.awaitAll()
        }

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

                //Запускаем корутину в которой проходим по строкам и добавляем их в БД
                myScope.launch {
                    val deferredList = strings.map { line ->
                        //Объявляем переменную word
                        lateinit var word: Words
                        
                        val wordsInString = line.split(";")
                        if (wordsInString.size > 2) {
                            Log.e("Two ';'", word.englishWord)
                        } else {
                            //GetValue кидает исключение если ключа нет
                            val levelId = levelsMap.getValue(fileName)
                            word = Words(
                                null,
                                wordsInString[0],
                                wordsInString[1],
                                0,
                                false,
                                "",
                                levelId
                            )
                        }

                        async(Dispatchers.IO) {
                            val copyWord = word.copy()
                            db.getDao().insertWord(copyWord)
                        }
                    }
                    deferredList.awaitAll()
                }

                // Проходим всем строкам
//                strings.forEach { line ->
//                    //Разделяем строку по ";"
//                    val wordsInString = line.split(";")
//                    if (wordsInString.size > 2) {
//                        Log.e("Two ';'", word.englishWord)
//                    } else {
//                        //GetValue кидает исключение если ключа нет
//                        val levelId = levelsMap.getValue(fileName)
//                        word =
//                            Words(null, wordsInString[0], wordsInString[1], 0, false, "", levelId)
//                    }
//                    // Вызываем асинхронную функцию
//                    CoroutineScope(Dispatchers.IO).launch {
//                        db.getDao().insertWord(word)
//                    }
//                }
            } catch (ex: Exception) {
                Log.e("Text read", ex.cause.toString())
            }

        }
        Log.i("Created database", "Created database")
    }

    override fun downDB() {
        TODO("Not yet implemented")
    }
}