package com.example.learn_words_app.data.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.example.learn_words_app.R
import com.example.learn_words_app.data.additionalData.User
import com.example.learn_words_app.data.dataBase.Words
import com.example.learn_words_app.data.interfaces.MainPageContract
import com.example.learn_words_app.databinding.FragmentLearnWordsBinding

class MainPageView : MainPageContract.View {
    @SuppressLint("SetTextI18n")
    override fun nextWord(
        binding: FragmentLearnWordsBinding,
        user: User,
        listOfNewWords: MutableList<Words>,
        indexWord: Int,
        listOfWords: MutableList<Words>,
        countLearnedWords: Int,
        hashMap: HashMap<Int, String>,
        checkAddWord: Boolean,
        thisContext: Context
    ) {
        val word = listOfWords[indexWord]
        var englishWord = word.englishWord
        var russianWord = word.russianTranslation

        //Если в английском слове есть '(),' значит есть пояснение, пояснение нужно вынести отдельно
        if (englishWord.contains("(")) {
            if (englishWord[englishWord.length - 1] == '&') {
                englishWord = englishWord.dropLast(1)

            } else {
                val splitWords = word.englishWord.split("(")
                englishWord = splitWords[0]
                englishWord.trim()
                addEnglishExplanation(binding, splitWords[1], thisContext)
            }
        }

        //Если в русском слове есть '(),' значит есть пояснение, пояснение нужно вынести отдельно
        binding.learnWordsWord.text = englishWord
        binding.learnWordsTranslation.text = russianWord
        val countLearningWords = user.countLearningWords

        binding.learnWordsLearnedCountNewWords.text =
            "Заучено $countLearnedWords/$countLearningWords новых слов"

        binding.levelName.text = hashMap[word.id]

        //Для изменения названия уровня
        changeLevelName(binding, hashMap, listOfWords, indexWord)

        if (checkAddWord) {
            //Добавляем слово в список, новых слов
            listOfNewWords.add(listOfWords[indexWord])
        }


    }

    override fun changeLevelName(
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

    //Меняем на странице ui элементы для ситуации когда всё выучено
    override fun changePageToYouAllLearned(binding: FragmentLearnWordsBinding) {
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

    @SuppressLint("SetTextI18n")
    private fun addEnglishExplanation(
        binding: FragmentLearnWordsBinding,
        explanation: String,
        thisContext: Context
    ) {


        //Создаём black line down
        val blackLineDown = LinearLayout(thisContext)
        var layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            4,
        )
        //Присваиваем id
        blackLineDown.id = R.id.blackLineDownExplanation

        //Привязываем элемент
        layoutParams.startToStart = binding.learnWordsLayoutInCard.id
        layoutParams.endToEnd = binding.learnWordsLayoutInCard.id
        layoutParams.bottomToTop = binding.guideline.id
        blackLineDown.layoutParams = layoutParams

        blackLineDown.setBackgroundColor(android.graphics.Color.BLACK)

        //добавляем black line
        val layout = binding.learnWordsLayoutInCard
        layout.addView(blackLineDown)

        //------------------------------------------------------------------------------------------------------------------------------------------------------------------

        //Создаём Container explanation
        val containerExplanation = ConstraintLayout(thisContext)
        layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        //Присваиваем id
        containerExplanation.id = R.id.containerExplanation

        //Привязываем элемент
        layoutParams.startToStart = binding.learnWordsLayoutInCard.id
        layoutParams.endToEnd = binding.learnWordsLayoutInCard.id
        layoutParams.bottomToTop = blackLineDown.id

        containerExplanation.layoutParams = layoutParams

        //добавляем container explanation
        layout.addView(containerExplanation)

        //------------------------------------------------------------------------------------------------------------------------------------------------------------------
        val explanationName = TextView(thisContext)
        layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        //Присваиваем id
        explanationName.id = R.id.explanationName

        //Привязываем элемент
        layoutParams.startToStart = containerExplanation.id
        layoutParams.topToTop = containerExplanation.id
        layoutParams.bottomToTop = R.id.explanationText

        explanationName.layoutParams = layoutParams

        //Добавляем текст
        explanationName.text = "Пояснение:"
        explanationName.textSize = 25f


        //Добавляем в контейнер explanation name
        containerExplanation.addView(explanationName)

        //------------------------------------------------------------------------------------------------------------------------------------------------------------------
        val explanationText = TextView(thisContext)
        layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        //Присваиваем id
        explanationText.id = R.id.explanationText

        //Привязываем элемент
        layoutParams.startToStart = containerExplanation.id
        layoutParams.topToBottom = explanationName.id
        layoutParams.bottomToBottom = containerExplanation.id

        explanationText.layoutParams = layoutParams

        //Добавляем текст
        val englishWord = explanation.replace(")", "")
        explanationText.text = englishWord
        explanationText.textSize = 25f
        explanationText.gravity = Gravity.START

        //Добавляем в контейнер explanation name
        containerExplanation.addView(explanationText)

//------------------------------------------------------------------------------------------------------------------------------------------------------------------
        //Создаём black line up
        val blackLineUp = LinearLayout(thisContext)
        layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            4,
        )
        //Присваиваем id
        blackLineUp.id = R.id.blackLineUpExplanation

        //Привязываем элемент
        layoutParams.startToStart = binding.learnWordsLayoutInCard.id
        layoutParams.endToEnd = binding.learnWordsLayoutInCard.id
        layoutParams.bottomToTop = containerExplanation.id
        blackLineUp.layoutParams = layoutParams

        blackLineUp.setBackgroundColor(android.graphics.Color.BLACK)

        //добавляем black line
        layout.addView(blackLineUp)

        //------------------------------------------------------------------------------------------------------------------------------------------------------------------
        //Перепривязваем container со словом

        //Чтобы поменять start of на end of
        val constraintLayout = binding.learnWordsLayoutInCard
        // Create a ConstraintSet object
        val constraintSet = ConstraintSet()
        // Clone the existing constraints from the ConstraintLayout
        constraintSet.clone(constraintLayout)
        // Set the constraint
        constraintSet.connect(
            binding.learnWordsWordAndTranscriptionContainer.id,
            ConstraintSet.BOTTOM,
            blackLineUp.id,
            ConstraintSet.TOP,
            100,
        )
        constraintSet.applyTo(constraintLayout)
    }

    private fun changeWordForShow(word: String): String {
        var myWord = word
        if (word.contains("_")) {
            myWord = word.replace("_", " ")
        }
        myWord = myWord.replaceFirstChar { it.uppercase() }
        return myWord
    }
}