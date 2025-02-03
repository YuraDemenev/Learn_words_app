package com.example.learn_words_app.data.fragments.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.learn_words_app.R
import com.example.learn_words_app.data.additionalData.FlowLevelsModel
import com.example.learn_words_app.data.additionalData.LevelsCardData

class CardAdapter(
    private val data: MutableList<LevelsCardData>,
    var flowLevelsModel: FlowLevelsModel
) :
    RecyclerView.Adapter<CardAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (Class Adapter)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val levelsName: TextView
        val countWords: TextView
        val levelsPercent: TextView
        val checkBox: CheckBox

        init {
            levelsName = view.findViewById(R.id.levelsName)
            countWords = view.findViewById(R.id.countWords)
            levelsPercent = view.findViewById(R.id.levelsPercent)
            checkBox = view.findViewById(R.id.levelsCheckBox)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_for_levels, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get an element from your dataset at this position and replace the
        // contents of the view with that element
//        holder.textTest.text = data[position]
        holder.levelsPercent.text = data[position].percentsLearned.toString() + "%"
        val countWords = data[position].countWords

        //Создаем текст кол-во слов + слов/а/о
        //Для слов <9
        if (countWords <= 9) {
            if (countWords == 1) {
                holder.countWords.text = "$countWords слово"
            } else if (countWords in 2..4) {
                holder.countWords.text = "$countWords слова"
            } else if (countWords >= 5 || countWords == 0) {
                holder.countWords.text = "$countWords слов"
            }
        } else {
            //Если число оканчивающиеся на 1, кроме 11 добавляется 0
            if (countWords.toString().last() == '1') {
                if (countWords != 11) {
                    holder.countWords.text = "$countWords слово"
                } else {
                    holder.countWords.text = "$countWords слов"
                }
                //Если число оканчивающиеся на 2, 3 кроме 12,13,14
            } else if (countWords.toString().last() == '2' || countWords.toString()
                    .last() == '3' || countWords.toString().last() == '4'
            ) {
                if (countWords != 12 && countWords != 13 && countWords != 14 && countWords.toString()
                        .last() != '4'
                ) {
                    holder.countWords.text = "$countWords слов"
                } else {
                    holder.countWords.text = "$countWords слова"
                }
                //Числа, оканчивающиеся на 5, 6, 7, 8, 9, 0 и числа от 11 до 19
            } else {
                holder.countWords.text = "$countWords слов"
            }
        }

        //Заполняем название уровня
        var levelName = data[position].levelName
        //Если уровень содержит _ то меняем на пробел**
        if (levelName.contains("_")) {
            levelName = levelName.replace("_", " ")
        }

        //Проверяем есть ли уровень в flowLevelsModel, чтобы поставить галочку в checkBox
        if (flowLevelsModel.data.value?.contains(levelName) == true) {
            holder.checkBox.isChecked = true
        }

        //1 букву в слове возводим в upperCase
        levelName = levelName.replaceFirstChar { it.uppercase() }

        holder.levelsName.text = levelName

        //TODO Если ничего не меняли в темах не обновлять Proto data
        //Когда нажали на checkBox
        holder.checkBox.setOnClickListener {
            var localLevelName = holder.levelsName.text.toString()
            //1 букву в слове возводим в lowerCase (Потому что у нас в БД все названия с lowerCase
            localLevelName = localLevelName.replaceFirstChar { it.lowercase() }

            //Если выбрали check box
            if (holder.checkBox.isChecked) {
                val hashSet = flowLevelsModel.getData()
                hashSet.add(localLevelName)
                flowLevelsModel.updateLevels(hashSet)
                
            } else {
                if (flowLevelsModel.data.value?.contains(localLevelName) == true) {
                    flowLevelsModel.data.value?.remove(localLevelName)
                } else {
                    Log.e(
                        "flow Levels Model error",
                        "Flow levels Model does`t have level name: $localLevelName"
                    )
                    throw Exception()
                }
            }
        }
    }
}