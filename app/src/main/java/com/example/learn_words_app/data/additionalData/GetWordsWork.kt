package com.example.learn_words_app.data.additionalData

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.learn_words_app.data.dataBase.MainDB
import com.example.learn_words_app.data.models.MainPageModel
import com.example.learn_words_app.data.presenters.MainPagePresenter
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

        var arrayOfWords: Array<Int> = arrayOf()
        val myScope = CoroutineScope(Dispatchers.IO)

        //TODO сделать callback?
        runBlocking {
            myScope.launch {
                arrayOfWords = presenter.getWordsForRepeat(db)
            }.join()
        }

        //Обновляем proto data
        presenter.updateUserProto()



        return Result.success()
    }
}