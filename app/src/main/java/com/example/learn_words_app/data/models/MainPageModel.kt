package com.example.learn_words_app.data.models

import android.content.Context
import android.util.Log
import com.app.proto.LevelsProto
import com.example.learn_words_app.data.additionalData.FlowLevelsModel
import com.example.learn_words_app.data.additionalData.LevelsCardData
import com.example.learn_words_app.data.additionalData.User
import com.example.learn_words_app.data.dataBase.Levels
import com.example.learn_words_app.data.dataBase.MainDB
import com.example.learn_words_app.data.dataBase.Words
import com.example.learn_words_app.data.dataBase.WordsLevels
import com.example.learn_words_app.data.interfaces.MainPageContract
import com.example.learn_words_app.data.proto.convertLevelsProtoToLevels
import com.example.learn_words_app.data.proto.userParamsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class MainPageModel : MainPageContract.Model {
    override suspend fun getLevelsCardData(
        context: Context,
        db: MainDB
    ): MutableList<LevelsCardData> {
        //Создаем Scope для запуска корутин
        val myScope = CoroutineScope(Dispatchers.IO)
        //Массив для возвращения
        val listOfLevels = mutableListOf<LevelsCardData>()

        //Получаем уровни для данных в карточках на странице выбора уровней
        myScope.launch {
            val levelsFromDB = db.getDao().getAllLevels()

            //Проходим по всем уровням
            levelsFromDB.forEach { level ->
                if (level.id != null) {
                    val countWords = db.getDao().getCountWordsByLevelId(level.id)
                    //Вычисление процентов выученных слов
                    val percentage = countWords / 100 * level.countLearnedWords
                    //Создание LevelCardData
                    val levelCarData =
                        LevelsCardData(level.name, countWords, percentage)
                    listOfLevels.add(levelCarData)
                }
            }
        }.join()

        return listOfLevels
    }

    override suspend fun checkUserData(
        context: Context,
        db: MainDB,
        flowLevelsModel: FlowLevelsModel
    ) {

        val userFlow = getUserProtoData(context)
        //Проверяем есть ли пользователь в хранилище или нет
        var checkUser = userFlow.first()
        //Если пользователя нет
        if (checkUser.userId == "") {
            //Получаем список id и name из таблицы tables
            val baseLevels = db.getDao().getBaseLevels("A1", "A2", "B1")
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
                val user = User(userId = "test", countLearningWords = 10)
                //Обновляем данные в Proto DataStore
//                context.userParamsDataStore.updateData { userPorto ->
//                    userPorto.toBuilder().clearListOfLevels()
//                        .setUserId("test")
//                        .setCurRepeatDays(0)
//                        .setMaxRepeatDays(0)
//                        .setCountFullLearnedWords(0)
//                        .setCountLearningWords(10)
//                        .setCountKnewWords(0)
//                        .addAllListOfLevels(listOfLevelsBuilders)
//                        .setCheckBritishVariables(false)
//                        .build()
//                }
                setUserProtoData(context, user, listOfLevelsBuilders)
            } catch (e: Exception) {
                Log.e(
                    "Check user data. Update Proto DataStore",
                    "can`t update, err: $e"
                )
                return
            }
            //Получаем user для того чтобы ниже получить данные
            checkUser = userFlow.first()

        }
        //Проверка что flow levels model не null
        if (flowLevelsModel.data.value == null) {
            Log.e(
                "flow levels model is null",
                "Main page model, checkUserData, flow levels model is null"
            )
        }

        //Добавляем в flow levels model уровни
        val listOfLevels = checkUser.listOfLevels
        listOfLevels.forEach { locLevel ->
            if (locLevel.id != null) {
                flowLevelsModel.data.value?.add(locLevel.name)
            } else {
                Log.e(
                    "level id is null",
                    "Main page model, checkUserData, level id is null"
                )
            }
        }

        Log.i("Check user data", "Success checked user data")
    }

    //Обновляем Proto данные пользователя
    override suspend fun updateProtoData(context: Context, flowLevelsModel: FlowLevelsModel) {
        //Для добавления в Proto data levelsList
        val listOfLevelsBuilders = mutableListOf<LevelsProto>()
        //Проверка что flow levels model не null
        if (flowLevelsModel.data.value == null) {
            Log.e(
                "flow levels model is null",
                "Main page model, updateProtoData, flow levels model is null"
            )
        }

        //Получаем уровни из Flow level
        val curLevels = flowLevelsModel.data.value?.toList()
        if (curLevels == null) {
            Log.e(
                "current levels are null",
                "Main page model, updateProtoData, current levels are null"
            )
            throw Exception()
        } else {
            //Проходим по list Levels и заполняем List LevelsProto
            curLevels.forEach { level ->
                listOfLevelsBuilders.add(
                    LevelsProto.newBuilder().setId(0).setName(level).build()
                )
            }
        }
        //Получаем прогресс пользователя
        val userFlow = getUserProtoData(context)
        val user = userFlow.first()

        //Обновляем прогресс пользователя
        setUserProtoData(context, user, listOfLevelsBuilders)
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

        var mapIndex = 1
        //HashMap для работы с Levels, чтобы id уровня было по порядку
        val levelsMap = ConcurrentHashMap<String, Int>()
        files.forEach { fileName ->
            val splitNames = fileName.split(".")
            levelsMap[splitNames[0]] = mapIndex
            mapIndex++
        }
        levelsMap["your_words"] = mapIndex


        //Создаем Scope для запуска корутин
        val myScope = CoroutineScope(Dispatchers.IO)

        //Запускаем асинхронное заполнение таблицы Levels
        myScope.launch {
            val deferredList = levelsMap.map { mapValue ->
                async(Dispatchers.IO) {
                    val level = Levels(mapValue.value, mapValue.key, 0)
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
                        val nameInMap = fileName.split(".")[0]

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
                            val levelId = levelsMap.getValue(nameInMap)
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
                            val levelId = levelsMap.getValue(nameInMap)
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

    private fun getUserProtoData(context: Context): Flow<User> {
        //Получаем данные из хранилища Proto DataStore
        val userFlow: Flow<User> = context.userParamsDataStore.data.map { userProto ->
            User(
                userProto.userId,
                userProto.curRepeatDays,
                userProto.maxRepeatDays,
                userProto.countFullLearnedWords,
                userProto.countLearningWords,
                userProto.countLearnedWordsToday,
                userProto.countKnewWords,
                userProto.listOfLevelsList.map { levelsProto ->
                    convertLevelsProtoToLevels(levelsProto)
                }.toMutableList(),
                userProto.checkBritishVariables,
                Instant.ofEpochSecond(
                    userProto.lastTimeLearnedWords.seconds,
                    userProto.lastTimeLearnedWords.nanos.toLong()
                )

            )
        }
        return userFlow
    }

    private suspend fun setUserProtoData(
        context: Context,
        user: User,
        listOfLevelsBuilders: MutableList<LevelsProto>
    ) {
        //Обновляем прогресс пользователя
        context.userParamsDataStore.updateData { userPorto ->
            userPorto.toBuilder().clearListOfLevels()
                .setUserId(user.userId)
                .setCurRepeatDays(user.curRepeatDays)
                .setMaxRepeatDays(user.maxRepeatDays)
                .setCountFullLearnedWords(user.countFullLearnedWords)
                .setCountLearningWords(user.countLearningWords)
                .setCountKnewWords(user.countKnewWords)
                .addAllListOfLevels(listOfLevelsBuilders)
                .setCheckBritishVariables(false)
                .build()
        }
    }

//    private fun checkIsDataNull(data: Any, tag: String, message: String): Boolean {
//        if (data == null) {
//            Log.e(tag, message)
//            return false
//        }
//        return true
//    }
}