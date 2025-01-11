package com.example.learn_words_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.learn_words_app.data.fragments.LevelsFragment
import com.example.learn_words_app.data.fragments.MainFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.empty_fragment)
        loadFragment("1")

    }

    fun loadFragment(name: String) {
        val manager = supportFragmentManager.beginTransaction()
        if (name == "1") {
            manager.replace(R.id.empty_fragment_container, MainFragment()).commit()
        } else {
            manager.replace(R.id.empty_fragment_container, LevelsFragment()).commit()
        }
    }
}