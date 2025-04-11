package com.example.learn_words_app.data.views

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
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
import com.google.android.material.button.MaterialButton
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
        db: MainDB,
        countWordsForRepeat: Int,
        countRepeatWordsInSession: Int

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

        val progressBar = binding.progressBar
        progressBar.progress =
            (countRepeatWordsInSession / countWordsForRepeat.toFloat() * 100).toInt()

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

    @SuppressLint("SetTextI18n")
    override fun writeWord(
        binding: FragmentRepeatWordsBinding,
        checkEnglishWord: Boolean,
        indexWord: Int,
        listOfWords: List<Pair<Words, String>>,
        thisContext: Context,
    ) {
        lateinit var wordToCheck: String
        val buttonsColor = ContextCompat.getColor(thisContext, R.color.grey)
        val buttonsRedColor = ContextCompat.getColor(thisContext, R.color.red)

        //Для оптимизации
        run {
            val word = listOfWords[indexWord]
            if (checkEnglishWord) {
                wordToCheck = word.first.englishWord
            } else {
                wordToCheck = word.first.russianTranslation
            }
            val wordsToCheck = wordToCheck.split("(")
            wordToCheck = wordsToCheck[0]
        }

        //Container
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
        containerParams.marginStart = convertDpToPx(thisContext, 15f)
        containerParams.marginEnd = convertDpToPx(thisContext, 15f)

        container.layoutParams = containerParams

        //Plain text
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
            if (checkEnglishWord) {
                hint = "Введите слово"
            } else {
                hint = "Write word"
            }
            id = View.generateViewId()
            textSize = 16f
            isFocusable = true
            isCursorVisible = true
            setTextColor(Color.BLACK)
            inputType = android.text.InputType.TYPE_CLASS_TEXT
        }

        //Black Line
        val blackLine = ConstraintLayout(thisContext).apply {
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                1
            ).apply {
                startToStart = binding.layoutInCard.id
                endToEnd = binding.layoutInCard.id
                topToBottom = plainText.id
                topMargin = 1
            }
            id = View.generateViewId()
        }

        //Добавление кнопки для проверки
        val buttonConfirm = MaterialButton(thisContext).apply {
            val parentWidth = binding.layoutInCard.width
            val widthPercent = (parentWidth * 0.4).toInt()
            layoutParams = ConstraintLayout.LayoutParams(
                widthPercent,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                startToEnd = R.id.getHintWriteWord
                endToEnd = container.id
                topToBottom = blackLine.id
                topMargin = 5
                cornerRadius = convertDpToPx(thisContext, 10f)
                setBackgroundColor(buttonsColor)
            }

            id = R.id.confirmWordWriteWord
            text = "TEST Confirm"
        }
        buttonConfirm.setOnClickListener {
            //TODO Добавить плавную анимацию
            val text = plainText.text.toString()
            if (text == wordToCheck) {
                showHideWord(container, binding)
            } else {
                buttonConfirm.setBackgroundColor(buttonsRedColor)

                val translationX = PropertyValuesHolder.ofFloat(
                    "translationX",
                    -10f,
                    10f,
                    -8f,
                    8f,
                    -6f,
                    6f,
                    -4f,
                    4f,
                    -2f,
                    2f,
                    0f
                )
                val animator = ObjectAnimator.ofPropertyValuesHolder(buttonConfirm, translationX)
                //Чтобы в конце анимации поменять цвет назад
                animator.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {}

                    override fun onAnimationEnd(animation: Animator) {
                        buttonConfirm.setBackgroundColor(buttonsColor)
                    }

                    override fun onAnimationCancel(animation: Animator) {}

                    override fun onAnimationRepeat(animation: Animator) {}

                })

                animator.duration = 800 // Duration
                animator.repeatCount = 0 // No repeat

                animator.start()
                //TODO Добавить вибрацию
            }
        }

        //Добавление кнопки для получения подсказки
        val buttonGetHint = MaterialButton(thisContext).apply {
            val parentWidth = binding.layoutInCard.width
            val widthPercent = (parentWidth * 0.4).toInt()
            layoutParams = ConstraintLayout.LayoutParams(
                widthPercent,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                startToStart = container.id
                endToStart = R.id.confirmWordWriteWord
                topToBottom = blackLine.id
                topMargin = 5
                cornerRadius = convertDpToPx(thisContext, 10f)
                setBackgroundColor(buttonsColor)
            }
            id = R.id.getHintWriteWord
            text = "TEST Hint"
        }

        buttonGetHint.setOnClickListener {
            val text = plainText.text.toString()

            val spannable = SpannableString(text)
            var i = 0
            var checkFault = false
            while (i < text.length) {
                if (i >= wordToCheck.length || text[i] != wordToCheck[i]) {
                    checkFault = true
                    spannable.setSpan(
                        ForegroundColorSpan(Color.RED),
                        i,
                        i + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                i++
            }
            //Если не найдено ошибок добавляем 1 букву
            if (!checkFault && text.length != wordToCheck.length) {
                plainText.setText(wordToCheck.substring(0, i + 1))
                //Чтобы корректно сдвинуть фокус
                i++

                //Если не найдено ошибок и длина текста равна длине слова, значит слово корректное
            } else if (!checkFault && text.length == wordToCheck.length) {
                showHideWord(container, binding)
            } else {
                plainText.setText(spannable)
            }
            //Двигаем фокус
            plainText.setSelection(i)
        }

        //Add All
        container.addView(plainText)
        container.addView(blackLine)
        container.addView(buttonConfirm)
        container.addView(buttonGetHint)

        val layout = binding.layoutInCard
        layout.addView(container)
    }

    //Функция для того, чтобы отобразить спрятанные элементы, когда пользователь вспомнил слово
    private fun showHideWord(container: View, binding: FragmentRepeatWordsBinding) {
        binding.layoutInCard.removeView(container)
        binding.hideWord.visibility = View.VISIBLE

        //Если есть объяснение выводим его
        val explanationContainer: ConstraintLayout? =
            binding.root.findViewById(R.id.scrollViewWithTableContainerEnglish)
        if (explanationContainer != null) {
            explanationContainer.visibility = View.VISIBLE
        }
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

    //TODO сделать красивую анимацию
//    private fun animateGradient(button: MaterialButton, mistakes: Int, totalLength: Int) {
//        // Calculate the percentage of the button to cover
//        val mistakePercentage = mistakes.toFloat() / totalLength.toFloat()
//        val gradientWidth =
//            (mistakePercentage * 0.3f * button.width).toInt() // Limit to 30% of the button
//
//        // Create a ShapeDrawable with a gradient
//        val gradient = ShapeDrawable(object : RectShape() {
//            override fun draw(canvas: Canvas, paint: Paint) {
//                val gradientPaint = LinearGradient(
//                    0f, 0f, gradientWidth.toFloat(), 0f,
//                    Color.RED, Color.parseColor("#FFC0CB"), // Red to pink gradient
//                    Shader.TileMode.CLAMP
//                )
//                paint.shader = gradientPaint
//                canvas.drawRect(0f, 0f, gradientWidth.toFloat(), height, paint)
//            }
//        })
//
//        // Animate the gradient width dynamically
//        val animator = ValueAnimator.ofInt(0, gradientWidth)
//        animator.duration = 1000 // Animation duration (1 second)
//        animator.addUpdateListener {
//            val animatedWidth = it.animatedValue as Int
//            gradient.bounds = Rect(0, 0, animatedWidth, button.height)
//            button.background = gradient
//        }
//        animator.start()
//    }
}