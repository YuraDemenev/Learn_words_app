package com.example.learn_words_app

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.learn_words_app.data.additionalData.FragmentsNames
import com.example.learn_words_app.data.additionalData.GetWordsWork
import com.example.learn_words_app.data.fragments.LearnWordsFragment
import com.example.learn_words_app.data.fragments.LevelsFragment
import com.example.learn_words_app.data.fragments.MainFragment
import com.example.learn_words_app.data.fragments.RepeatWordsFragment
import java.util.UUID
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    override fun onDestroy() {
        //TODO Сделать сохранения прогресса пользователя в Proto data
        super.onDestroy()
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