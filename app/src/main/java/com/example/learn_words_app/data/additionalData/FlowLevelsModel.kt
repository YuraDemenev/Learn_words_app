package com.example.learn_words_app.data.additionalData

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

//Класс для Flow данных для связанных с Levels (Для того чтобы изменять UI при изменении данных)
open class FlowLevelsModel : ViewModel() {
    //Lazy чтобы каждый раз заново не создавался
    val data: MutableLiveData<HashMap<String, Int>> by lazy {
        MutableLiveData<HashMap<String, Int>>(HashMap())
    }
}