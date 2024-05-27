package com.jamesellerbee.taskfire.app.ui.auth

import com.jamesellerbee.taskfire.app.dal.rest.Request
import com.jamesellerbee.taskfire.app.dal.rest.RestRequestService
import com.jamesellerbee.tasktracker.lib.util.ResolutionStrategy
import com.jamesellerbee.tasktracker.lib.util.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel(serviceLocator: ServiceLocator) {
    enum class Mode { LOGIN, REGISTER }

    private val restRequestService by serviceLocator.resolveLazy<RestRequestService>(
        ResolutionStrategy.ByType(type = RestRequestService::class)
    )

    private val _mode = MutableStateFlow(Mode.LOGIN)
    val mode = _mode.asStateFlow()

    private val _isLoggingIn = MutableStateFlow(false)
    val isLoggingIn = _isLoggingIn.asStateFlow()

    private val _isLoginSuccess = MutableStateFlow<Boolean?>(null)
    val isLoginSuccess = _isLoginSuccess.asStateFlow()

    private val _isRegisterSuccess = MutableStateFlow<Boolean?>(null)
    val isRegisterSuccess = _isRegisterSuccess.asStateFlow()

    fun setMode(mode: Mode) {
        _mode.value = mode
        _isLoginSuccess.value = null
        _isRegisterSuccess.value = null
        _isLoggingIn.value = false

    }

    fun login(username: String, password: String) {
        _isLoggingIn.value = true
        _isLoginSuccess.value = null

        restRequestService.enqueueRequest(Request.Auth(username, password) { success ->
            _isLoggingIn.value = false
            _isLoginSuccess.value = success
        })
    }

    fun register(email: String, username: String, password: String) {
        _isLoggingIn.value = true

        restRequestService.enqueueRequest(Request.Register(email, username, password) { success ->
            _isLoggingIn.value = false
            _isRegisterSuccess.value = success
        })
    }
}