package com.example.learn_words_app.data.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.learn_words_app.R
import com.example.learn_words_app.databinding.FragmentLevelsBinding

class LevelsFragment : Fragment(R.layout.fragment_levels) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentLevelsBinding.inflate(layoutInflater)
    }
}