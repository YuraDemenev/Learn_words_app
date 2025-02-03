package com.example.learn_words_app.data.additionalData

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {
    //Позволяет менять данные
    private val _user = MutableLiveData<User>().apply { value = User() }

    // Предоставляет только доступ к чтению
    val user: LiveData<User> get() = _user
    fun updateUser(newUser: User) {
        _user.value = newUser
    }

    fun getUser(): User {
        return _user.value ?: throw IllegalStateException("User is null")
    }
}