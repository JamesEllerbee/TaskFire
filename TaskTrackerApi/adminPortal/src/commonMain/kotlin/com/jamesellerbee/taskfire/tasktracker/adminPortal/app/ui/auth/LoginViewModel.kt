package com.jamesellerbee.taskfire.tasktracker.adminPortal.app.ui.auth

import com.jamesellerbee.taskfire.tasktracker.adminPortal.app.dal.rest.Request
import com.jamesellerbee.taskfire.tasktracker.adminPortal.app.dal.rest.RestRequestService
import com.jamesellerbee.tasktracker.lib.util.ResolutionStrategy
import com.jamesellerbee.tasktracker.lib.util.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel(serviceLocator: ServiceLocator) {
    private val restRequestService by serviceLocator.resolveLazy<RestRequestService>(
        ResolutionStrategy.ByType(type = RestRequestService::class)
    )

    private val _isLoggingIn = MutableStateFlow(false)
    val isLoggingIn = _isLoggingIn.asStateFlow()

    private val _isLoginSuccess = MutableStateFlow<Boolean?>(null)
    val isLoginSuccess = _isLoginSuccess.asStateFlow()

    fun login(username: String, password: String) {
        _isLoggingIn.value = true
        _isLoginSuccess.value = null

        restRequestService.enqueueRequest(Request.Auth(username, password) { success ->
            _isLoggingIn.value = false
            _isLoginSuccess.value = success
        })
    }
}