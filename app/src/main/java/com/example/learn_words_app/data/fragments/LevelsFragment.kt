package com.example.learn_words_app.data.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.proto.LevelsProto
import com.example.learn_words_app.MainActivity
import com.example.learn_words_app.R
import com.example.learn_words_app.data.additionalData.FlowLevelsModel
import com.example.learn_words_app.data.additionalData.FragmentsNames
import com.example.learn_words_app.data.additionalData.LevelsCardData
import com.example.learn_words_app.data.additionalData.convertDateToTimestamp
import com.example.learn_words_app.data.dataBase.MainDB
import com.example.learn_words_app.data.fragments.adapters.CardAdapter
import com.example.learn_words_app.data.models.MainPageModel
import com.example.learn_words_app.data.presenters.MainPagePresenter
import com.example.learn_words_app.data.views.MainPageView
import com.example.learn_words_app.databinding.FragmentLevelsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LevelsFragment : Fragment(R.layout.fragment_levels) {
    private val flowLevelsModel: FlowLevelsModel by activityViewModels()
    private lateinit var binding: FragmentLevelsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Получаем binding
        binding = FragmentLevelsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val thisContext = requireContext()
        //Получаем/создаем БД
        val db = MainDB.getDB(thisContext)
        val presenter = MainPagePresenter(MainPageModel(), MainPageView())
        val myScope = CoroutineScope(Dispatchers.IO)

        lateinit var arrayOfLevelsData: MutableList<LevelsCardData>

        //Получаем список из карточек
        runBlocking {
            myScope.async {
                arrayOfLevelsData = presenter.getLevelsCardData(thisContext, db)
            }.join()
        }

        //Удаляем последний элемент и ставим его на 1 место (Карточка Свои слова должна быть 1 элементом)
        val lastElement = arrayOfLevelsData.removeAt(arrayOfLevelsData.size - 1)
        arrayOfLevelsData.add(0, lastElement)
        //Переименовываем с Your_Words на Ваши слова
        arrayOfLevelsData[0].levelName = "Ваши слова"

        //TODO Убрать костыль в виде пустой карточки (Почему -то последний элемент не отображается)
        //TODO Переделать порядок уровней в recycler view
        arrayOfLevelsData.add(LevelsCardData("", 0, 0))
        //TODO Сделать изменение в FlowLevels при выборе тем
        //Для создание списка из card_view_for_levels
        val cardAdapter = CardAdapter(arrayOfLevelsData, flowLevelsModel)

        val recyclerView: RecyclerView = binding.levelsRecyclerView
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = cardAdapter

        //Для возвращения в главное меню
        binding.backToMainMenuContainer.setOnClickListener {
            //TODO Сделать alert если выбрано 0 категорий
            runBlocking {
                myScope.launch {
                    val user = presenter.getUser(thisContext, db)

                    //Для добавления в Proto data levelsList
                    val listOfLevelsBuilders = mutableListOf<LevelsProto>()
                    //Проверка что flow levels model не null
                    if (flowLevelsModel.data.value == null) {
                        Log.e(
                            "flow levels model is null",
                            "LevelsFragment, backToMainMenuContainer, flow levels model is null"
                        )
                    }
                    //Получаем уровни из Flow level
                    val curLevels = flowLevelsModel.data.value?.toList()
                    if (curLevels == null) {
                        Log.e(
                            "current levels are null",
                            "LevelsFragment, backToMainMenuContainer, current levels are null"
                        )
                        throw Exception()
                    } else {
                        //Проходим по list Levels и заполняем List LevelsProto
                        curLevels.forEach { level ->
                            listOfLevelsBuilders.add(
                                LevelsProto.newBuilder().setId(0).setName(level).build()
                            )
                        }
                    }

                    val emptyList: List<Int> = listOf()

                    presenter.updateUserProto(
                        thisContext,
                        user,
                        listOfLevelsBuilders,
                        emptyList,
                        user.convertDateToTimestamp()
                    )
                }.join()
            }
            (requireActivity() as MainActivity).loadFragment(FragmentsNames.MAIN)
        }
    }
}