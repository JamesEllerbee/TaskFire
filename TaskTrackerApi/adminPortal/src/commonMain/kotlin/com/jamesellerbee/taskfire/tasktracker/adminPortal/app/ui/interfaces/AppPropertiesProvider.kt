package com.jamesellerbee.taskfire.tasktracker.adminPortal.app.ui.interfaces

import kotlinx.coroutines.flow.Flow

interface AppPropertiesProvider {
    companion object {
        const val USE_DARK_MODE = "useDarkMode"
        const val DOMAIN = "domain"
        const val ALLOW_SELF_SIGNED_CERT = "allowSelfSignedCert"
    }

    val propertiesFlow: Flow<Pair<String, Any>>

    fun <T> get(property: String, defaultValue: T): T
    fun set(property: String, value: Any)
}