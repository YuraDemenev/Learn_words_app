package com.example.learn_words_app

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.learn_words_app.data.fragments.LevelsFragment
import com.example.learn_words_app.data.fragments.MainFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.empty_fragment)
        loadFragment("Main")

    }

    fun loadFragment(name: String) {
        val manager = supportFragmentManager.beginTransaction()
        when (name) {
            "Main" -> {
                manager.replace(R.id.empty_fragment_container, MainFragment()).commit()
            }

            "Levels" -> {
                manager.replace(R.id.empty_fragment_container, LevelsFragment()).commit()
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