package com.jamesellerbee.taskfire.tasktracker.adminportal.web.dal.settings

import com.jamesellerbee.taskfire.tasktracker.adminPortal.app.ui.interfaces.AppPropertiesProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class WebAppPropertiesProvider : AppPropertiesProvider {
    private val _propertiesFlow = MutableSharedFlow<Pair<String, Any>>()
    override val propertiesFlow: Flow<Pair<String, Any>> = _propertiesFlow.asSharedFlow()

    override fun get(property: String, defaultValue: Any): Any {
        return defaultValue
    }

    override fun set(property: String, value: Any) {
    }
}