package com.example.learn_words_app.data.additionalData

import com.example.learn_words_app.data.dataBase.Levels
import com.example.learn_words_app.data.dataBase.Words
import com.google.protobuf.Timestamp
import java.time.Instant

data class User(
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
    var hashMapOfWordsForRepeatAndLevelsNames: HashMap<Words, String> = hashMapOf(),
    var countRepeatedWordsToday: Int = 0
)

//Функция, чтобы вернуть TimeStamp для изменения proto data
// (Время в proto data должно меняться только когда пользователь выучил все новые слова, поэтому
// если надо обновить и не менять время, возвращаем то время, которое есть у пользователя)
fun User.convertDateToTimestamp(): Timestamp {
    return Timestamp.newBuilder()
        .setSeconds(this.lastTimeLearnedWords.epochSecond)
        .setNanos(this.lastTimeLearnedWords.nano).build()
}
//TODO добавить app verison?
