package com.jamesellerbee.taskfire.tasktracker.adminPortal.app.ui.interfaces

import kotlinx.coroutines.flow.Flow

interface AppPropertiesProvider {
    companion object {
        const val APP_THEME = "appTheme"
    }

    val propertiesFlow: Flow<Pair<String, Any>>

    fun get(property: String, defaultValue: Any): Any
    fun set(property: String, value: Any)
}