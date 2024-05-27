package com.jamesellerbee.taskfire.tasktracker.app.web.dal.settings

import com.jamesellerbee.taskfire.app.interfaces.AppPropertiesProvider
import kotlinx.browser.localStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.w3c.dom.Storage
import org.w3c.dom.WindowLocalStorage
import org.w3c.dom.get
import org.w3c.dom.set
import tasktrackerapi.app.generated.resources.Res

@OptIn(ExperimentalResourceApi::class)
class WebAppPropertiesProvider : AppPropertiesProvider {
    private val properties = mutableMapOf<String, String>()
    private val _propertiesFlow = MutableSharedFlow<Pair<String, Any>>()
    override val propertiesFlow: Flow<Pair<String, Any>> = _propertiesFlow.asSharedFlow()

    init {
        CoroutineScope(SupervisorJob()).launch {
            val appSettingsBytes = Res.readBytes("app.json")
            properties.putAll(
                if (appSettingsBytes.isEmpty()) {
                    emptyMap()
                } else {
                    Json.decodeFromString<Map<String, String>>(appSettingsBytes.decodeToString())
                }
            )
        }
    }

    override fun <T> get(property: String, defaultValue: T): T {
        val result = properties[property] ?: localStorage[property] ?: return defaultValue
        return when (defaultValue) {
            is Boolean -> {
                result.toBooleanStrict()
            }

            is String -> {
                result
            }

            else -> {
                defaultValue
            }
        } as T
    }

    override fun set(property: String, value: Any) {
        localStorage[property] = value.toString()

        CoroutineScope(SupervisorJob()).launch {
            _propertiesFlow.emit(Pair(property, value))
        }
    }
}