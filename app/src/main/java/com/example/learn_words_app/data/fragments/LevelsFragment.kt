package com.example.learn_words_app.data.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.learn_words_app.R
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


    }
}