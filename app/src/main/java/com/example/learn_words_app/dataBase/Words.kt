package com.example.learn_words_app.dataBase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class Words(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    @ColumnInfo(name = "english_word")
    val englishWord: String,
    @ColumnInfo(name = "russian_translation")
    val russianTranslation: String,
    @ColumnInfo(name = "transcription_id")
    val transcriptionId: Int,
    @ColumnInfo(name = "has_british_variable")
    val hasBritishVariable: Boolean,
    @ColumnInfo(name = "british_variable")
    val britishVariable: String
)
