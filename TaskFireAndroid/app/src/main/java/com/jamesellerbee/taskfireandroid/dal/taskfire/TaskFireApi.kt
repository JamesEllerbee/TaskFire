package com.jamesellerbee.taskfireandroid.dal.taskfire

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class TaskFireApi(baseUrl: String) {
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val taskFireService = retrofit.create<TaskFireService>()

    private val _authenticated = MutableStateFlow(false)
    val authenticated = _authenticated.asStateFlow()

    private var _cookie: String? = null
    val cookie get() = _cookie!!

    private var _account: Account? = null
    val account get() = _account!!

    fun setCookie(cookie: String) {
        _cookie = cookie
        _authenticated.value = true
    }

    fun setAccount(account: Account) {
        _account = account
    }

    fun logout() {
        _authenticated.value = false
        _cookie = null
    }
}