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
        //TODO Сделать проверку на ситуацию если слов для повторения нет

        //Индекс 1 потому что 0 элемент используем в блоке run
        var index = 1
        val thisContext = requireContext()
        var listOfWords = getListOfWords(user.hashMapOfWordsForRepeatAndLevelsNames)
        val db = MainDB.getDB(thisContext)
        val myScope = CoroutineScope(Dispatchers.IO)

        //Для оптимизации
        run {
            //Получаем случайное значение, от которого зависит, какое слово покажем пользователю, русское или английское
            val randBool = getBoolean()
            lateinit var word: String
            lateinit var hideWord: String
            if (randBool) {
                word = listOfWords[0].first.englishWord
                hideWord = listOfWords[0].first.russianTranslation
            } else {
                word = listOfWords[0].first.russianTranslation
                hideWord = listOfWords[0].first.englishWord
            }
            binding.word.text = word
            binding.hideWord.text = hideWord
        }

        //Listener "Посмотреть слово"
        binding.seeWordCard.setOnClickListener {
            binding.seeWordCard.visibility = View.INVISIBLE
            binding.writeWordCard.visibility = View.INVISIBLE
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
            binding.seeWordCard.visibility = View.INVISIBLE
            binding.writeWordCard.visibility = View.INVISIBLE

            repeatWordsPresenter.writeWord(
                binding,
                checkExplanation,
                index,
                listOfWords,
                thisContext
            )
        }

        //Listener "Я не вспомнил это слово"
        binding.learnWordsIDontKnowThisWordText.setOnClickListener {
            val checkEnglishWord = getBoolean()
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
//            val word = pair.second

            //Если пользователь вспомнил слово, то его не надо повторять и удаляем его из hashSet
//            if (word != null) {
//                user.hashMapOfWordsForRepeatAndLevelsNames.remove(pair.second)
//
//                //Обновляем значение в БД
//                myScope.launch {
//                    val listOfUpdateWords: List<Words> = listOf(word)
//                    //-1 значит в функции нынешняя stage будет получена из БД и увеличена на 1
//                    mainPagePresenter.updateWordsLevels(db, listOfUpdateWords, -1)
//                }
//            }

            index++
            //Когда index равен размеру листа, получаем новый лист
            if (index == listOfWords.size) {
                listOfWords = getListOfWords(user.hashMapOfWordsForRepeatAndLevelsNames)
            }
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
}