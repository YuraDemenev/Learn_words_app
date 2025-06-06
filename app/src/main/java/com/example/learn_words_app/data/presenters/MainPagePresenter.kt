package com.example.learn_words_app.data.presenters

import android.content.Context
import android.view.LayoutInflater
import com.app.proto.LevelsProto
import com.example.learn_words_app.data.additionalData.FlowLevelsModel
import com.example.learn_words_app.data.additionalData.LevelsCardData
import com.example.learn_words_app.data.additionalData.User
import com.example.learn_words_app.data.additionalData.UserViewModel
import com.example.learn_words_app.data.dataBase.MainDB
import com.example.learn_words_app.data.dataBase.Words
import com.example.learn_words_app.data.interfaces.MainPageContract
import com.example.learn_words_app.data.interfaces.WordCallback
import com.example.learn_words_app.databinding.FragmentLearnWordsBinding
import com.google.protobuf.Timestamp

class MainPagePresenter(
    private val model: MainPageContract.Model,
    private var mainView: MainPageContract.View
) : MainPageContract.Presenter {
    override fun createAlertLevels(
        thisContext: Context,
        inflater: LayoutInflater
    ) {
        return mainView.createAlertLevels(thisContext, inflater)
    }

    override suspend fun getWordsForRepeat(db: MainDB): Array<Int> {
        return model.getWordsForRepeat(db)
    }

    override fun deleteExplanations(
        binding: FragmentLearnWordsBinding,
        thisContext: Context
    ) {
        mainView.deleteExplanations(binding, thisContext)
    }

    override fun createAlertChoseCountLearningWords(
        userViewModel: UserViewModel,
        thisContext: Context,
        inflater: LayoutInflater,
    ) {
        mainView.createAlertChoseCountLearningWords(userViewModel, thisContext, inflater)
    }

    override fun nextWord(
        binding: FragmentLearnWordsBinding,
        user: User,
        listOfNewWords: MutableList<Words>,
        indexWord: Int,
        listOfWords: MutableList<Words>,
        hashMap: HashMap<Int, String>,
        checkAddWord: Boolean,
        thisContext: Context,
        checkExplanation: Boolean,
        countLearnedWordsInSession: Int,
        countWordsForLearn: Int
    ): Boolean {
        return mainView.nextWord(
            binding,
            user,
            listOfNewWords,
            indexWord,
            listOfWords,
            hashMap,
            checkAddWord,
            thisContext,
            checkExplanation,
            countLearnedWordsInSession,
            countWordsForLearn
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

    override fun changePageToYouAllLearned(binding: FragmentLearnWordsBinding, str: String) {
        mainView.changePageToYouAllLearned(binding, str)
    }

    override suspend fun checkUserData(
        context: Context,
        db: MainDB,
    ): HashSet<String> {
        return model.checkUserData(context, db)
    }

    override suspend fun getOneWordForLearn(
        db: MainDB,
        flowLevelsModel: FlowLevelsModel,
        wordId: Int?,
        callback: WordCallback
    ) {
        model.getOneWordForLearn(db, flowLevelsModel, wordId, callback)
    }

    override suspend fun getWordsForLearn(
        db: MainDB,
        flowLevelsModel: FlowLevelsModel,
        countLearningWords: Int
    ): Pair<MutableList<Words>, HashMap<Int, String>> {
        return model.getWordsForLearn(db, flowLevelsModel, countLearningWords)
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
        listOfLevelsBuilders: MutableList<LevelsProto>,
        listOfWordsIdsForRepeat: List<Int>,
        lastTimeLearned: Timestamp
    ) {
        model.updateUserProto(
            context,
            user,
            listOfLevelsBuilders,
            listOfWordsIdsForRepeat,
            lastTimeLearned
        )
    }

    override suspend fun getUser(context: Context, db: MainDB): User {
        return model.getUser(context, db)
    }

    override suspend fun getLevelsCardData(
        db: MainDB
    ): MutableList<LevelsCardData> {
        return model.getLevelsCardData(db)
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