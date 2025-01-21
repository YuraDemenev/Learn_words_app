package com.example.learn_words_app.data.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.learn_words_app.MainActivity
import com.example.learn_words_app.R
import com.example.learn_words_app.data.additionalData.FlowLevelsModel
import com.example.learn_words_app.data.additionalData.FragmentsNames
import com.example.learn_words_app.data.additionalData.User
import com.example.learn_words_app.data.dataBase.MainDB
import com.example.learn_words_app.data.dataBase.Words
import com.example.learn_words_app.data.interfaces.MainPageContract
import com.example.learn_words_app.data.interfaces.WordCallback
import com.example.learn_words_app.data.models.MainPageModel
import com.example.learn_words_app.data.presenters.MainPagePresenter
import com.example.learn_words_app.data.proto.convertProtoLevelsToLevels
import com.example.learn_words_app.databinding.FragmentLearnWordsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class LearnWordsFragment : Fragment(R.layout.fragment_learn_words), MainPageContract.View {
    private lateinit var binding: FragmentLearnWordsBinding

    private lateinit var user: User

    //Список из уровней которые сейчас выбраны пользователем, для изменения UI, и работы программы
    private val flowLevelsModel: FlowLevelsModel by activityViewModels()

    //Для сохранения тех слов которые пользователь не знает
    private val listOfNewWords = mutableListOf<Words>()

    private val presenter = MainPagePresenter(MainPageModel(), this)
    private val myScope = CoroutineScope(Dispatchers.IO)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Получаем binding
        binding = FragmentLearnWordsBinding.inflate(inflater)

        return binding.root
    }

    //TODO Продумать ситуацию когда, запрашиваем больше слов чем есть в БД на данном уровне
    //TODO Если есть пояснение в скобках, нужно вывести в отдельной строке пояснения

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val thisContext = requireContext()
        //Получаем/создаем БД
        val db = MainDB.getDB(thisContext)

        //Для возвращения в главное меню
        binding.learnWordsBackToMenuContainer.setOnClickListener {
            (requireActivity() as MainActivity).loadFragment(FragmentsNames.MAIN)
        }

        //Для перехода на страницу повтора слов
        binding.repeatWords.setOnClickListener {
            (requireActivity() as MainActivity).loadFragment(FragmentsNames.MAIN)
        }

        //Получаем пользователя
        runBlocking {
            myScope.launch {
                user = presenter.getUser(thisContext)
            }.join()
        }

        //Получаем слова для изучения
        lateinit var pair: Pair<MutableList<Words>, HashMap<Int, String>>
        runBlocking {
            myScope.launch {
                pair = presenter.getWordsForLearn(
                    thisContext,
                    db,
                    flowLevelsModel,
                    user.countLearningWords - user.countLearnedWordsToday
                )
            }.join()
        }

        //Проверяем что все слова выучены
        if (user.countLearnedWordsToday == user.countLearningWords) {
            val countLearningWords = user.countLearningWords
            val countLearnedWords = user.countLearnedWordsToday

            binding.learnWordsLearnedCountNewWords.text =
                "Заучено $countLearnedWords/$countLearningWords новых слов"

            changePageToYouAllLearned()
        } else {

            //Список слов, для того чтобы предлагать пользователю новые слова
            val listOfWords = pair.first
            //Hash Map, чтобы получать название уровня по id
            val hashMap = pair.second

            //Для итерации по listOfWords
            var indexWord = 0

            //Кол-во слов которые нужно выучить
            val countLearningWords = user.countLearningWords

            binding.learnWordsWord.text = listOfWords[indexWord].englishWord
            binding.learnWordsTranslation.text = listOfWords[indexWord].russianTranslation

            //Для изменения названия уровня
            changeLevelName(binding, hashMap, listOfWords, indexWord)

            //Меняем кол-во изученных слов
            if (user.countLearnedWordsToday != 0) {
                val countLearnedWords = user.countLearnedWordsToday
                binding.learnWordsLearnedCountNewWords.text =
                    "Заучено $countLearnedWords/$countLearningWords новых слов"
            }

//-----------------------------------------------------------------------------------------------------------------------------------------------

            //При нажатии на 'я не знаю это слово'
            //TODO Добавить красивые анимации смены слова
            binding.learnWordsIDontKnowThisWordText.setOnClickListener {
                if (user.countLearnedWordsToday < countLearningWords - 1) {
                    indexWord++
                    user.countLearnedWordsToday++

                    nextWord(indexWord, listOfWords, user.countLearnedWordsToday, hashMap)

                } else if (user.countLearnedWordsToday == countLearningWords - 1) {
                    indexWord++
                    //Добавляем слово в список, новых слов
                    listOfNewWords.add(listOfWords[indexWord])

                    //Увеличиваем кол-во выученных слов
                    user.countLearnedWordsToday++

                    val countLearningWordsText = user.countLearningWords
                    binding.learnWordsLearnedCountNewWords.text =
                        "Заучено $countLearningWordsText/$countLearningWords новых слов"

                    //Для изменения названия уровня
                    changeLevelName(binding, hashMap, listOfWords, indexWord)

                    //Меняем на странице ui элементы для ситуации когда всё выучено
                    changePageToYouAllLearned()
                }
            }

//-----------------------------------------------------------------------------------------------------------------------------------------------
            //При нажатии на 'я знаю это слово'
            binding.learnWordsIKnowThisWordText.setOnClickListener {

                //Необходимо получить 1 новое слово из БД
                lateinit var newWord: Words
                //Делаем callback чтобы не блокировать основной поток пока получаем значение из БД
                //В этой функции также обновляем слово, которое знаем
                myScope.launch {
                    presenter.getOneWordForLearn(
                        thisContext,
                        db,
                        flowLevelsModel,
                        listOfWords[indexWord].id,
                        object : WordCallback {
                            override fun onWordReceived(words: Words) {
                                newWord = words
                                listOfWords.add(newWord)
                            }
                        })
                }

                indexWord++

                //Если мы должны получить последние слово из списка, но он ешё не пришло из БД,
                // то создаем progress bar
                if (indexWord == listOfWords.size) {
                    //Скрываем элементы
                    val wordAndTranslationContainer =
                        binding.learnWordsWordAndTranscriptionContainer
                    wordAndTranslationContainer.visibility = View.INVISIBLE

                    val translation = binding.learnWordsTranslation
                    translation.visibility = View.INVISIBLE

                    //Создаём progress bar
                    val progressBar = ProgressBar(thisContext)
                    val layoutParams = ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    )

                    layoutParams.startToStart = binding.learnWordsLayoutInCard.id
                    layoutParams.endToEnd = binding.learnWordsLayoutInCard.id
                    layoutParams.topToBottom = binding.learnWordsHighContainer.id
                    layoutParams.bottomToTop = binding.learnWordsDownContainer.id
                    progressBar.layoutParams = layoutParams

                    //Add progressBar
                    val layout = binding.learnWordsLayoutInCard
                    layout.addView(progressBar)

                    //TODO сделать что-то с while
                    while (indexWord == listOfWords.size) {

                    }

                    //Удаляем progress bar
                    layout.removeView(progressBar)
                    //Делаем видимыми элементы
                    wordAndTranslationContainer.visibility = View.VISIBLE
                    translation.visibility = View.VISIBLE

                }
                nextWord(indexWord, listOfWords, user.countLearnedWordsToday, hashMap)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val thisContext = requireContext()
        //Получаем/создаем БД
        val db = MainDB.getDB(thisContext)

        myScope.launch {
            presenter.updateWordsLevels(db, listOfNewWords, 1)
        }
        val listOfLevelsBuilders = convertProtoLevelsToLevels(user.listOfLevels)

        //Обновляем данные в user proto
        myScope.launch {
            presenter.updateUserProto(thisContext, user, listOfLevelsBuilders)
        }
    }

    private fun changeLevelName(
        binding: FragmentLearnWordsBinding,
        hashMap: HashMap<Int, String>,
        listOfWords: MutableList<Words>,
        indexWord: Int
    ) {
        //Для изменения названия уровня
        val str = hashMap[listOfWords[indexWord].levelId]
        if (str != null) {
            binding.levelName.text = changeWordForShow(str)
        } else {
            Log.e("Learn words fragment", "changeLevelName, str is null")
            throw Exception()
        }
    }

    private fun changeWordForShow(word: String): String {
        var myWord = word
        if (word.contains("_")) {
            myWord = word.replace("_", " ")
        }
        myWord = myWord.replaceFirstChar { it.uppercase() }
        return myWord
    }

    //Меняем текущее слово на следующее
    @SuppressLint("SetTextI18n")
    private fun nextWord(
        indexWord: Int,
        listOfWords: MutableList<Words>,
        countLearnedWords: Int,
        hashMap: HashMap<Int, String>
    ) {
        binding.learnWordsWord.text = listOfWords[indexWord].englishWord
        binding.learnWordsTranslation.text = listOfWords[indexWord].russianTranslation
        val countLearningWords = user.countLearningWords

        binding.learnWordsLearnedCountNewWords.text =
            "Заучено $countLearnedWords/$countLearningWords новых слов"

        binding.levelName.text = hashMap[listOfWords[indexWord].id]

        //Для изменения названия уровня
        changeLevelName(binding, hashMap, listOfWords, indexWord)

        //Добавляем слово в список, новых слов
        listOfNewWords.add(listOfWords[indexWord])
    }

    //Меняем на странице ui элементы для ситуации когда всё выучено
    private fun changePageToYouAllLearned() {
        //Удаляем элемент перевод
        var parent = binding.learnWordsTranslation.parent as ViewGroup
        parent.removeView(binding.learnWordsTranslation)

        //Удаляем элемент транскрипция
        parent = binding.transcription.parent as ViewGroup
        parent.removeView(binding.transcription)

        //Удаляем элемент я знаю это слово
        parent = binding.learnWordsIKnowThisWordText.parent as ViewGroup
        parent.removeView(binding.learnWordsIKnowThisWordText)

        //Удаляем элемент я не знаю это слово
        parent = binding.learnWordsIDontKnowThisWordText.parent as ViewGroup
        parent.removeView(binding.learnWordsIDontKnowThisWordText)

        //Убираем margin у английского слова, чтобы разместить элемент посередине
        val layoutParams =
            binding.learnWordsWord.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.topMargin = 0

        //Чтобы поменять start of на end of
        val constraintLayout = binding.learnWordsLayoutInCard
        // Create a ConstraintSet object
        val constraintSet = ConstraintSet()
        // Clone the existing constraints from the ConstraintLayout
        constraintSet.clone(constraintLayout)
        // Set the constraint
        constraintSet.connect(
            binding.learnWordsWordAndTranscriptionContainer.id,
            ConstraintSet.TOP,
            binding.guideline.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.applyTo(constraintLayout)

        binding.learnWordsWord.text = "Вы выучили все слова на сегодня"
    }
}