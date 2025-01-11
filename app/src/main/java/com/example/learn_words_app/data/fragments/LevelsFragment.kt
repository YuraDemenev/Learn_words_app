package com.example.learn_words_app.data.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.learn_words_app.R
import com.example.learn_words_app.data.additionalData.LevelsCardData
import com.example.learn_words_app.data.dataBase.MainDB
import com.example.learn_words_app.data.fragments.adapters.CardAdapter
import com.example.learn_words_app.data.interfaces.MainPageContract
import com.example.learn_words_app.data.models.MainPageModel
import com.example.learn_words_app.data.presenters.MainPagePresenter
import com.example.learn_words_app.databinding.FragmentLevelsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class LevelsFragment : Fragment(R.layout.fragment_levels), MainPageContract.View {

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
        val presenter = MainPagePresenter(MainPageModel(), this)
        val myScope = CoroutineScope(Dispatchers.IO)

        lateinit var arrayOfLevelsData: MutableList<LevelsCardData>

        //Получаем список из карточек
        runBlocking {
            myScope.async {
                arrayOfLevelsData = presenter.getLevelsCardData(thisContext, db)
            }.join()
        }

        //Для создание списка из card_view_for_levels
        val cardAdapter = CardAdapter(arrayOfLevelsData)

        val recyclerView: RecyclerView = binding.levelsRecyclerView
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = cardAdapter
    }
}