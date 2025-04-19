package com.example.learn_words_app.data.dataBase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "levels")
data class Levels(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    @ColumnInfo(name = "name")
    val name: String,
    //TODO исправить название
//    @ColumnInfo(name = "count_learned_words")
    val countLearnedWords: Int,
)
