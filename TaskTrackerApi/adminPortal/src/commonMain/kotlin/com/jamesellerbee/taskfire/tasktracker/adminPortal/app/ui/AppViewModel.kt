package com.jamesellerbee.taskfire.tasktracker.adminPortal.app.ui

import com.jamesellerbee.taskfire.tasktracker.adminPortal.app.dal.rest.RestRequestService
import com.jamesellerbee.taskfire.tasktracker.adminPortal.app.ui.interfaces.AppPropertiesProvider
import com.jamesellerbee.tasktracker.lib.util.ResolutionStrategy
import com.jamesellerbee.tasktracker.lib.util.ServiceLocator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AppViewModel(serviceLocator: ServiceLocator, isSystemInDarkTheme: Boolean) {
    private val appPropertiesProvider by serviceLocator.resolveLazy<AppPropertiesProvider>(
        ResolutionStrategy.ByType(
            type = AppPropertiesProvider::class
        )
    )

    private val restRequestService by serviceLocator.resolveLazy<RestRequestService>(
        ResolutionStrategy.ByType(
            RestRequestService::class
        )
    )

    val authed = restRequestService.isAuthed

    val useDarkTheme = appPropertiesProvider.propertiesFlow.map {
        appPropertiesProvider.get(property = AppPropertiesProvider.APP_THEME, isSystemInDarkTheme) as Boolean
    }.stateIn(
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        started = SharingStarted.Eagerly,
        initialValue = appPropertiesProvider.get(
            property = AppPropertiesProvider.APP_THEME,
            isSystemInDarkTheme
        ) as Boolean
    )

    fun setTheme(useDarkTheme: Boolean) {
        appPropertiesProvider.set(AppPropertiesProvider.APP_THEME, useDarkTheme)
    }
}