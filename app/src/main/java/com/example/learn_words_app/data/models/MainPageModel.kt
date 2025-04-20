package com.example.learn_words_app.data.models

import android.content.Context
import android.util.Log
import com.app.proto.LevelsProto
import com.example.learn_words_app.data.additionalData.FlowLevelsModel
import com.example.learn_words_app.data.additionalData.LevelsCardData
import com.example.learn_words_app.data.additionalData.User
import com.example.learn_words_app.data.additionalData.convertDateToTimestamp
import com.example.learn_words_app.data.dataBase.Levels
import com.example.learn_words_app.data.dataBase.MainDB
import com.example.learn_words_app.data.dataBase.Words
import com.example.learn_words_app.data.dataBase.WordsLevels
import com.example.learn_words_app.data.interfaces.MainPageContract
import com.example.learn_words_app.data.interfaces.WordCallback
import com.example.learn_words_app.data.proto.convertLevelsProtoToLevels
import com.example.learn_words_app.data.proto.convertLevelsToProtoLevels
import com.example.learn_words_app.data.proto.userParamsDataStore
import com.google.protobuf.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class MainPageModel : MainPageContract.Model {
    override suspend fun getWordsForRepeat(db: MainDB): Array<Int> {
        //TODO проверить запрос, что он учитывает stage
        val date = Date.from(Instant.now())
        val listOfWords = db.getDao().getWordsForRepeat(date?.time?.toLong())

        return listOfWords
    }

    override suspend fun getOneWordForLearn(
        context: Context,
        db: MainDB,
        flowLevelsModel: FlowLevelsModel,
        wordId: Int?,
        callback: WordCallback
    ) {
        val myScope = CoroutineScope(Dispatchers.IO)
        lateinit var listOfLevels: List<Levels>

        if (wordId != null) {
            //Меняем в words_levels stage на 6, так как пользователь уже слово знает
            myScope.launch {
                db.getDao().updateWordLevelsStage(wordId, 6, Date.from(Instant.now()))
            }.join()
        } else {
            Log.e("Main Page Model", "getOneWordForLearn, wordId is null")
            throw Exception()
        }

        //Получаем уровни
        myScope.launch {
            val hashSet = flowLevelsModel.data.value
            if (hashSet != null) {
                if (hashSet.size == 0) {
                    Log.e("Main page model get words for learn.", "hash set size is zero")
                }
                listOfLevels = db.getDao().getLevelsByNamesMultipleQueries(hashSet)
            }
        }.join()

        //Получаем из list levels ids чтобы их использовать в следующем запросе
        val arrayLevelsIds: Array<Int> = Array(listOfLevels.size) { 0 }
        listOfLevels.forEachIndexed { index, level ->
            if (level.id != null) {
                arrayLevelsIds[index] = level.id
            }
        }

        //Получаем случайное слово
        lateinit var word: Words
        myScope.launch {
            word = db.getDao().getWordByLevelsIdsMultiplyQueries(arrayLevelsIds)

        }.join()

        callback.onWordReceived(word)
    }

    override suspend fun getUser(context: Context, db: MainDB): User {
        val userFlow = getUserProtoData(context, db)
        return userFlow.first()
    }

    override suspend fun getWordsForLearn(
        context: Context,
        db: MainDB,
        flowLevelsModel: FlowLevelsModel,
        countLearningWords: Int
    ): Pair<MutableList<Words>, HashMap<Int, String>> {
        val myScope = CoroutineScope(Dispatchers.IO)
        lateinit var listOfLevels: List<Levels>

        //Получаем уровни
        myScope.launch {
            val hashSet = flowLevelsModel.data.value
            if (hashSet != null) {
                if (hashSet.size == 0) {
                    Log.e("Main page model get words for learn.", "hash set size is zero")
                    throw Exception()
                }
                listOfLevels = db.getDao().getLevelsByNamesMultipleQueries(hashSet)
            } else {
                Log.e("Main page model", "getWordsForLearn, hash set is null")
            }
        }.join()

        //Получаем из list levels ids чтобы их использовать в следующем запросе
        val arrayLevelsIds: Array<Int> = Array(listOfLevels.size) { 0 }
        listOfLevels.forEachIndexed { index, level ->
            if (level.id != null) {
                arrayLevelsIds[index] = level.id
            }
        }

        //Получаем случайный список слов с levels_id
        lateinit var words: List<Words>
        myScope.launch {
            words =
                db.getDao().getWordsByLevelsIdsMultiplyQueries(arrayLevelsIds, countLearningWords)
        }.join()

        //Создаем hash map, чтобы хранить названия уровней по ids
        val hashMap = HashMap<Int, String>()
        listOfLevels.forEach { element ->
            if (element.id != null) {
                hashMap[element.id] = element.name
            } else {
                Log.e(
                    "Main page contract",
                    "getWordsForLearn, listOfLevels has element with null id"
                )
                throw Exception()
            }
        }
        val pair = Pair(words.toMutableList(), hashMap)
        return pair
    }

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
        db: MainDB

    ): HashSet<String> {
        val userFlow = getUserProtoData(context, db)
        //Проверяем есть ли пользователь в хранилище или нет
        var checkUser = userFlow.first()
        val flowLevels: HashSet<String> = hashSetOf()

        //Если пользователя нет
        if (checkUser.userId == "") {
            //Получаем список id и name из таблицы tables
            val baseLevels = db.getDao().getBaseLevels("a1", "a2", "b1")
            //Создаем список для LevelsProto
            var listOfLevelsBuilders = mutableListOf<LevelsProto>()

            listOfLevelsBuilders = convertLevelsToProtoLevels(baseLevels)

            if (listOfLevelsBuilders.size != 3) {
                Log.e("Main Page Model", "checkUserData, listOfLevelsBuilders don`t has 3 elements")
                throw Exception()
            }

            try {
                val userId = UUID.randomUUID().toString()
                val user = User(userId = userId, countLearningWords = 10)
                val emptyListOfIds: List<Int> = listOf()
                setUserProtoData(
                    context,
                    user,
                    listOfLevelsBuilders,
                    emptyListOfIds,
                    user.convertDateToTimestamp()
                )
            } catch (e: Exception) {
                Log.e(
                    "Check user data. Update Proto DataStore",
                    "can`t update, err: $e"
                )
                throw e
            }
            //Получаем user для того чтобы ниже получить данные
            checkUser = userFlow.first()

        }

        //Добавляем в flow levels model уровни
        var listOfLevels = checkUser.listOfLevels
        //Проверка в случае если, количество уровней == 0
        if (listOfLevels.size == 0) {
            //Создаем Scope для запуска корутин
            val myScope = CoroutineScope(Dispatchers.IO)
            //Получаем уровни для данных в карточках на странице выбора уровней
            myScope.launch {
                listOfLevels = db.getDao().getBaseLevels("a1", "a2", "b1").toMutableList()
            }.join()
        }

        listOfLevels.forEach { locLevel ->
            if (locLevel.id != null) {
                flowLevels.add(locLevel.name)
            } else {
                Log.e(
                    "level id is null",
                    "Main page model, checkUserData, level id is null"
                )
                throw Exception()
            }
        }

        Log.i("Check user data", "Success checked user data")
        return flowLevels
    }

    //TODO почистить context где он не используется
    override suspend fun updateWordsLevels(
        db: MainDB,
        listOfNewWords: List<Words>,
        stage: Int
    ) {
        listOfNewWords.forEach { word ->
            val myScope = CoroutineScope(Dispatchers.IO)
            //TODO сделать обновление levels.count_learned_words когда слово выучено
            myScope.launch {
                if (word.id != null) {
                    //Если фаза не введена, то получаем из БД
                    if (stage == -1) {
                        var stageChanged = -1
                        //TODO так как words_levels может быть несколько слов, нужно поменять, чтобы обновлять все слова
                        //Получаем фазу для данного слова
                        myScope.launch {
                            stageChanged = db.getDao().getStageByWordId(word.id)
                            stageChanged++
                        }.join()
                        //TODO Поменять значения 24 72 168 720 4320
                        //TODO добавить проверку для ситуации когда больше 5
                        var countHoursForAdd = 0L
                        when (stageChanged) {
                            1 -> {
                                countHoursForAdd = 1
                            }

                            2 -> {
                                countHoursForAdd = 1
                            }

                            3 -> {
                                countHoursForAdd = 1
                            }

                            4 -> {
                                countHoursForAdd = 1
                            }

                            5 -> {
                                countHoursForAdd = 1
                            }

                            else -> {
                                //TODO добавить обновление кол-ва выученных слов
                                stageChanged = 6
                                countHoursForAdd = 0
                            }
                        }

                        //TODO Поменять на Hours
                        val dateLearn =
                            Date.from(Instant.now().plus(countHoursForAdd, ChronoUnit.MILLIS))
                        db.getDao().updateWordLevelsStage(word.id, stageChanged, dateLearn)

                    } else {
                        val dateLearn = Date.from(Instant.now().plus(4, ChronoUnit.MILLIS))
                        db.getDao().updateWordLevelsStage(word.id, stage, dateLearn)
                    }

                } else {
                    Log.e("Main Page Model", "updateWordsLevels word id is null")
                    throw Exception()
                }
            }
        }
    }

    override suspend fun updateUserProto(
        context: Context,
        user: User,
        listOfLevelsBuilders: MutableList<LevelsProto>,
        listOfWordsIdsForRepeat: List<Int>,
        lastTimeLearned: Timestamp
    ) {
        setUserProtoData(
            context,
            user,
            listOfLevelsBuilders,
            listOfWordsIdsForRepeat,
            lastTimeLearned
        )
    }

    override suspend fun clearUserData(context: Context) {
        try {
            val listOfWordsIdsForRepeat: List<Int> = listOf()
            val timestamp = Timestamp.newBuilder().setSeconds(0).setNanos(0).build()
            //Обновляем данные в Proto DataStore
            context.userParamsDataStore.updateData { userPorto ->
                userPorto.toBuilder()
                    .clearListOfWordsIdsForRepeat()
                    .clearListOfLevels()
                    .setUserId("")
                    .setCurRepeatDays(0)
                    .setMaxRepeatDays(0)
                    .setCountFullLearnedWords(0)
                    .setCountLearningWords(0)
                    .setCountLearnedWordsToday(0)
                    .setCheckLearnedAllWordsToday(false)
                    .setCountKnewWords(0)
                    .setCheckBritishVariables(false)
                    .setLastTimeLearnedWords(timestamp)
                    .addAllListOfWordsIdsForRepeat(listOfWordsIdsForRepeat)
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
                                "",
                                britishVariable,
                                levelId,
                                0
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
                            val wordLevels = WordsLevels(id, levelId, 0, Date(0))
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

    private fun getUserProtoData(context: Context, db: MainDB): Flow<User> {
        //Получаем данные из хранилища Proto DataStore
        val userFlow: Flow<User> = context.userParamsDataStore.data.map { userProto ->
            User(
                userProto.userId,
                userProto.curRepeatDays,
                userProto.maxRepeatDays,
                userProto.countFullLearnedWords,
                userProto.countLearningWords,
                userProto.countLearnedWordsToday,
                userProto.checkLearnedAllWordsToday,
                userProto.countKnewWords,
                userProto.listOfLevelsList.map { levelsProto ->
                    convertLevelsProtoToLevels(levelsProto)
                }.toMutableList(),
                userProto.checkBritishVariables,
                Instant.ofEpochSecond(
                    userProto.lastTimeLearnedWords.seconds,
                    userProto.lastTimeLearnedWords.nanos.toLong()
                ),
                getWordsByIds(userProto.listOfWordsIdsForRepeatList, db),
                userProto.countRepeatedWordsToday

            )
        }
        return userFlow
    }

    private suspend fun setUserProtoData(
        context: Context,
        user: User,
        listOfLevelsBuilders: MutableList<LevelsProto>,
        listOfWordsIdsForRepeat: List<Int>,
        lastTimeLearned: Timestamp
    ) {
        if (listOfWordsIdsForRepeat.isNotEmpty()) {
            //Полностью обновляем пользователя
            context.userParamsDataStore.updateData { userPorto ->
                userPorto.toBuilder()
                    //Очищаем user proto data
                    .clearListOfWordsIdsForRepeat()
                    .clearListOfLevels()
                    //Меняем user proto data
                    .setUserId(user.userId)
                    .setCurRepeatDays(user.curRepeatDays)
                    .setMaxRepeatDays(user.maxRepeatDays)
                    .setCountFullLearnedWords(user.countFullLearnedWords)
                    .setCountLearningWords(user.countLearningWords)
                    .setCountLearnedWordsToday(user.countLearnedWordsToday)
                    .setCheckLearnedAllWordsToday(user.checkLearnedAllWordsToday)
                    .setCountKnewWords(user.countKnewWords)
                    .addAllListOfLevels(listOfLevelsBuilders)
                    .setCheckBritishVariables(false)
                    .setLastTimeLearnedWords(lastTimeLearned)
                    .addAllListOfWordsIdsForRepeat(listOfWordsIdsForRepeat)
                    .setCountRepeatedWordsToday(user.countRepeatedWordsToday)
                    .build()
            }

        } else {
            //Не обновляем список words ids
            context.userParamsDataStore.updateData { userPorto ->
                userPorto.toBuilder()
                    .clearListOfLevels()
                    .setUserId(user.userId)
                    .setCurRepeatDays(user.curRepeatDays)
                    .setMaxRepeatDays(user.maxRepeatDays)
                    .setCountFullLearnedWords(user.countFullLearnedWords)
                    .setCountLearningWords(user.countLearningWords)
                    .setCountLearnedWordsToday(user.countLearnedWordsToday)
                    .setCheckLearnedAllWordsToday(user.checkLearnedAllWordsToday)
                    .setCountKnewWords(user.countKnewWords)
                    .addAllListOfLevels(listOfLevelsBuilders)
                    .setCheckBritishVariables(false)
                    .setLastTimeLearnedWords(lastTimeLearned)
                    .build()
            }
        }
    }

    //Функция чтобы при получении ids уровней из proto преобразовать данные в list words
    private fun getWordsByIds(listOfIds: MutableList<Int>, db: MainDB): HashMap<Words, String> {
        val hashMap: HashMap<Words, String> = hashMapOf()

        runBlocking {
            // List to hold jobs
            val jobs = mutableListOf<Job>()

            val myScope = CoroutineScope(Dispatchers.IO)
            listOfIds.forEach { id ->
                val job = myScope.launch {
                    val word = db.getDao().getWordById(id)
                    val levelName = db.getDao().getLevelNameById(word.levelId)
                    hashMap[word] = levelName
                }
                jobs.add(job)
            }

            jobs.joinAll()
        }

        return hashMap
    }
}