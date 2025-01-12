package com.example.learn_words_app.data.additionalData

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

//Класс для Flow данных для связанных с Levels
open class FlowLevelsModel : ViewModel() {
    //Lazy чтобы каждый раз заново не создавался
    val data: MutableLiveData<HashSet<String>> by lazy {
        MutableLiveData<HashSet<String>>()
    }
}