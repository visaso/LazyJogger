package com.example.lazyjogger.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class UserModel(application: Application): AndroidViewModel(application) {

    private val users: LiveData<List<User>> = UserDB.get(getApplication()).userDao().getAll()

    fun getUsers() = users
}