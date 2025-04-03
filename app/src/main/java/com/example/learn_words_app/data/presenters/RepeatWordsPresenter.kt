package com.example.learn_words_app.data.presenters

import android.content.Context
import com.example.learn_words_app.data.additionalData.User
import com.example.learn_words_app.data.dataBase.MainDB
import com.example.learn_words_app.data.dataBase.Words
import com.example.learn_words_app.data.interfaces.RepeatWordsContract
import com.example.learn_words_app.databinding.FragmentRepeatWordsBinding

class RepeatWordsPresenter(
    private val model: RepeatWordsContract.Model,
    private var view: RepeatWordsContract.View
) : RepeatWordsContract.Presenter {
    override fun nextWords(
        binding: FragmentRepeatWordsBinding,
        checkEnglishWord: Boolean,
        user: User,
        indexWord: Int,
        listOfWords: List<Pair<Words, String>>,
        thisContext: Context,
        checkExplanation: Boolean,
        db: MainDB
    ): Pair<Boolean, Words> {
        return view.nextWords(
            binding,
            checkEnglishWord,
            user,
            indexWord,
            listOfWords,
            thisContext,
            checkExplanation,
            db
        )
    }

    override fun writeWord(
        binding: FragmentRepeatWordsBinding,
        checkEnglishWord: Boolean,
        indexWord: Int,
        listOfWords: List<Pair<Words, String>>,
        thisContext: Context
    ) {
        return view.writeWord(
            binding,
            checkEnglishWord,
            indexWord,
            listOfWords,
            thisContext,
        )
    }
}