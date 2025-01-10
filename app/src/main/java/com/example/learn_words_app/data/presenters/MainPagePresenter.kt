package com.example.learn_words_app.data.presenters

import android.content.Context
import com.example.learn_words_app.data.dataBase.MainDB
import com.example.learn_words_app.data.interfaces.MainPageContract

class MainPagePresenter(
    private val model: MainPageContract.Model,
    private var mainView: MainPageContract.View
) : MainPageContract.Presenter {
    override suspend fun checkUserData(context: Context, db: MainDB) {
        model.checkUserData(context, db)
    }

    //For Dev
    override suspend fun clickOnUpDB(c: Context, db: MainDB) {
        model.upDB(c, db)
    }

    //For Dev
    override fun clickOnDownDB(db: MainDB) {
        model.downDB(db)
    }

    //For Dev
    override suspend fun clearUserData(context: Context) {
        model.clearUserData(context)
    }


}