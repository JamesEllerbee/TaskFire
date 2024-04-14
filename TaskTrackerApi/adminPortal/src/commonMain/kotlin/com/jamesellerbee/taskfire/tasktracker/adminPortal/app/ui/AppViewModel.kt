package com.jamesellerbee.taskfire.tasktracker.adminPortal.app.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppViewModel {
    private val _authed = MutableStateFlow(false)
    val authed = _authed.asStateFlow()
}