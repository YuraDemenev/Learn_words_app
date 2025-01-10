package com.example.learn_words_app.data.models

import android.content.Context
import android.util.Log
import com.app.proto.LevelsProto
import com.example.learn_words_app.data.dataBase.Levels
import com.example.learn_words_app.data.dataBase.MainDB
import com.example.learn_words_app.data.dataBase.Words
import com.example.learn_words_app.data.dataBase.WordsLevels
import com.example.learn_words_app.data.interfaces.MainPageContract
import com.example.learn_words_app.data.proto.convertLevelsProtoToLevels
import com.example.learn_words_app.data.proto.userParamsDataStore
import com.example.learn_words_app.data.userData.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class MainPageModel : MainPageContract.Model {
    override suspend fun checkUserData(context: Context, db: MainDB) {
        //Получаем данные из хранилища Proto DataStore
        val userFlow: Flow<User> = context.userParamsDataStore.data.map { userProto ->
            User(
                userProto.userId,
                userProto.curRepeatDays,
                userProto.maxRepeatDays,
                userProto.countFullLearnedWords,
                userProto.countLearningWords,
                userProto.countKnewWords,
                userProto.listOfLevelsList.map { levelsProto ->
                    convertLevelsProtoToLevels(levelsProto)
                }.toMutableList(),
                userProto.checkBritishVariables
            )
        }

        //Проверяем есть ли пользователь в хранилище или нет
        val checkUser = userFlow.first()
        //Если пользователя нет
        if (checkUser.userId.contains("")) {
            //Получаем список id и name из таблицы tables
            val baseLevels = db.getDao().getBaseLevels("A1.txt", "A2.txt", "B1.txt")
            //Создаем список для LevelsProto
            val listOfLevelsBuilders = mutableListOf<LevelsProto>()

            //Проходим по list Levels и заполняем List LevelsProto
            baseLevels.forEach { level ->
                if (level.id != null) {
                    listOfLevelsBuilders.add(
                        LevelsProto.newBuilder().setId(level.id).setName(level.name).build()
                    )
                } else {
                    Log.e("Check user data. get levels from db", "level id is null")
                    return
                }
            }

            try {
                //Обновляем данные в Proto DataStore
                context.userParamsDataStore.updateData { userPorto ->
                    userPorto.toBuilder().clearListOfLevels()
                        .setUserId("test")
                        .setCurRepeatDays(0)
                        .setMaxRepeatDays(0)
                        .setCountFullLearnedWords(0)
                        .setCountLearningWords(0)
                        .setCountKnewWords(0)
                        .addAllListOfLevels(listOfLevelsBuilders)
                        .setCheckBritishVariables(false)
                        .build()
                }
            } catch (e: Exception) {
                Log.e(
                    "Check user data. Update Proto DataStore",
                    "can`t update, err: $e"
                )
                return
            }

        }
        Log.i("Check user data", "Success checked user data")
    }

    override suspend fun clearUserData(context: Context) {
        try {
            //Обновляем данные в Proto DataStore
            context.userParamsDataStore.updateData { userPorto ->
                userPorto.toBuilder().clearListOfLevels()
                    .setUserId("")
                    .setCurRepeatDays(0)
                    .setMaxRepeatDays(0)
                    .setCountFullLearnedWords(0)
                    .setCountLearningWords(0)
                    .setCountKnewWords(0)
                    .clearListOfLevels()
                    .setCheckBritishVariables(false)
                    .build()
            }
        } catch (e: Exception) {
            Log.e(
                "Clear user data",
                "can`t clear, err: $e"
            )
            return
        }
        Log.i("Cleared user data", "Success cleared user data")
    }

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
                myScope.launch {
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
                            var britishVariable = ""

                            //Проверяем содержит ли слово UK
                            val check = wordsInString[0].contains("UK", ignoreCase = true)
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
                            //Добавляем новое слово в таблицу word
                            val idLong = db.getDao().insertWord(word)
                            val id = idLong.toInt()
                            //Получаем id levels
                            val levelId = levelsMap.getValue(fileName)
                            //Добавляем wordsLevels в таблицу wordsLevels
                            val wordLevels = WordsLevels(id, levelId, 0)
                            db.getDao().insertWordsLevel(wordLevels)

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
        Log.i("Dropped database", "Dropped database")
    }
}