package com.example.learn_words_app.data.dataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.learn_words_app.data.additionalData.Converters

@Database(
    entities = [Words::class, Levels::class, WordsLevels::class],
    version = 17,
    exportSchema = true
)
@TypeConverters(Converters::class)
//abstract class Используется для определения общих характеристик и поведения, которые
// разделяют классы-наследники.
abstract class MainDB : RoomDatabase() {
    //Абстрактная функция не имеет тела (реализации) и должна быть переопределена в
    // классе-наследнике. Она задает "контракт", который обязаны выполнить все наследники.
    abstract fun getDao(): WordsDAO

    // companion object - способ определять статические члены класса
    companion object {
        fun getDB(context: Context): MainDB {
            return Room.databaseBuilder(
                context.applicationContext,
                MainDB::class.java,
                "words.DB"
            ).fallbackToDestructiveMigration().build()
        }
    }
}