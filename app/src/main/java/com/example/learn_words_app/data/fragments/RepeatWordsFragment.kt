package com.example.learn_words_app.data.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.learn_words_app.R
import com.example.learn_words_app.data.additionalData.FlowLevelsModel
import com.example.learn_words_app.data.additionalData.User
import com.example.learn_words_app.data.additionalData.UserViewModel
import com.example.learn_words_app.databinding.FragmentLearnWordsBinding

class RepeatWordsFragment : Fragment(R.layout.fragment_repeat_words) {
    private lateinit var binding: FragmentLearnWordsBinding
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var user: User

    //Список из уровней которые сейчас выбраны пользователем, для изменения UI, и работы программы
    private val flowLevelsModel: FlowLevelsModel by activityViewModels()
    private var checkExplanation = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Получаем binding
        binding = FragmentLearnWordsBinding.inflate(inflater)
        user = userViewModel.getUser()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
       
    }
}