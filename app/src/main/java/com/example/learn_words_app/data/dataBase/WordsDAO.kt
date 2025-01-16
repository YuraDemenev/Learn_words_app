package com.example.learn_words_app.data.dataBase

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Upsert
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow

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

    //Динамический запрос для получения слов из таблицы по уровням
    @RawQuery
    suspend fun getWordsByLevelsIds(query: SupportSQLiteQuery): List<Words>
    suspend fun getWordsByLevelsIdsMultiplyQueries(ids: Array<Int>, countWords: Int): List<Words> {
        val placeholders = ids.joinToString(" OR ") { "level_id = ?" }
        val args = ids.map { "$it" }.toTypedArray()
        val query = SimpleSQLiteQuery(
            "SELECT * FROM words WHERE $placeholders ORDER BY RANDOM() LIMIT $countWords",
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
    @Query("SELECT COUNT(*) FROM words WHERE level_id=:levelId")
    fun getCountWordsByLevelId(levelId: Int): Int

    //Запрос на проверку наличие записи в таблице по имени
    @Query("SELECT EXISTS(SELECT * FROM levels WHERE name = :name)")
    suspend fun checkLevelExist(name: String): Boolean

    // Запрос на получение количества записей в таблице levels
    @Query("SELECT COUNT(*) FROM levels")
    suspend fun getCountLevels(): Int
}