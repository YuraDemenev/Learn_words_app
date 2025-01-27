package com.example.learn_words_app.data.additionalData

import com.example.learn_words_app.data.dataBase.Levels
import com.example.learn_words_app.data.dataBase.Words
import com.google.protobuf.Timestamp
import java.time.Instant

class User(
    var userId: String = "",
    var curRepeatDays: Int = 0,
    var maxRepeatDays: Int = 0,
    var countFullLearnedWords: Int = 0,
    var countLearningWords: Int = 0,
    var countLearnedWordsToday: Int = 0,
    var checkLearnedAllWordsToday: Boolean = false,
    var countKnewWords: Int = 0,
    var listOfLevels: MutableList<Levels> = mutableListOf(),
    var checkBritishVariables: Boolean = false,
    var lastTimeLearnedWords: Instant = Instant.now(),
    var listOfWordsForRepeat: List<Words> = listOf()
)

fun User.convertDateToTimestamp(): Timestamp {
    return Timestamp.newBuilder()
        .setSeconds(this.lastTimeLearnedWords.epochSecond)
        .setNanos(this.lastTimeLearnedWords.nano).build()
}
