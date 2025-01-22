package com.example.learn_words_app.data.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
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
            //Символ & означает что не надо переносить значение в () в пояснение
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
        explanations: String,
        thisContext: Context
    ) {
        //Добавляем container с scroll view и table и text view
        val container = ConstraintLayout(thisContext)
        var constraintParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        container.id = R.id.scrollViewWithTableContainer

        constraintParams.startToStart = binding.learnWordsLayoutInCard.id
        constraintParams.endToEnd = binding.learnWordsLayoutInCard.id
        constraintParams.bottomToTop = binding.guideline.id

        container.layoutParams = constraintParams
        //Add
        val layout = binding.learnWordsLayoutInCard
        layout.addView(container)

//------------------------------------------------------------------------------------------------------------------------------------------------------------------

        //Добавляем NestedScrollView
        val scrollView = NestedScrollView(thisContext)
        constraintParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        constraintParams.matchConstraintMaxHeight = convertDpToPx(thisContext, 150f)
        scrollView.id = R.id.scrollViewWithTable

        constraintParams.startToStart = binding.learnWordsLayoutInCard.id
        constraintParams.endToEnd = binding.learnWordsLayoutInCard.id
        constraintParams.bottomToBottom = container.id

        scrollView.layoutParams = constraintParams


        container.addView(scrollView)

        //------------------------------------------------------------------------------------------------------------------------------------------------------------------
        //Add table
        val table = TableLayout(thisContext).apply {
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
        }

        table.background = ContextCompat.getDrawable(thisContext, R.drawable.rounded_corners)

        val strings = explanations.split(",").toMutableList()
        //В последнем элементе ')' её нужно удалить
        strings[strings.size - 1] = strings[strings.size - 1].dropLast(1)

        strings.forEach { string ->
            if (string.last() == '.') {
                string.dropLast(1)
            }
            val tableRow = TableRow(thisContext).apply {
                layoutParams = TableLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = 15
                    marginEnd = 15
                }
            }


            // Add TextViews with strings to the TableRow
            val textView = TextView(thisContext).apply {
                text = string
                textSize = 15f
                // Padding for aesthetics
                setPadding(16, 16, 16, 16)
            }
            tableRow.addView(textView)

            // Добавляем row
            table.addView(tableRow)
        }

        scrollView.addView(table)

        //------------------------------------------------------------------------------------------------------------------------------------------------------------------
        //Добавляем TextView
        val textView = TextView(thisContext)
        constraintParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        constraintParams.startToStart = container.id
        constraintParams.topToTop = container.id
        constraintParams.bottomToTop = scrollView.id
        constraintParams.bottomMargin = convertDpToPx(thisContext, 5f)
        constraintParams.marginStart = convertDpToPx(thisContext, 10f)

        textView.textSize = 15f
        textView.text = "Explanation:"

        textView.layoutParams = constraintParams
        //Add
        container.addView(textView)

        //------------------------------------------------------------------------------------------------------------------------------------------------------------------

        //Привязываем wordsContainer к container
        val constraintLayout = binding.learnWordsLayoutInCard
        // Create a ConstraintSet object
        val constraintSet = ConstraintSet()
        // Clone the existing constraints from the ConstraintLayout
        constraintSet.clone(constraintLayout)
        // Set the constraint
        constraintSet.connect(
            binding.learnWordsWordAndTranscriptionContainer.id,
            ConstraintSet.BOTTOM,
            container.id,
            ConstraintSet.TOP,
            convertDpToPx(thisContext, 15f)
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

    private fun convertDpToPx(context: Context, dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }
}