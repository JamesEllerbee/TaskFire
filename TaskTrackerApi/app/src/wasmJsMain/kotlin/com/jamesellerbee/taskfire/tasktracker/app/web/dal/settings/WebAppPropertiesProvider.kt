package com.jamesellerbee.taskfire.tasktracker.app.web.dal.settings

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class WebAppPropertiesProvider : AppPropertiesProvider {
    private val properties = mutableMapOf<String, Any>()

    private val _propertiesFlow = MutableSharedFlow<Pair<String, Any>>()
    override val propertiesFlow: Flow<Pair<String, Any>> = _propertiesFlow.asSharedFlow()

    override fun <T> get(property: String, defaultValue: T): T {
        return defaultValue
    }

    override fun set(property: String, value: Any) {
        properties[property] = value

        CoroutineScope(SupervisorJob()).launch(Dispatchers.Default) {
            _propertiesFlow.emit(Pair(property, value))
        }
    }
}