package com.example.learn_words_app

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.learn_words_app.data.additionalData.FragmentsNames
import com.example.learn_words_app.data.fragments.LearnWordsFragment
import com.example.learn_words_app.data.fragments.LevelsFragment
import com.example.learn_words_app.data.fragments.MainFragment
import com.example.learn_words_app.data.fragments.RepeatWordsFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}