package com.example.learn_words_app.dataBase

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface WordsDAO {
    //Upsert объеденение insert и update
    @Upsert
    fun insertWord(word: Words)

    @Query("SELECT * FROM words")
    //Flow похож на каналы в Go, данные приходят когда обновляются, можно использовать
    //observe для контроля изменений в данных
    fun getItems(): Flow<List<Words>>

}