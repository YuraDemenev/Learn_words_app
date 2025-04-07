package com.example.learn_words_app.data.models

import com.example.learn_words_app.data.interfaces.RepeatWordsContract

class RepeatWordsModel : RepeatWordsContract.Model {
//    override suspend fun updateWordsLevels(
//        db: MainDB,
//        word: Words,
//    ) {
//        if (word.id != null) {
//            val myScope = CoroutineScope(Dispatchers.IO)
//            var stage = 0
//            //Получаем фазу для данного слова
//            myScope.launch {
//                stage = db.getDao().getStageByWordId(word.id)
//                stage++
//            }.join()
//
//            var countHoursForAdd = 0L
//            when (stage) {
//                1 -> {
//                    countHoursForAdd = 24
//                }
//
//                2 -> {
//                    countHoursForAdd = 72
//                }
//
//                3 -> {
//                    countHoursForAdd = 168
//                }
//
//                4 -> {
//                    countHoursForAdd = 720
//                }
//
//                5 -> {
//                    countHoursForAdd = 4320
//                }
//
//            }
//
//            val dateLearn =
//                Date.from(Instant.now().plus(countHoursForAdd, ChronoUnit.HOURS))
//            db.getDao().updateWordLevelsStage(word.id, stage, dateLearn)
//        } else {
//            Log.e("Repeat Words Model", "updateWordsLevels word id is null")
//            throw Exception()
//        }
//    }
}