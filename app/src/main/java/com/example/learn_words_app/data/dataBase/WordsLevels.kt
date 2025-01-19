package com.example.learn_words_app.data.dataBase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import java.util.Date

//Junction Table
@Entity(
    tableName = "words_levels", foreignKeys = [
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
        ),
        ForeignKey(
            //Таблица на которую указывает внешний ключ
            entity = Words::class,
            //Указывает столбец в родительской таблице на которую указывает внешний ключ
            parentColumns = arrayOf("id"),
            //Указывает столбец для которого определен внешний ключ
            childColumns = arrayOf("word_id"),
            //Optional
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["word_id", "level_id"]
)

data class WordsLevels(
    @ColumnInfo(name = "word_id")
    val wordId: Int,
    @ColumnInfo(name = "level_id", index = true)
    val levelId: Int,
    @ColumnInfo(name = "stage")
    val stage: Int,
    @ColumnInfo(name = "date_for_repeat")
    val dateForLearn: Date?
)