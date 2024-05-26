package com.jamesellerbee.taskfire.app.desktop.dal.settings

import com.jamesellerbee.taskfire.app.interfaces.AppPropertiesProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DesktopAppPropertiesProvider : AppPropertiesProvider {
    private val properties = Properties()

    private val _propertiesFlow = MutableSharedFlow<Pair<String, Any>>()
    override val propertiesFlow: Flow<Pair<String, Any>> = _propertiesFlow.asSharedFlow()

    private var saveJob: Job? = null

    init {
        CoroutineScope(SupervisorJob()).launch(Dispatchers.IO) {
            properties.load(FileInputStream(File("app.properties")))
        }
    }

    override fun <T> get(property: String, defaultValue: T): T {
        return when (defaultValue) {
            is Boolean -> {
                properties.getProperty(property, defaultValue.toString()).toBooleanStrict()
            }

            is String -> {
                properties.getProperty(property, defaultValue.toString()).toString()
            }

            else -> {
                defaultValue
            }
        } as T
    }

    override fun set(property: String, value: Any) {
        properties.setProperty(property, value.toString())

        CoroutineScope(SupervisorJob()).launch(Dispatchers.Default) {
            _propertiesFlow.emit(Pair(property, value))

            withContext(Dispatchers.IO) {
                saveJob?.cancel()
                saveJob = launch {
                    delay(3000)
                    properties.store(FileOutputStream(File("adminPortal.properties")), null)
                }
            }
        }
    }
}