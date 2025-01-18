package com.example.learn_words_app.data.interfaces

import com.example.learn_words_app.data.dataBase.Words


interface WordCallback {
    fun onWordReceived(words: Words)
}
