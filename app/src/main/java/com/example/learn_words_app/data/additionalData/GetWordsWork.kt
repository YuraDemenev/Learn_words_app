package com.example.learn_words_app.data.additionalData

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.learn_words_app.data.dataBase.MainDB
import com.example.learn_words_app.data.models.MainPageModel
import com.example.learn_words_app.data.presenters.MainPagePresenter
import com.example.learn_words_app.data.proto.convertLevelsToProtoLevels
import com.example.learn_words_app.data.views.MainPageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class GetWordsWork(thisContext: Context, workerParams: WorkerParameters) :
    Worker(thisContext, workerParams) {
    override fun doWork(): Result {
        val db = MainDB.getDB(applicationContext)
        val presenter = MainPagePresenter(MainPageModel(), MainPageView())

        var arrayOfWordsIds: Array<Int> = arrayOf()
        val myScope = CoroutineScope(Dispatchers.IO)
        lateinit var user: User

        runBlocking {
            myScope.launch {
                arrayOfWordsIds = presenter.getWordsForRepeat(db)
                user = presenter.getUser(applicationContext, db)
            }.join()
        }

        val listOfProtoLevels = convertLevelsToProtoLevels(user.listOfLevels)

        //Обновляем proto data
        myScope.launch {
            presenter.updateUserProto(
                applicationContext,
                user,
                listOfProtoLevels,
                arrayOfWordsIds.toList(),
                user.convertDateToTimestamp()
            )
        }

        return Result.success()
    }
}