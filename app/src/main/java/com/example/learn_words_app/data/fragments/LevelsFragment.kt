package com.example.learn_words_app.data.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.learn_words_app.R
import com.example.learn_words_app.data.fragments.adapters.CardAdapter
import com.example.learn_words_app.databinding.FragmentLevelsBinding

class LevelsFragment : Fragment(R.layout.fragment_levels) {

    private lateinit var binding: FragmentLevelsBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Получаем binding
        binding = FragmentLevelsBinding.inflate(inflater)

        //Для создание списка из card_view_for_levels
        val dataSet = arrayOf(
            "Test1",
            "Test2",
            "Test3",
            "Test4",
            "Test5",
            "Test6",
            "Test7",
            "Test8",
            "Test9",
            "Test10",
            "Test11",
            "Test12",
            "Test13",
            "Test1",
            "Test2",
            "Test3",
            "Test4",
            "Test5",
            "Test6",
            "Test7",
            "Test8",
            "Test9",
            "Test10",
            "Test11",
            "Test12",
            "Test13"
        )
        val cardAdapter = CardAdapter(dataSet)

        val recyclerView: RecyclerView = binding.levelsRecyclerView
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = cardAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }
}