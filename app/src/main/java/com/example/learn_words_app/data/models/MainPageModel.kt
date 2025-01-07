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
    override suspend fun upDB(c: Context, db: MainDB) {
        //Получаем названия всех файлов в папке
        val files = c.assets.list("develop_db") ?: arrayOf()

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
        }.join()

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

                //Для того чтобы отловить exception в корутине
//                val exceptionHandler = CoroutineExceptionHandler { _, exception ->
//                    Log.e(
//                        "try to handle word and add in db",
//                        "in file:$fileName exception: $exception"
//                    )
//                }

                //Запускаем корутину в которой проходим по строкам и добавляем их в БД
                myScope.launch() {
                    val deferredList = strings.map { line ->
                        //Объявляем переменную word
                        lateinit var word: Words

                        //Делим строку по ';'
                        val wordsInString = line.split(";").toMutableList()
                        if (wordsInString.size > 2) {
                            Log.e("Two ';'", word.englishWord)
                        } else {
                            //Проверка, что в русском переводе русские буквы
                            wordsInString[1].forEach { symbol ->
                                val number = symbol.code
                                if (number < 127 && ((number in 65..90) || (number in 97..122)) && wordsInString[0] != "dot") {
                                    val errWord = wordsInString[1]
                                    Log.e(
                                        "russian word has an english letters",
                                        "word: $errWord file: $fileName"
                                    )
                                }
                            }

                            //GetValue кидает исключение если ключа нет
                            val levelId = levelsMap.getValue(fileName)
                            //Для проверки имеет ли слово британский вариант
                            var check = false
                            var britishVariable = ""

                            //Проверяем содержит ли слово UK
                            check = wordsInString[0].contains("UK", ignoreCase = true)
                            if (check) {
                                //Получаем британский вариант слова
                                var index = wordsInString[0].indexOf("UK")
                                index += 3
                                while (wordsInString[0][index] != ')') {
                                    britishVariable += wordsInString[0][index]
                                    index++
                                }

                                //Меняем английское слово
                                index = wordsInString[0].indexOf("UK")
                                index -= 2
                                if (wordsInString[0][index] != ' ') {
                                    index++
                                }
                                var string = wordsInString[0]
                                string = string.substring(0, index)
                                wordsInString[0] = string
                                println()
                            }

                            //Создаем слово
                            word = Words(
                                null,
                                wordsInString[0],
                                wordsInString[1],
                                0,
                                britishVariable,
                                levelId
                            )
                        }
                        //Upsert request to DB
                        async(Dispatchers.IO) {
                            db.getDao().insertWord(word)
                        }
                    }
                    deferredList.awaitAll()
                }.join()

            } catch (ex: Exception) {
                Log.e("Read from assets files in develop_bd", ex.cause.toString())
            }

        }
        Log.i("Created database", "Created database")
    }

    override fun downDB(db: MainDB) {
        CoroutineScope(Dispatchers.IO).launch {
            db.getDao().deleteDataFromWordsTable()
            db.getDao().deleteDataFromLevelsTable()
            db.getDao().deletePrimaryKeys()
        }
    }
}