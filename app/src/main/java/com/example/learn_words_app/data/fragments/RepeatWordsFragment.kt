package com.example.learn_words_app.data.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.learn_words_app.R
import com.example.learn_words_app.data.additionalData.FlowLevelsModel
import com.example.learn_words_app.data.additionalData.User
import com.example.learn_words_app.data.additionalData.UserViewModel
import com.example.learn_words_app.data.dataBase.MainDB
import com.example.learn_words_app.data.dataBase.Words
import com.example.learn_words_app.data.models.MainPageModel
import com.example.learn_words_app.data.models.RepeatWordsModel
import com.example.learn_words_app.data.presenters.MainPagePresenter
import com.example.learn_words_app.data.presenters.RepeatWordsPresenter
import com.example.learn_words_app.data.views.MainPageView
import com.example.learn_words_app.data.views.RepeatWordsView
import com.example.learn_words_app.databinding.FragmentRepeatWordsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class RepeatWordsFragment : Fragment(R.layout.fragment_repeat_words) {
    private lateinit var binding: FragmentRepeatWordsBinding
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var user: User

    //Список из уровней которые сейчас выбраны пользователем, для изменения UI, и работы программы
    private val flowLevelsModel: FlowLevelsModel by activityViewModels()
    private var checkExplanation = false
    private val repeatWordsPresenter = RepeatWordsPresenter(RepeatWordsModel(), RepeatWordsView())
    private val mainPagePresenter = MainPagePresenter(MainPageModel(), MainPageView())


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRepeatWordsBinding.inflate(inflater)
        user = userViewModel.getUser()
        return binding.root
    }


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        userViewModel.user.observe(viewLifecycleOwner) { userObserve ->
            binding.countRepeatedWords.text =
                "Повторено ${userObserve.countRepeatedWordsToday}/${userObserve.hashMapOfWordsForRepeatAndLevelsNames.size} слов"
        }
        var listOfWords = getListOfWords(user.hashMapOfWordsForRepeatAndLevelsNames)
        if (listOfWords.isEmpty()) {
            hideCards(binding)
            binding.learnWordsDownContainer.visibility = View.INVISIBLE
            binding.transcription.visibility = View.INVISIBLE
            val wordView = binding.word
            wordView.text = "Слов для повтора нет"
            wordView.apply {
                layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    startToStart = binding.layoutInCard.id
                    endToEnd = binding.layoutInCard.id
                    topToTop = binding.guidelineInCard.id
                    bottomToBottom = binding.guidelineInCard.id
                }
            }

            return
        }
        var index = 0
        val thisContext = requireContext()
        val db = MainDB.getDB(thisContext)
        var checkWriteWord = false
        var word = listOfWords[0].first
        val myScope = CoroutineScope(Dispatchers.IO)

        //Для оптимизации
        run {
            //Получаем случайное значение, от которого зависит, какое слово покажем пользователю, русское или английское
            val randBool = getBoolean()
            lateinit var wordStr: String
            lateinit var hideWord: String
            if (randBool) {
                wordStr = word.englishWord
                hideWord = listOfWords[0].first.russianTranslation
            } else {
                wordStr = word.russianTranslation
                hideWord = listOfWords[0].first.englishWord
            }
            binding.word.text = wordStr
            binding.hideWord.text = hideWord
        }

        //Listener "Посмотреть слово"
        binding.seeWordCard.setOnClickListener {
            hideCards(binding)
            binding.hideWord.visibility = View.VISIBLE

            //Если есть объяснение
            if (checkExplanation) {
                val explanationContainer: ConstraintLayout? =
                    binding.root.findViewById(R.id.scrollViewWithTableContainerEnglish)
                if (explanationContainer != null) {
                    explanationContainer.visibility = View.VISIBLE
                }
            }
        }

        //Listener "Ввести слово"
        binding.writeWordCard.setOnClickListener {
            hideCards(binding)
            checkWriteWord = true

            repeatWordsPresenter.writeWord(
                binding,
                checkExplanation,
                index,
                listOfWords,
                thisContext,
            )
        }

        //Listener "Я не вспомнил это слово"
        binding.learnWordsIDontKnowThisWordText.setOnClickListener {
            //Чтобы удалить container ввода слова
            if (checkWriteWord) {
                val writeWordContainer: ConstraintLayout? =
                    binding.root.findViewById(R.id.repeatWordWriteWord)
                binding.layoutInCard.removeView(writeWordContainer)
            }

            checkWriteWord = false
            val checkEnglishWord = getBoolean()
            index++
            val pair = repeatWordsPresenter.nextWords(
                binding,
                checkEnglishWord,
                user,
                index,
                listOfWords,
                thisContext,
                checkExplanation,
                db
            )
            checkExplanation = pair.first
            word = pair.second

            //Когда index равен размеру листа, получаем новый лист
            if (index == listOfWords.size) {
                listOfWords = getListOfWords(user.hashMapOfWordsForRepeatAndLevelsNames)
            }
        }

        //Listener "Я вспомнил это слово"
        binding.learnWordsIKnowThisWordText.setOnClickListener {
            //Если пользователь вспомнил слово, то его не надо повторять и удаляем его из hashSet
            user.hashMapOfWordsForRepeatAndLevelsNames.remove(word)

            //Обновляем значение в БД
            myScope.launch {
                val listOfUpdateWords: List<Words> = listOf(word)
                //-1 значит в функции нынешняя stage будет получена из БД и увеличена на 1
                mainPagePresenter.updateWordsLevels(db, listOfUpdateWords, -1)
            }

            //Чтобы удалить container ввода слова
            if (checkWriteWord) {
                val writeWordContainer: ConstraintLayout? =
                    binding.root.findViewById(R.id.repeatWordWriteWord)
                binding.layoutInCard.removeView(writeWordContainer)
            }

            checkWriteWord = false
            val checkEnglishWord = getBoolean()
            index++
            val pair = repeatWordsPresenter.nextWords(
                binding,
                checkEnglishWord,
                user,
                index,
                listOfWords,
                thisContext,
                checkExplanation,
                db
            )
            checkExplanation = pair.first
            word = pair.second

        }
    }

    //Получаем из hashMap listOfWords
    private fun getListOfWords(hashMap: HashMap<Words, String>): List<Pair<Words, String>> {
        val list = ArrayList<Pair<Words, String>>(hashMap.size)
        hashMap.forEach { (word, levelName) ->
            list.add(Pair(word, levelName))
        }
        return list
    }

    private fun getBoolean(): Boolean {
        //TODO поменять на 2
        val value = Random.nextInt(1, 2)
        return value == 1
    }

    private fun hideCards(binding: FragmentRepeatWordsBinding) {
        binding.seeWordCard.visibility = View.INVISIBLE
        binding.writeWordCard.visibility = View.INVISIBLE
    }
}