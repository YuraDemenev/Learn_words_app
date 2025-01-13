package com.example.learn_words_app.data.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.learn_words_app.MainActivity
import com.example.learn_words_app.data.additionalData.FlowLevelsModel
import com.example.learn_words_app.data.dataBase.MainDB
import com.example.learn_words_app.data.interfaces.MainPageContract
import com.example.learn_words_app.data.models.MainPageModel
import com.example.learn_words_app.data.presenters.MainPagePresenter
import com.example.learn_words_app.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainFragment : Fragment(), MainPageContract.View {
    //Список из уровней которые сейчас выбраны пользователем, для изменения UI, и работы программы
    private val flowLevelsModel: FlowLevelsModel by activityViewModels()

    //Binding
    private lateinit var binding: ActivityMainBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Получаем binding
        binding = ActivityMainBinding.inflate(inflater)
//        val myView = inflater.inflate(R.layout.activity_main, container, false)
        //Auto generated
//        enableEdgeToEdge()
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
        return binding.root
    }

    //SetTextI18n - убирает предупреждение при сложении строк (Android создает предупреждение для поддержки разных языков)
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val thisContext = requireContext()
        //Получаем/создаем БД
        val db = MainDB.getDB(thisContext)
        //Создаем presenter для MVP архитектуры
        val presenter = MainPagePresenter(MainPageModel(), this)
        //Создаем Scope для запуска корутин
        val myScope = CoroutineScope(Dispatchers.IO)

        //Проверка данных пользователя
        //TODO что-то сделать с run block??
        runBlocking {
            myScope.launch { presenter.checkUserData(thisContext, db, flowLevelsModel) }.join()
        }

        //Переход на страницу выбора тем
        binding.mainTextContainerChooseCategory.setOnClickListener {
            (requireActivity() as MainActivity).loadFragment("Levels")
        }

        //Only for DEV
        //Listener нажатия на текст UpDB
        binding.upDataBase.setOnClickListener {
            myScope.launch { presenter.clickOnUpDB(thisContext, db) }
        }

        //Listener нажатия на текст Down DB
        binding.downDataBase.setOnClickListener { presenter.clickOnDownDB(db) }

        //Listener нажатия на текст Clear user data
        binding.clearUserData.setOnClickListener {
            myScope.launch {
                presenter.clearUserData(thisContext)
            }
        }

        //Наблюдатель за FlowLevelsModel.
        //Меняем кол-во категорий
        flowLevelsModel.data.observe(viewLifecycleOwner, { levelsData ->
            //Меняем текст на UI
            binding.mainSmallTextSelectedCategories.text = "Выбрано категорий: ${levelsData.size}"
        })
    }
}