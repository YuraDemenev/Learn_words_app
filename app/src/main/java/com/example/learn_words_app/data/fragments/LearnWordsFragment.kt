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
import com.example.learn_words_app.data.additionalData.UserViewModel
import com.example.learn_words_app.data.dataBase.MainDB
import com.example.learn_words_app.data.dataBase.Words
import com.example.learn_words_app.data.interfaces.WordCallback
import com.example.learn_words_app.data.models.MainPageModel
import com.example.learn_words_app.data.presenters.MainPagePresenter
import com.example.learn_words_app.data.proto.convertLevelsToProtoLevels
import com.example.learn_words_app.data.views.MainPageView
import com.example.learn_words_app.databinding.FragmentLearnWordsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class LearnWordsFragment : Fragment(R.layout.fragment_learn_words) {
    private lateinit var binding: FragmentLearnWordsBinding
    private val userViewModel: UserViewModel by activityViewModels()

    private var user = userViewModel.getUser()

    //Чтобы проверять было ли добавлено Explanation и если да убирать его
    private var checkExplanation = false

    //Если пользователь знает слово, меняем на true и в OnDestroy,
    // если пользователь не выучил ни 1 слова, обновляем proto data
    private var checkUserKnowWord = false

    //Список из уровней которые сейчас выбраны пользователем, для изменения UI, и работы программы
    private val flowLevelsModel: FlowLevelsModel by activityViewModels()

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

        return binding.root
    }

    //TODO Продумать ситуацию когда, запрашиваем больше слов чем есть в БД на данном уровне

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

        //Проверяем что все слова выучены
        if (user.countLearnedWordsToday == user.countLearningWords) {
            val countLearningWords = user.countLearningWords
            val countLearnedWords = user.countLearnedWordsToday

            binding.learnWordsLearnedCountNewWords.text =
                "Заучено $countLearnedWords/$countLearningWords новых слов"

            presenter.changePageToYouAllLearned(binding)
        } else {
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
                user.countLearnedWordsToday,
                hashMap,
                false,
                thisContext,
                checkExplanation
            )

//-----------------------------------------------------------------------------------------------------------------------------------------------

            //При нажатии на 'я не знаю это слово'
            //TODO Добавить красивые анимации смены слова
            binding.learnWordsIDontKnowThisWordText.setOnClickListener {
                if (user.countLearnedWordsToday < countLearningWords - 1) {
                    indexWord++
                    user.countLearnedWordsToday++

                    checkExplanation = presenter.nextWord(
                        binding,
                        user,
                        listOfNewWords,
                        indexWord,
                        listOfWords,
                        user.countLearnedWordsToday,
                        hashMap,
                        true,
                        thisContext,
                        checkExplanation
                    )

                } else if (user.countLearnedWordsToday == countLearningWords - 1) {
                    indexWord++
                    //Добавляем слово в список, новых слов
                    listOfNewWords.add(listOfWords[indexWord])

                    //Увеличиваем кол-во выученных слов
                    user.countLearnedWordsToday++

                    //Проверяем есть ли сейчас explanation container на экране
                    if (checkExplanation) {
                        presenter.deleteExplanations(binding, thisContext)
                    }

                    val countLearningWordsText = user.countLearningWords
                    binding.learnWordsLearnedCountNewWords.text =
                        "Заучено $countLearningWordsText/$countLearningWords новых слов"

                    //Для изменения названия уровня
                    presenter.changeLevelName(binding, hashMap, listOfWords, indexWord)

                    //Меняем на странице ui элементы для ситуации когда всё выучено
                    presenter.changePageToYouAllLearned(binding)
                }
            }

//-----------------------------------------------------------------------------------------------------------------------------------------------
            //При нажатии на 'я знаю это слово'
            binding.learnWordsIKnowThisWordText.setOnClickListener {
                user.countKnewWords += 1
                checkUserKnowWord = true

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
                    user.countLearnedWordsToday,
                    hashMap,
                    false,
                    thisContext,
                    checkExplanation
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (listOfNewWords.size != 0) {
            val thisContext = requireContext()
            val listOfLevelsBuilders = convertLevelsToProtoLevels(user.listOfLevels)

            //Получаем/создаем БД
            val db = MainDB.getDB(thisContext)

            myScope.launch {
                presenter.updateWordsLevels(db, listOfNewWords, 1)
            }

            //Обновляем данные в user
            userViewModel.updateUser(user)

        } else if (checkUserKnowWord) {
            //Обновляем данные в user
            userViewModel.updateUser(user)
        }
    }
}