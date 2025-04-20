package com.example.learn_words_app.data.dataBase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "words", indices = [Index(value = ["english_word"], unique = true)])
data class Words(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    @ColumnInfo(name = "english_word")
    val englishWord: String,
    @ColumnInfo(name = "russian_translation")
    val russianTranslation: String,
    @ColumnInfo(name = "transcription")
    val transcriptionId: String,
    @ColumnInfo(name = "british_variable")
    val britishVariable: String,
    @ColumnInfo(name = "pronunciation_id")
    val pronunciationId: Int,
    @ColumnInfo(name = "stage")
    val stage: Int,
    @ColumnInfo(name = "date_for_repeat")
    val dateForLearn: Date?
)
