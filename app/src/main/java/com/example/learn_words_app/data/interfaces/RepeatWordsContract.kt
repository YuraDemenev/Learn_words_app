package com.example.learn_words_app.data.interfaces

import android.content.Context
import com.example.learn_words_app.data.additionalData.User
import com.example.learn_words_app.data.dataBase.Words
import com.example.learn_words_app.databinding.FragmentLearnWordsBinding

interface RepeatWordsContract {

    //View концентрируется только на UI и пользовательском взаимодействии, а логика работы с данными вынесена в другие компоненты.
    interface View {
        fun nextWords(
            binding: FragmentLearnWordsBinding,
            user: User,
            indexWord: Int,
            listOfWords: MutableList<Words>,
            thisContext: Context,
            checkExplanation: Boolean
        )
    }

    //Управляет данными приложения.
    //Выполняет бизнес-логику, например, получает данные из базы данных, сетевого API или других источников.
    //Не знает ничего о View и UI.
    interface Model {

    }

    //Посредник между View и Model.
    //Получает действия пользователя из View и запрашивает данные у Model.
    //Возвращает обработанные данные в View.
    interface Presenter {

    }
}