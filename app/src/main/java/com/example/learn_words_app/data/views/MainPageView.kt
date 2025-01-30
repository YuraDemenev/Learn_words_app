package com.example.learn_words_app.data.views

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
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
import com.example.learn_words_app.data.additionalData.convertDateToTimestamp
import com.example.learn_words_app.data.dataBase.Words
import com.example.learn_words_app.data.interfaces.MainPageContract
import com.example.learn_words_app.data.presenters.MainPagePresenter
import com.example.learn_words_app.data.proto.convertLevelsToProtoLevels
import com.example.learn_words_app.databinding.FragmentLearnWordsBinding
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        thisContext: Context,
        checkExplanation: Boolean
    ): Boolean {
        val word = listOfWords[indexWord]
        var englishWord = word.englishWord
        var russianWord = word.russianTranslation
        var checkEnglishExplanation = false
        var checkRussianExplanation = false

        if (checkExplanation) {
            deleteExplanations(binding, thisContext)
        }

        //Если в английском слове есть '(),' значит есть пояснение, пояснение нужно вынести отдельно
        if (englishWord.contains("(")) {
            //Символ & означает что не надо переносить значение в () в пояснение
            if (englishWord[englishWord.length - 1] == '&') {
                englishWord = englishWord.dropLast(1)

            } else {
                checkEnglishExplanation = true

                val splitWords = word.englishWord.split("(")
                englishWord = splitWords[0]
                englishWord.trim()
                if (splitWords.size == 2) {
                    addEnglishExplanation(binding, splitWords[1], thisContext)
                } else {
                    Log.e(
                        "MainPageView",
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

            } else {

                checkRussianExplanation = true
                val splitWords = word.russianTranslation.split("(")
                russianWord = splitWords[0]
                russianWord.trim()
                if (splitWords.size == 2) {
                    addRussianExplanation(binding, splitWords[1], thisContext)
                } else {
                    Log.e(
                        "MainPageView",
                        "nextWord, splitWords russian, size!=2, russian word: $russianWord"
                    )
                    throw Exception()
                }
            }
        }

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

        return checkEnglishExplanation || checkRussianExplanation
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

    override fun deleteExplanations(
        binding: FragmentLearnWordsBinding,
        thisContext: Context
    ) {
        val englishContainer: ConstraintLayout? =
            binding.root.findViewById(R.id.scrollViewWithTableContainerEnglish)

        if (englishContainer != null) {
            val parent = englishContainer.parent as ViewGroup
            parent.removeView(englishContainer)

            //привязываем word and transcription container
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
                binding.guideline.id,
                ConstraintSet.BOTTOM,
                convertDpToPx(thisContext, 40f)
            )
            constraintSet.applyTo(constraintLayout)
        }

        val russianContainer: ConstraintLayout? =
            binding.root.findViewById(R.id.scrollViewWithTableContainerRussian)

        russianContainer?.let {
            val parent = russianContainer.parent as ViewGroup
            parent.removeView(russianContainer)
        }
    }

    override fun createAlertChoseCountLearningWords(
        user: User,
        thisContext: Context,
        inflater: LayoutInflater,
        presenter: MainPagePresenter
    ) {
        var checkChose = false
        val myScope = CoroutineScope(Dispatchers.IO)

        val builder = AlertDialog.Builder(thisContext)
        val dialogView = inflater.inflate(R.layout.alert_choose_count_learning_words, null)

        builder.setView(dialogView)

        // Create and show the dialog
        val dialog: AlertDialog = builder.create()
        //Чтобы alert был без заднего фона
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        //Добавляем listener на закрытие alert
        dialog.setOnDismissListener {
            if (checkChose) {
                myScope.launch {
                    //Обновляем proto user data
                    val listOfProtoLevels = convertLevelsToProtoLevels(user.listOfLevels)
                    val emptyList: List<Int> = listOf()

                    presenter.updateUserProto(
                        thisContext,
                        user,
                        listOfProtoLevels,
                        emptyList,
                        user.convertDateToTimestamp()
                    )
                }
            }
        }

        //Создаем alert
        dialog.show()

        if (user.countLearningWords == 10) {
            dialog.findViewById<MaterialCardView>(R.id.tenWords)
                .setBackgroundColor(Color.GREEN)
        }

        //TODO Добавить цвет для кнопок при нажатии и при открытие alert
        //Нажатие на 5
        dialog.findViewById<MaterialCardView>(R.id.fiveWords).setOnClickListener {
            checkChose = true
            user.countLearningWords = 5
        }

        //Нажатие на 10
        dialog.findViewById<MaterialCardView>(R.id.tenWords).setOnClickListener {
            checkChose = true
            user.countLearningWords = 10
        }

        //Нажатие на 15
        dialog.findViewById<MaterialCardView>(R.id.fifteenWords).setOnClickListener {
            checkChose = true
            user.countLearningWords = 15
        }

        //Нажатие на 20
        dialog.findViewById<MaterialCardView>(R.id.twentyWords).setOnClickListener {
            checkChose = true
            user.countLearningWords = 20
        }

        //Нажатие на 25
        dialog.findViewById<MaterialCardView>(R.id.twentyFiveWords).setOnClickListener {
            checkChose = true
            user.countLearningWords = 25
        }

        //Нажатие на 30
        dialog.findViewById<MaterialCardView>(R.id.thirtyWords).setOnClickListener {
            checkChose = true
            user.countLearningWords = 30
        }

        //Нажатие на 35
        dialog.findViewById<MaterialCardView>(R.id.thirtyFiveWords).setOnClickListener {
            checkChose = true
            user.countLearningWords = 35
        }

        //Нажатие на 40
        dialog.findViewById<MaterialCardView>(R.id.fortyWords).setOnClickListener {
            checkChose = true
            user.countLearningWords = 40
        }

        //Нажатие на 45
        dialog.findViewById<MaterialCardView>(R.id.fortyFiveWords).setOnClickListener {
            checkChose = true
            user.countLearningWords = 45
        }

        //Нажатие на 50
        dialog.findViewById<MaterialCardView>(R.id.fiftyWords).setOnClickListener {
            checkChose = true
            user.countLearningWords = 50
        }
    }

    @SuppressLint("SetTextI18n")
    private fun addEnglishExplanation(
        binding: FragmentLearnWordsBinding,
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

        constraintParams.startToStart = binding.learnWordsLayoutInCard.id
        constraintParams.endToEnd = binding.learnWordsLayoutInCard.id
        constraintParams.bottomToTop = binding.guideline.id

        container.layoutParams = constraintParams
        //Add
        val layout = binding.learnWordsLayoutInCard
        layout.addView(container)

//------------------------------------------------------------------------------------------------------------------------------------------------------------------

        createExplanationContainer(
            thisContext,
            binding,
            container,
            explanations,
            "Explanation",
            R.id.scrollViewWithTableRussian
        )

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

    @SuppressLint("SetTextI18n")
    private fun addRussianExplanation(
        binding: FragmentLearnWordsBinding,
        explanations: String,
        thisContext: Context
    ) {
        //Добавляем container с scroll view и table и text view
        val container = ConstraintLayout(thisContext)
        val constraintParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        container.id = R.id.scrollViewWithTableContainerRussian

        constraintParams.startToStart = binding.learnWordsLayoutInCard.id
        constraintParams.endToEnd = binding.learnWordsLayoutInCard.id
        constraintParams.topToBottom = binding.learnWordsTranslation.id

        container.layoutParams = constraintParams
        //Add
        val layout = binding.learnWordsLayoutInCard
        layout.addView(container)

        createExplanationContainer(
            thisContext,
            binding,
            container,
            explanations,
            "Пояснение",
            R.id.scrollViewWithTableRussian
        )

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

    private fun createExplanationContainer(
        thisContext: Context,
        binding: FragmentLearnWordsBinding,
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
        textView.text = textViewText

        textView.layoutParams = constraintParams
        //Add
        container.addView(textView)
    }
}