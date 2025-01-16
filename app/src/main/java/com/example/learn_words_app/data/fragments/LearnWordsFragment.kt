package com.example.learn_words_app.data.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.learn_words_app.MainActivity
import com.example.learn_words_app.R
import com.example.learn_words_app.data.additionalData.FlowLevelsModel
import com.example.learn_words_app.data.additionalData.FragmentsNames
import com.example.learn_words_app.data.dataBase.MainDB
import com.example.learn_words_app.data.dataBase.Words
import com.example.learn_words_app.data.interfaces.MainPageContract
import com.example.learn_words_app.data.models.MainPageModel
import com.example.learn_words_app.data.presenters.MainPagePresenter
import com.example.learn_words_app.databinding.FragmentLearnWordsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LearnWordsFragment : Fragment(R.layout.fragment_learn_words), MainPageContract.View {
    private lateinit var binding: FragmentLearnWordsBinding

    //Список из уровней которые сейчас выбраны пользователем, для изменения UI, и работы программы
    private val flowLevelsModel: FlowLevelsModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Получаем binding
        binding = FragmentLearnWordsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val thisContext = requireContext()
        //Получаем/создаем БД
        val db = MainDB.getDB(thisContext)
        val presenter = MainPagePresenter(MainPageModel(), this)
        val myScope = CoroutineScope(Dispatchers.IO)

        //Для возвращения в главное меню
        binding.learnWordsBackToMenuContainer.setOnClickListener {
            (requireActivity() as MainActivity).loadFragment(FragmentsNames.LEVELS)
        }

        //Список слов, для того чтобы предлагать пользователю новые слова
        var listOfWords = mutableListOf<Words>()
        runBlocking {
            myScope.launch {
                listOfWords = presenter.getWordsForLearn(thisContext, db, flowLevelsModel)
            }.join()
        }

        //Для сохранения тех слов которые пользователь не знает
        val listOfNewWords = mutableListOf<Words>()
        //Для итерации по listOfWords
        //TODO Продумать что будет если пользователь в середине изучения выйдет в главное меню
        var indexWord = 0
        var countLearnedWords = 0

        binding.learnWordsWord.text = listOfWords[indexWord].englishWord
        binding.learnWordsTranslation.text = listOfWords[indexWord].russianTranslation

        //При нажатии на 'я знаю это слово'
        //TODO Добавить красивые анимации смены слова
        binding.learnWordsIKnowThisWordText.setOnClickListener {
            //TODO изменить 10 на переменную
            if (countLearnedWords < 10) {
                indexWord++
                binding.learnWordsIKnowThisWordText.text = listOfWords[indexWord].englishWord
                binding.learnWordsTranslation.text = listOfWords[indexWord].russianTranslation
                
            } else {
                //Удаляем элемент перевод
                val parent = binding.learnWordsTranslation.parent as ViewGroup
                parent.removeView(binding.learnWordsTranslation)

                //Убираем margin у английского слова, чтобы разместить элемент посередине
                val layoutParams =
                    binding.learnWordsWord.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.topMargin = 0

                //Чтобы поменять start of end
                val constraintLayout = binding.learnWordsLayoutInCard
                // Create a ConstraintSet object
                val constraintSet = ConstraintSet()
                // Clone the existing constraints from the ConstraintLayout
                constraintSet.clone(constraintLayout)
                // Set the constraint
                constraintSet.connect(
                    binding.learnWordsWord.id,
                    ConstraintSet.BOTTOM,
                    binding.learnWordsDownContainer.id,
                    ConstraintSet.TOP,
                    0
                )
            }
        }

        //При нажатии на 'я не знаю это слово'
//        binding.learnWordsIDontKnowThisWordText
    }
}