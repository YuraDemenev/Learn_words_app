package com.example.learn_words_app.data.presenters

import android.content.Context
import com.example.learn_words_app.data.additionalData.FlowLevelsModel
import com.example.learn_words_app.data.additionalData.LevelsCardData
import com.example.learn_words_app.data.additionalData.User
import com.example.learn_words_app.data.dataBase.MainDB
import com.example.learn_words_app.data.dataBase.Words
import com.example.learn_words_app.data.interfaces.MainPageContract
import com.example.learn_words_app.data.interfaces.WordCallback

class MainPagePresenter(
    private val model: MainPageContract.Model,
    private var mainView: MainPageContract.View
) : MainPageContract.Presenter {
    override suspend fun checkUserData(
        context: Context,
        db: MainDB,
        flowLevelsModel: FlowLevelsModel
    ) {
        model.checkUserData(context, db, flowLevelsModel)
    }

    override suspend fun getOneWordForLearn(
        context: Context,
        db: MainDB,
        flowLevelsModel: FlowLevelsModel,
        wordId: Int?,
        callback: WordCallback
    ) {
        model.getOneWordForLearn(context, db, flowLevelsModel, wordId, callback)
    }

    override suspend fun updateProtoData(context: Context, flowLevelsModel: FlowLevelsModel) {
        model.updateProtoData(context, flowLevelsModel)
    }

    override suspend fun getWordsForLearn(
        context: Context,
        db: MainDB,
        flowLevelsModel: FlowLevelsModel,
        countLearningWords: Int
    ): Pair<MutableList<Words>, HashMap<Int, String>> {
        return model.getWordsForLearn(context, db, flowLevelsModel, countLearningWords)
    }

    override suspend fun updateWordsLevels(
        context: Context,
        db: MainDB,
        listOfNewWords: List<Words>
    ) {
        model.updateWordsLevels(context, db, listOfNewWords)
    }

    override suspend fun getUser(context: Context): User {
        return model.getUser(context)
    }

    override suspend fun getLevelsCardData(
        context: Context,
        db: MainDB
    ): MutableList<LevelsCardData> {
        return model.getLevelsCardData(context, db)
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