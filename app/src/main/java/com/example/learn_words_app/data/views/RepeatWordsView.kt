package com.example.learn_words_app.data.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.example.learn_words_app.R
import com.example.learn_words_app.data.additionalData.User
import com.example.learn_words_app.data.dataBase.MainDB
import com.example.learn_words_app.data.dataBase.Words
import com.example.learn_words_app.data.interfaces.RepeatWordsContract
import com.example.learn_words_app.databinding.FragmentRepeatWordsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class RepeatWordsView : RepeatWordsContract.View {
    @SuppressLint("SetTextI18n")
    override fun nextWords(
        binding: FragmentRepeatWordsBinding,
        checkEnglishWord: Boolean,
        user: User,
        indexWord: Int,
        listOfWords: List<Pair<Words, String>>,
        thisContext: Context,
        checkExplanation: Boolean,
        db: MainDB
    ): Pair<Boolean, Words> {
        val pair = listOfWords[indexWord]
        val word = pair.first
        var englishWord = word.englishWord
        var russianWord = word.russianTranslation
        var checkExplanationCurrentWord = false

        //Возвращаем значок клавиатуры и глаза
        binding.seeWordCard.visibility = View.VISIBLE
        binding.writeWordCard.visibility = View.VISIBLE

        //Убираем скрытое слово
        binding.hideWord.visibility = View.INVISIBLE

        //Используем канал, чтобы запросить из БД значение, и не дожидаться результата
        val channel = Channel<String>()
        //Получаем название из БД
        CoroutineScope(Dispatchers.IO).launch {
            // Simulate async database fetch
            val wordLevel = db.getDao().getLevelNameById(word.levelId)
            channel.send(wordLevel) // Send the result to the channel
            channel.close() // Close the channel after sending the result
        }

        //Удаляем объяснение
        if (checkExplanation) {
            deleteExplanations(binding, thisContext)
        }

        //Если мы выводим английское слово

        //Если в английском слове есть '(),' значит есть пояснение, пояснение нужно вынести отдельно
        if (englishWord.contains("(")) {
            //Символ & означает что не надо переносить значение в () в пояснение
            if (englishWord[englishWord.length - 1] == '&') {
                englishWord = englishWord.dropLast(1)

            } else {
                checkExplanationCurrentWord = true

                val splitWords = word.englishWord.split("(")
                englishWord = splitWords[0]
                englishWord.trim()
                if (splitWords.size == 2) {
                    addExplanation(binding, splitWords[1], thisContext)
                } else {
                    Log.e(
                        "RepeatWordsView",
                        "nextWord, splitWords english, size!=2, english word: $englishWord"
                    )
                    throw Exception()
                }
            }
        }

        //Если в русском слове есть '(),' значит есть пояснение, пояснение нужно вынести отдельно
        if (russianWord.contains("(")) {
            //Символ & означает что не надо переносить значение в () в пояснение
            if (russianWord[russianWord.length - 1] == '&') {
                russianWord = russianWord.dropLast(1)

            } else if (!checkExplanationCurrentWord) {
                checkExplanationCurrentWord = true
                val splitWords = word.russianTranslation.split("(")
                russianWord = splitWords[0]
                russianWord.trim()
                if (splitWords.size == 2) {
                    addExplanation(binding, splitWords[1], thisContext)
                } else {
                    Log.e(
                        "MainPageView",
                        "nextWord, splitWords russian, size!=2, russian word: $russianWord"
                    )
                    throw Exception()
                }
            } else if (checkExplanationCurrentWord) {
                //Убираем скобки
                val russianWords = russianWord.split("(")
                russianWord = russianWords[0]
            }
        }


        //Добавляем слово
        if (checkEnglishWord) {
            binding.word.text = englishWord
            binding.hideWord.text = russianWord
        } else {
            binding.word.text = russianWord
            binding.hideWord.text = englishWord
        }

        binding.countRepeatedWords.text =
            "Повторено ${user.countRepeatedWordsToday}/${user.hashMapOfWordsForRepeatAndLevelsNames.size} слов"

        var wordLevel = ""
        //Получаем из канала wordLevel
        runBlocking {
            wordLevel = channel.receive()
        }

        wordLevel = wordLevel.replaceFirstChar { it.uppercase() }
        wordLevel = wordLevel.replace("_", " ")

        //Меняем название уровня
        binding.levelName.text = wordLevel

        return Pair(checkExplanationCurrentWord, word)
    }

    override fun writeWord(
        binding: FragmentRepeatWordsBinding,
        checkEnglishWord: Boolean,
        indexWord: Int,
        listOfWords: List<Pair<Words, String>>,
        thisContext: Context
    ) {
        lateinit var wordToCheck: String

        //Для оптимизации
        run {
            val word = listOfWords[indexWord]
            if (checkEnglishWord) {
                wordToCheck = word.first.englishWord
            } else {
                wordToCheck = word.first.russianTranslation
            }
        }

        val container = ConstraintLayout(thisContext)
        val containerParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        container.id = R.id.repeatWordWriteWord

        containerParams.startToStart = binding.layoutInCard.id
        containerParams.endToEnd = binding.layoutInCard.id
        containerParams.topToTop = binding.guidelineInCard.id
        containerParams.bottomToBottom = binding.guidelineInCard.id
        containerParams.marginStart = convertDpToPx(thisContext, 50f)
        containerParams.marginEnd = convertDpToPx(thisContext, 50f)

        container.layoutParams = containerParams

        val plainText = EditText(thisContext).apply {
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                startToStart = container.id
                endToEnd = container.id
                topToTop = container.id
                bottomToBottom = container.id
            }
            hint = "test"
            textSize = 15f
            isFocusable = true // Disable focus to make it behave like plain text
            isCursorVisible = true // Hide the cursor
            setTextColor(Color.BLACK)
        }
        val blackLine = ConstraintLayout(thisContext).apply {
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                2
            ).apply {
                containerParams.startToStart = binding.layoutInCard.id
                containerParams.endToEnd = binding.layoutInCard.id
                containerParams.topToBottom = plainText.id
                containerParams.topMargin = 1
            }
        }

        container.addView(plainText)
        container.addView(blackLine)
        val layout = binding.layoutInCard
        layout.addView(container)
    }

    private fun deleteExplanations(
        binding: FragmentRepeatWordsBinding,
        thisContext: Context
    ) {
        val englishContainer: ConstraintLayout? =
            binding.root.findViewById(R.id.scrollViewWithTableContainerEnglish)

        if (englishContainer != null) {
            val parent = englishContainer.parent as ViewGroup
            parent.removeView(englishContainer)
        }
    }

    private fun convertDpToPx(context: Context, dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }

    @SuppressLint("SetTextI18n")
    private fun addExplanation(
        binding: FragmentRepeatWordsBinding,
        explanations: String,
        thisContext: Context
    ) {
        //Добавляем container с scroll view и table и text view
        val container = ConstraintLayout(thisContext)
        val constraintParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        container.id = R.id.scrollViewWithTableContainerEnglish

        constraintParams.startToStart = binding.layoutInCard.id
        constraintParams.endToEnd = binding.layoutInCard.id
        constraintParams.topToBottom = binding.hideWordContainer.id
        constraintParams.topMargin = convertDpToPx(thisContext, 8f)

        container.layoutParams = constraintParams
        container.visibility = View.INVISIBLE
        //Add
        val layout = binding.layoutInCard
        layout.addView(container)

//------------------------------------------------------------------------------------------------------------------------------------------------------------------

        createExplanationContainer(
            thisContext,
            binding,
            container,
            explanations,
            "Explanation",
            R.id.scrollViewWithTableEnglish
        )

        //------------------------------------------------------------------------------------------------------------------------------------------------------------------

//        //Привязываем wordsContainer к container
//        val constraintLayout = binding.layoutInCard
//        // Create a ConstraintSet object
//        val constraintSet = ConstraintSet()
//        // Clone the existing constraints from the ConstraintLayout
//        constraintSet.clone(constraintLayout)
//        // Set the constraint
//        constraintSet.connect(
//            binding.learnWordsWordAndTranscriptionContainer.id,
//            ConstraintSet.BOTTOM,
//            container.id,
//            ConstraintSet.TOP,
//            convertDpToPx(thisContext, 15f)
//        )
//        constraintSet.applyTo(constraintLayout)
    }

    private fun createExplanationContainer(
        thisContext: Context,
        binding: FragmentRepeatWordsBinding,
        container: ConstraintLayout,
        explanations: String,
        textViewText: String,
        scrollViewId: Int
    ) {

        //Добавляем NestedScrollView
        val scrollView = NestedScrollView(thisContext)
        var constraintParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        constraintParams.matchConstraintMaxHeight = convertDpToPx(thisContext, 150f)
        scrollView.id = scrollViewId

        constraintParams.startToStart = binding.layoutInCard.id
        constraintParams.endToEnd = binding.layoutInCard.id
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

        table.background =
            ContextCompat.getDrawable(thisContext, R.drawable.learn_words_table_rounded_corners)

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
        textView.text = textViewText

        textView.layoutParams = constraintParams
        //Add
        container.addView(textView)
    }

}