package com.jamesellerbee.taskfire.adminPortal.desktop.dal.settings

import com.jamesellerbee.taskfire.tasktracker.adminPortal.app.ui.interfaces.AppPropertiesProvider
import java.util.Properties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class DesktopAppPropertiesProvider : AppPropertiesProvider {
    private val properties = Properties()

    private val _propertiesFlow = MutableSharedFlow<Pair<String, Any>>()
    override val propertiesFlow: Flow<Pair<String, Any>> = _propertiesFlow.asSharedFlow()

    init {
        // TODO load from file
    }

    override fun get(property: String, defaultValue: Any): Any {
        return when (defaultValue) {
            is Boolean -> {
                properties.getProperty(property, defaultValue.toString()).toBooleanStrict()
            }

            else -> {
                defaultValue
            }
        }
    }

    override fun set(property: String, value: Any) {
        properties.setProperty(property, value.toString())

        CoroutineScope(SupervisorJob()).launch(Dispatchers.Default) {
            _propertiesFlow.emit(Pair(property, value))
        }

        // TODO persist
    }
}