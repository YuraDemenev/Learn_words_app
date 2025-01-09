package com.example.learn_words_app.data.userData

import com.example.learn_words_app.data.dataBase.Levels

class User(
    var userId: String = "",
    var curRepeatDays: Int = 0,
    var maxRepeatDays: Int = 0,
    var countFullLearnedWords: Int = 0,
    var countLearningWords: Int = 0,
    var countKnewWords: Int = 0,
    var listOfLevels: MutableList<Levels> = mutableListOf(),
    var checkBritishVariables: Boolean = false
)
//userId_: String,
//curRepeatDays_: Int,
//maxRepeatDays_: Int,
//countFullLearnedWords_: Int,
//countLearningWords_: Int,
//countKnewWords_: Int,
//listOfLevels_: MutableList<Levels>,
//checkBritishVariables_: Boolean