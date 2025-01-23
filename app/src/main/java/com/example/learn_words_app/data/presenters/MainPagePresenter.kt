package com.example.learn_words_app.data.presenters

import android.content.Context
import com.app.proto.LevelsProto
import com.example.learn_words_app.data.additionalData.FlowLevelsModel
import com.example.learn_words_app.data.additionalData.LevelsCardData
import com.example.learn_words_app.data.additionalData.User
import com.example.learn_words_app.data.dataBase.MainDB
import com.example.learn_words_app.data.dataBase.Words
import com.example.learn_words_app.data.interfaces.MainPageContract
import com.example.learn_words_app.data.interfaces.WordCallback
import com.example.learn_words_app.databinding.FragmentLearnWordsBinding

class MainPagePresenter(
    private val model: MainPageContract.Model,
    private var mainView: MainPageContract.View
) : MainPageContract.Presenter {

    override fun nextWord(
        binding: FragmentLearnWordsBinding,
        user: User,
        listOfNewWords: MutableList<Words>,
        indexWord: Int,
        listOfWords: MutableList<Words>,
        countLearnedWords: Int,
        hashMap: HashMap<Int, String>,
        checkAddWord: Boolean,
        thisContext: Context,
        checkExplanation: Boolean
    ): Boolean {
        return mainView.nextWord(
            binding,
            user,
            listOfNewWords,
            indexWord,
            listOfWords,
            countLearnedWords,
            hashMap,
            checkAddWord,
            thisContext,
            checkExplanation
        )
    }

    override fun changeLevelName(
        binding: FragmentLearnWordsBinding,
        hashMap: HashMap<Int, String>,
        listOfWords: MutableList<Words>,
        indexWord: Int
    ) {
        mainView.changeLevelName(binding, hashMap, listOfWords, indexWord)
    }

    override fun changePageToYouAllLearned(binding: FragmentLearnWordsBinding) {
        mainView.changePageToYouAllLearned(binding)
    }

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
        db: MainDB,
        listOfNewWords: List<Words>,
        stage: Int
    ) {
        model.updateWordsLevels(db, listOfNewWords, stage)
    }

    override suspend fun updateUserProto(
        context: Context,
        user: User,
        listOfLevelsBuilders: MutableList<LevelsProto>
    ) {
        model.updateUserProto(context, user, listOfLevelsBuilders)
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