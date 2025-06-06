package com.example.learn_words_app.data.dataBase

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Upsert
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface WordsDAO {
    //Upsert объединение insert и update
    //Добавляем слово
    @Upsert
    suspend fun insertWord(word: Words): Long

    //Добавляем уровень английского
    @Upsert
    suspend fun insertLevel(level: Levels)

    //Добавляем значения в junction table
    @Upsert
    suspend fun insertWordsLevel(wordsLevel: WordsLevels)

    //Удаляем таблицу words
    @Query("DELETE FROM words")
    suspend fun deleteDataFromWordsTable()

    //Удаляем таблицу levels
    @Query("DELETE FROM levels")
    suspend fun deleteDataFromLevelsTable()

    //Удаляем primaryKeys, чтобы в новой таблице id начинался с 1
    @Query("DELETE FROM sqlite_sequence")
    suspend fun deletePrimaryKeys()

    @Query("UPDATE words SET stage = :stage, date_for_repeat = :date WHERE id = :wordId")
    fun updateWordStage(wordId: Int, stage: Int, date: Date)

    @Query(
        """
        SELECT id FROM words 
        WHERE words.date_for_repeat <= :dateNow AND words.date_for_repeat != 0 AND words.stage <6
        """
    )
    fun getWordsForRepeat(dateNow: Long?): Array<Int>

    @Query("SELECT name FROM levels WHERE id = :levelId")
    suspend fun getLevelNameById(levelId: Int): String

    @Query(
        """
            SELECT * 
            FROM levels 
            JOIN words_levels ON levels.id=words_levels.level_id 
            WHERE words_levels.word_id = :wordId 
            LIMIT 1
        """
    )
    suspend fun getLevelByWordId(wordId: Int): Levels

    @Query("SELECT * FROM words WHERE words.id = :wordId")
    suspend fun getWordById(wordId: Int): Words

    @Query("SELECT id FROM words WHERE words.english_word = :word")
    suspend fun getWordIdByEnglishWord(word: String): Int

    //Динамический запрос для получения 1 слова
    @RawQuery
    suspend fun getWordByLevelsIds(query: SupportSQLiteQuery): Words
    suspend fun getWordByLevelsIdsMultiplyQueries(ids: Array<Int>): Words {
        val placeholders = ids.joinToString(" OR ") { "words.level_id = ?" }
        val args = ids.map { "$it" }.toTypedArray()
        val query = SimpleSQLiteQuery(
            "SELECT * FROM words " +
                    "JOIN words_levels on words.id = words_levels.word_id " +
                    "WHERE $placeholders AND words_levels.stage = 0 " +
                    "ORDER BY RANDOM() LIMIT 1",
            args
        )
        return getWordByLevelsIds(query)
    }

    //Динамический запрос для получения слов из таблицы по уровням
    @RawQuery
    suspend fun getWordsByLevelsIds(query: SupportSQLiteQuery): List<Words>
    suspend fun getWordsByLevelsIdsMultiplyQueries(ids: Array<Int>, countWords: Int): List<Words> {
        val placeholders = ids.joinToString(" OR ") { "words_levels.level_id = ?" }
        val args = ids.map { "$it" }.toTypedArray()

        //Чтобы получить больше слов, и когда пользователь нажимает на я знаю это слово, ему не казалось что приложение лагает.
        //Потому что надо снова идти в БД за данными
        val changedCountWords = countWords + 10
        val query = SimpleSQLiteQuery(
            "SELECT words.* FROM words " +
                    "JOIN words_levels on words.id = words_levels.word_id " +
                    "WHERE $placeholders AND words.stage = 0 " +
                    "ORDER BY RANDOM() LIMIT $changedCountWords",
            args
        )
        return getWordsByLevelsIds(query)
    }

    //Динамический запрос для получения уровней из таблицы по именам
    @RawQuery
    suspend fun getLevelsByNames(query: SupportSQLiteQuery): List<Levels>
    suspend fun getLevelsByNamesMultipleQueries(levels: HashSet<String>): List<Levels> {
        val placeholders = levels.joinToString(" OR ") { "name = ?" }
        val args = levels.map { "$it" }.toTypedArray()
        //В array могут быть темы в которых есть пробелы, их нужно заменить на _
        args.forEachIndexed { i, word ->
            if (word.contains(" ")) {
                val newWord = word.replace(" ", "_")
                args[i] = newWord
            }

        }
        val query = SimpleSQLiteQuery("SELECT * FROM levels WHERE $placeholders", args)
        return getLevelsByNames(query)
    }

    @Query("SELECT * FROM words")
    //Flow похож на каналы в Go, данные приходят когда обновляются, можно использовать
    //observe для контроля изменений в данных
    fun getItems(): Flow<List<Words>>

    // Запрос на получение id из таблицы levels по name
    @Query("SELECT id FROM levels WHERE name = :name")
    fun getLevelId(name: String): Int

    //Запрос на получения базовые ids Levels по имени
    @Query("SELECT * FROM levels WHERE name = :name1 OR name = :name2 OR name = :name3 GROUP BY id")
    fun getBaseLevels(name1: String, name2: String, name3: String): List<Levels>

    //Запрос на получение всех Levels
    @Query("SELECT * FROM levels")
    fun getAllLevels(): Array<Levels>

    //Запрос на получение кол-во слов с определенным уровнем
    @Query("SELECT COUNT(*) FROM words_levels WHERE level_id=:levelId")
    fun getCountWordsByLevelId(levelId: Int): Int

    // Запрос на получение количества записей в таблице levels
    @Query("SELECT COUNT(*) FROM levels")
    suspend fun getCountLevels(): Int

    //Запрос на получения stage из words_levels
    @Query("SELECT stage FROM words WHERE id = :wordId")
    suspend fun getStageByWordId(wordId: Int): Int

    //Запрос на проверку наличие записи в таблице по имени
    @Query("SELECT EXISTS(SELECT * FROM levels WHERE name = :name)")
    suspend fun checkLevelExist(name: String): Boolean
}