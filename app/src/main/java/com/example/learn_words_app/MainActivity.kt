package com.example.learn_words_app

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.app.proto.LevelsProto
import com.example.learn_words_app.data.additionalData.FlowLevelsModel
import com.example.learn_words_app.data.additionalData.FragmentsNames
import com.example.learn_words_app.data.additionalData.GetWordsWork
import com.example.learn_words_app.data.additionalData.UserViewModel
import com.example.learn_words_app.data.additionalData.convertDateToTimestamp
import com.example.learn_words_app.data.dataBase.MainDB
import com.example.learn_words_app.data.fragments.LearnWordsFragment
import com.example.learn_words_app.data.fragments.LevelsFragment
import com.example.learn_words_app.data.fragments.MainFragment
import com.example.learn_words_app.data.fragments.RepeatWordsFragment
import com.example.learn_words_app.data.models.MainPageModel
import com.example.learn_words_app.data.presenters.MainPagePresenter
import com.example.learn_words_app.data.views.MainPageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val userViewModel: UserViewModel by viewModels()
    private val flowLevelsModel: FlowLevelsModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Получаем user из proto data
        val db = MainDB.getDB(this)
        val presenter = MainPagePresenter(MainPageModel(), MainPageView())
        val thisContext = this

        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                flowLevelsModel.updateLevels(presenter.checkUserData(thisContext, db))
                val user = presenter.getUser(thisContext, db)

                userViewModel.updateUser(user)
            }
        }


        //Создаём work request, чтобы каждые 2 часа получать слова из БД, которые надо повторять
        val workRequest =
            PeriodicWorkRequestBuilder<GetWordsWork>(1, TimeUnit.MINUTES)
                .addTag("GetWordsWork")
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "GetWordsWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest,
        )


//        deleteWorks()
//        getUUID()

        setContentView(R.layout.empty_fragment)
        loadFragment(FragmentsNames.MAIN)
    }

    fun loadFragment(fragmentName: FragmentsNames) {
        val manager = supportFragmentManager.beginTransaction()
        when (fragmentName) {
            FragmentsNames.MAIN -> {
                manager.replace(R.id.empty_fragment_container, MainFragment()).commit()
            }

            FragmentsNames.LEVELS -> {
                manager.replace(R.id.empty_fragment_container, LevelsFragment()).commit()
            }

            FragmentsNames.LEARN_WORDS -> {
                manager.replace(R.id.empty_fragment_container, LearnWordsFragment()).commit()
            }

            FragmentsNames.REPEAT_WORDS -> {
                manager.replace(R.id.empty_fragment_container, RepeatWordsFragment()).commit()
            }

            else -> {
                Log.e(
                    "Invalid fragment name",
                    "LoadFragment in main activity, invalid fragment name"
                )
                throw Exception("Invalid fragment name")
            }
        }
    }

    //TODO onPause поменять на onStop для прода?
    override fun onPause() {
        super.onPause()
        //Сохраняем пользователя в proto data
        val user = userViewModel.getUser()
        val presenter = MainPagePresenter(MainPageModel(), MainPageView())
        val myScope = CoroutineScope(Dispatchers.IO)
        val listOfLevelsBuilders = mutableListOf<LevelsProto>()
        val emptyList: List<Int> = listOf()
        val thisContext = this

        //Получаем уровни, которые выбраны у пользователя
        val levels = flowLevelsModel.getData()
        levels.forEach { level ->
            listOfLevelsBuilders.add(
                LevelsProto.newBuilder().setId(0).setName(level).build()
            )
        }

        runBlocking {
            myScope.launch {
                presenter.updateUserProto(
                    thisContext,
                    user,
                    listOfLevelsBuilders,
                    emptyList,
                    user.convertDateToTimestamp()
                )
            }.join()
        }
    }


    //TODO сделать background для status bar

    private fun checkWorkRequestStatus(workRequestId: UUID, context: Context) {
        val workManager = WorkManager.getInstance(context)
        val workInfo = workManager.getWorkInfoById(workRequestId).get()

        if (workInfo != null) {
            when (workInfo.state) {
                WorkInfo.State.ENQUEUED -> println("Work is enqueued")
                WorkInfo.State.RUNNING -> println("Work is running")
                WorkInfo.State.SUCCEEDED -> println("Work succeeded")
                WorkInfo.State.FAILED -> println("Work failed")
                WorkInfo.State.BLOCKED -> println("Work is blocked")
                WorkInfo.State.CANCELLED -> println("Work is cancelled")
            }
        }
    }

    private fun queryWorkRequestsByUniqueName(uniqueName: String, context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.getWorkInfosForUniqueWorkLiveData(uniqueName).observeForever { workInfoList ->
            workInfoList?.forEach { workInfo ->
                Log.i("test", "Work ID: ${workInfo.id}, State: ${workInfo.state}")
                // You can also retrieve other details like tags, output data, etc.
            }
        }
    }

    private fun cancelWorkRequestById(context: Context, workRequestId: UUID) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelWorkById(workRequestId)
    }

    private fun deleteWorks() {
        var workRequestId = UUID.fromString("70a066bd-bee9-4f99-9b16-60f282cce34f")

        cancelWorkRequestById(this, workRequestId)

        workRequestId = UUID.fromString("7412119b-a216-442e-b6f0-170a64ae326a")
        cancelWorkRequestById(this, workRequestId)

        workRequestId = UUID.fromString("95487208-36b9-4580-a7cb-cb83e7ef864c")
        cancelWorkRequestById(this, workRequestId)
    }

    private fun getUUID() {
        var uniqueName = "GetWordsWork" // Replace with your unique name
        queryWorkRequestsByUniqueName(uniqueName, this)

        uniqueName = "GetWordsWork1"
        queryWorkRequestsByUniqueName(uniqueName, this)

        uniqueName = "GetWordsWork2"
        queryWorkRequestsByUniqueName(uniqueName, this)
    }
}