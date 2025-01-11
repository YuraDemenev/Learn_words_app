package com.example.learn_words_app.data.interfaces

import android.content.Context
import com.example.learn_words_app.data.additionalData.LevelsCardData
import com.example.learn_words_app.data.dataBase.MainDB

interface MainPageContract {
    //View концентрируется только на UI и пользовательском взаимодействии, а логика работы с данными вынесена в другие компоненты.
    interface View {

    }

    //Управляет данными приложения.
    //Выполняет бизнес-логику, например, получает данные из базы данных, сетевого API или других источников.
    //Не знает ничего о View и UI.
    interface Model {
        suspend fun getLevelsCardData(context: Context, db: MainDB): MutableList<LevelsCardData>

        suspend fun checkUserData(context: Context, db: MainDB)

        //For Dev
        suspend fun upDB(c: Context, db: MainDB)

        //For Dev
        fun downDB(db: MainDB)

        //For Dev
        suspend fun clearUserData(context: Context)
    }

    //Посредник между View и Model.
    //Получает действия пользователя из View и запрашивает данные у Model.
    //Возвращает обработанные данные в View.
    interface Presenter {
        suspend fun getLevelsCardData(context: Context, db: MainDB): MutableList<LevelsCardData>

        suspend fun checkUserData(context: Context, db: MainDB)

        //For Dev
        suspend fun clickOnUpDB(c: Context, db: MainDB)

        //For Dev
        fun clickOnDownDB(db: MainDB)

        //For Dev
        suspend fun clearUserData(context: Context)
    }


}