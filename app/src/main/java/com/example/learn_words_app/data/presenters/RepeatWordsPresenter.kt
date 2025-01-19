package com.example.learn_words_app.data.presenters

import com.example.learn_words_app.data.interfaces.RepeatWordsContract

class RepeatWordsPresenter(
    private val model: RepeatWordsContract.Model,
    private var mainView: RepeatWordsContract.View
) {

}