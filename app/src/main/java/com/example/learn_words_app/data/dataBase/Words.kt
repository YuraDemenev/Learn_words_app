package com.example.learn_words_app.data.dataBase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "words", foreignKeys = [
        ForeignKey(
            //Таблица на которую указывает внешний ключ
            entity = Levels::class,
            //Указывает столбец в родительской таблице на которую указывает внешний ключ
            parentColumns = arrayOf("id"),
            //Указывает столбец для которого определен внешний ключ
            childColumns = arrayOf("level_id"),
            //Optional
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
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
    val britishVariable: String,
    @ColumnInfo(name = "level_id")
    val levelId: Int,
)
