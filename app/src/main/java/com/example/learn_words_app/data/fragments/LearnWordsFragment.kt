package com.example.learn_words_app.data.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.learn_words_app.MainActivity
import com.example.learn_words_app.R
import com.example.learn_words_app.data.additionalData.FlowLevelsModel
import com.example.learn_words_app.data.additionalData.FragmentsNames
import com.example.learn_words_app.data.additionalData.User
import com.example.learn_words_app.data.additionalData.UserViewModel
import com.example.learn_words_app.data.dataBase.MainDB
import com.example.learn_words_app.data.dataBase.Words
import com.example.learn_words_app.data.interfaces.WordCallback
import com.example.learn_words_app.data.models.MainPageModel
import com.example.learn_words_app.data.presenters.MainPagePresenter
import com.example.learn_words_app.data.views.MainPageView
import com.example.learn_words_app.databinding.FragmentLearnWordsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Instant


class LearnWordsFragment : Fragment(R.layout.fragment_learn_words) {
    private lateinit var binding: FragmentLearnWordsBinding
    private val userViewModel: UserViewModel by activityViewModels()

    //Список из уровней которые сейчас выбраны пользователем, для изменения UI, и работы программы
    private val flowLevelsModel: FlowLevelsModel by activityViewModels()

    private lateinit var user: User

    //Чтобы проверять было ли добавлено Explanation и если да убирать его
    private var checkExplanation = false

    //Для сохранения тех слов которые пользователь не знает
    private val listOfNewWords = mutableListOf<Words>()

    private val presenter = MainPagePresenter(MainPageModel(), MainPageView())
    private val myScope = CoroutineScope(Dispatchers.IO)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Получаем binding
        binding = FragmentLearnWordsBinding.inflate(inflater)
        user = userViewModel.getUser()
        return binding.root
    }

    //TODO Продумать ситуацию когда, запрашиваем больше слов чем есть в БД на данном уровне

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val thisContext = requireContext()
        //Получаем/создаем БД
        val db = MainDB.getDB(thisContext)
        val countWordsForLearn = user.countLearningWords
        var countLearnedWordsInSession = 0

        //Для возвращения в главное меню
        binding.learnWordsBackToMenuContainer.setOnClickListener {
            (requireActivity() as MainActivity).loadFragment(FragmentsNames.MAIN)
        }

        //Для перехода на страницу повтора слов
        binding.repeatWords.setOnClickListener {
            (requireActivity() as MainActivity).loadFragment(FragmentsNames.REPEAT_WORDS)
        }


        //Если пользователь уже выучил все слова
        if (user.checkLearnedAllWordsToday) {
            presenter.changePageToYouAllLearned(binding)
            val progressBar = binding.progressBar
            progressBar.progress = 100
            return
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

        //Список слов, для того чтобы предлагать пользователю новые слова
        val listOfWords = pair.first
        //Hash Map, чтобы получать название уровня по id
        val hashMap = pair.second

        //Для итерации по listOfWords
        var indexWord = 0

        //Кол-во слов которые нужно выучить
        val countLearningWords = user.countLearningWords

        checkExplanation = presenter.nextWord(
            binding,
            user,
            listOfNewWords,
            indexWord,
            listOfWords,
            hashMap,
            false,
            thisContext,
            checkExplanation,
            countLearnedWordsInSession,
            countWordsForLearn
        )


//-----------------------------------------------------------------------------------------------------------------------------------------------

        //При нажатии на 'я не знаю это слово'
        //TODO Добавить красивые анимации смены слова
        binding.learnWordsIDontKnowThisWordText.setOnClickListener {
            if (user.countLearnedWordsToday < countLearningWords - 1) {
                indexWord++
                user.countLearnedWordsToday++
                userViewModel.updateCountLearnedWordsToday(user.countLearnedWordsToday)

                checkExplanation = presenter.nextWord(
                    binding,
                    user,
                    listOfNewWords,
                    indexWord,
                    listOfWords,
                    hashMap,
                    true,
                    thisContext,
                    checkExplanation,
                    countLearnedWordsInSession,
                    countWordsForLearn
                )
                countLearnedWordsInSession++

            } else if (user.countLearnedWordsToday == countLearningWords - 1) {
                indexWord++
                //Добавляем слово в список, новых слов
                listOfNewWords.add(listOfWords[indexWord])

                //Увеличиваем кол-во выученных слов
                user.countLearnedWordsToday++
                userViewModel.updateCountLearnedWordsToday(user.countLearnedWordsToday)

                //Проверяем есть ли сейчас explanation container на экране
                if (checkExplanation) {
                    presenter.deleteExplanations(binding, thisContext)
                }

                //Для изменения названия уровня
                presenter.changeLevelName(binding, hashMap, listOfWords, indexWord)

                //Меняем на странице ui элементы для ситуации когда всё выучено
                presenter.changePageToYouAllLearned(binding)

                //Меняем данные в пользователе
                user.checkLearnedAllWordsToday = true
                user.lastTimeLearnedWords = Instant.now()
                userViewModel.updateUser(user)
                
                countLearnedWordsInSession++
                val progressBar = binding.progressBar
                progressBar.progress =
                    (countLearnedWordsInSession / countWordsForLearn.toFloat() * 100).toInt()
            }
        }

//-----------------------------------------------------------------------------------------------------------------------------------------------
        //При нажатии на 'я знаю это слово'
        binding.learnWordsIKnowThisWordText.setOnClickListener {
            user.countKnewWords += 1

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
            //TODO nextWord убрать user.countLearnedWordsToday
            checkExplanation = presenter.nextWord(
                binding,
                user,
                listOfNewWords,
                indexWord,
                listOfWords,
                hashMap,
                false,
                thisContext,
                checkExplanation,
                countLearnedWordsInSession,
                countWordsForLearn
            )
        }
    }

    override fun onPause() {
        super.onPause()

//        if (user.countLearnedWordsToday == user.countLearningWords) {
//            user.checkLearnedAllWordsToday = true
//            user.lastTimeLearnedWords = Instant.now()
//        }

        if (listOfNewWords.size != 0) {
            val thisContext = requireContext()
            //Получаем/создаем БД
            val db = MainDB.getDB(thisContext)

            myScope.launch {
                presenter.updateWordsLevels(db, listOfNewWords, 1)
            }
        }

//        //Обновляем данные в user
//        CoroutineScope(Dispatchers.Main).launch {
//            userViewModel.updateUser(user)
//        }
        //Обновляем данные в user
        userViewModel.updateUser(user)

    }
}