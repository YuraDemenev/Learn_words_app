package com.example.learn_words_app.data.additionalData

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

//Класс для Flow данных для связанных с Levels (Для того чтобы изменять UI при изменении данных)
open class FlowLevelsModel : ViewModel() {
    //Lazy чтобы каждый раз заново не создавался
    //HashMap здесь не получится сделать потому что когда будет отрисовываться levels recycle view
    // негде хранить информацию об id level
    val data: MutableLiveData<HashSet<String>> by lazy {
        MutableLiveData<HashSet<String>>(HashSet())
    }
}