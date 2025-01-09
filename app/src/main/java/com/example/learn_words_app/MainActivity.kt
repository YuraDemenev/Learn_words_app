package com.example.learn_words_app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.learn_words_app.data.dataBase.MainDB
import com.example.learn_words_app.data.interfaces.MainPageContract
import com.example.learn_words_app.data.models.MainPageModel
import com.example.learn_words_app.data.presenters.MainPagePresenter
import com.example.learn_words_app.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), MainPageContract.View {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Получаем binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        //Получаем/создаем БД
        val db = MainDB.getDB(this)
        val presenter = MainPagePresenter(MainPageModel(), this)
        //Создаем Scope для запуска корутин
        val myScope = CoroutineScope(Dispatchers.IO)
        val context = this

        //Auto generated
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Проверка данных пользователя
        myScope.launch { presenter.checkUserData(context, db) }

        //Only for DEV
        //Listener нажатия на текст UpDB
        binding.upDataBase.setOnClickListener {
            myScope.launch { presenter.clickOnUpDB(context, db) }
        }
        //Listener нажатия на текст Down DB
        binding.downDataBase.setOnClickListener { presenter.clickOnDownDB(db) }
    }
}