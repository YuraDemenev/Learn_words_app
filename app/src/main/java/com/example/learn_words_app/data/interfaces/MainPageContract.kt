package com.example.learn_words_app.data.interfaces

import android.content.Context
import com.app.proto.LevelsProto
import com.example.learn_words_app.data.additionalData.FlowLevelsModel
import com.example.learn_words_app.data.additionalData.LevelsCardData
import com.example.learn_words_app.data.additionalData.User
import com.example.learn_words_app.data.dataBase.MainDB
import com.example.learn_words_app.data.dataBase.Words
import com.example.learn_words_app.databinding.FragmentLearnWordsBinding

interface MainPageContract {
    //View концентрируется только на UI и пользовательском взаимодействии, а логика работы с данными вынесена в другие компоненты.
    interface View {
        fun deleteExplanations(
            binding: FragmentLearnWordsBinding,
            thisContext: Context
        )

        fun nextWord(
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
        ): Boolean

        fun changeLevelName(
            binding: FragmentLearnWordsBinding,
            hashMap: HashMap<Int, String>,
            listOfWords: MutableList<Words>,
            indexWord: Int
        )

        fun changePageToYouAllLearned(binding: FragmentLearnWordsBinding)

//        fun addExplanation(binding: FragmentLearnWordsBinding, word: Words, thisContext: Context)
    }

    //Управляет данными приложения.
    //Выполняет бизнес-логику, например, получает данные из базы данных, сетевого API или других источников.
    //Не знает ничего о View и UI.
    interface Model {
        suspend fun getWordsForRepeat(db: MainDB): Array<Int>

        suspend fun getWordsForLearn(
            context: Context,
            db: MainDB,
            flowLevelsModel: FlowLevelsModel,
            countLearningWords: Int
        ): Pair<MutableList<Words>, HashMap<Int, String>>

        suspend fun getOneWordForLearn(
            context: Context,
            db: MainDB,
            flowLevelsModel: FlowLevelsModel,
            wordId: Int?,
            callback: WordCallback
        )

        suspend fun getUser(context: Context, db: MainDB): User

        suspend fun getLevelsCardData(context: Context, db: MainDB): MutableList<LevelsCardData>

        suspend fun checkUserData(context: Context, db: MainDB, flowLevelsModel: FlowLevelsModel)

        suspend fun updateWordsLevels(
            db: MainDB,
            listOfNewWords: List<Words>,
            stage: Int
        )

        suspend fun updateUserProto(
            context: Context,
            user: User,
            listOfLevelsBuilders: MutableList<LevelsProto>,
            listOfWordsIdsForRepeat: List<Int>
        )

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
        suspend fun getWordsForRepeat(db: MainDB): Array<Int>
        fun deleteExplanations(
            binding: FragmentLearnWordsBinding,
            thisContext: Context
        )

        //Views
//-----------------------------------------------------------------------------------------------------------------------------------------------------
        fun nextWord(
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
        ): Boolean

        fun changeLevelName(
            binding: FragmentLearnWordsBinding,
            hashMap: HashMap<Int, String>,
            listOfWords: MutableList<Words>,
            indexWord: Int
        )

        fun changePageToYouAllLearned(binding: FragmentLearnWordsBinding)

        //Model
//-----------------------------------------------------------------------------------------------------------------------------------------------------
        suspend fun getWordsForLearn(
            context: Context,
            db: MainDB,
            flowLevelsModel: FlowLevelsModel,
            countLearningWords: Int
        ): Pair<MutableList<Words>, HashMap<Int, String>>

        suspend fun getUser(context: Context, db: MainDB): User

        suspend fun getLevelsCardData(context: Context, db: MainDB): MutableList<LevelsCardData>

        suspend fun checkUserData(context: Context, db: MainDB, flowLevelsModel: FlowLevelsModel)

        suspend fun updateWordsLevels(
            db: MainDB,
            listOfNewWords: List<Words>,
            stage: Int
        )

        suspend fun updateUserProto(
            context: Context,
            user: User,
            listOfLevelsBuilders: MutableList<LevelsProto>,
            listOfWordsIdsForRepeat: List<Int>
        )

        suspend fun getOneWordForLearn(
            context: Context,
            db: MainDB,
            flowLevelsModel: FlowLevelsModel,
            wordId: Int?,
            callback: WordCallback
        )

        //For Dev
        suspend fun clickOnUpDB(c: Context, db: MainDB)

        //For Dev
        fun clickOnDownDB(db: MainDB)

        //For Dev
        suspend fun clearUserData(context: Context)
    }
}