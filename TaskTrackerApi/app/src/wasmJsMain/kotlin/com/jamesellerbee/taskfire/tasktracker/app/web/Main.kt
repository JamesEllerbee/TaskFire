package com.jamesellerbee.taskfire.tasktracker.app.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.jamesellerbee.taskfire.app.interfaces.AppPropertiesProvider
import com.jamesellerbee.taskfire.app.interfaces.HttpClientProvider
import com.jamesellerbee.taskfire.app.ui.App
import com.jamesellerbee.taskfire.app.ui.auth.Login
import com.jamesellerbee.taskfire.tasktracker.app.web.dal.http.WebHttpClientProvider
import com.jamesellerbee.taskfire.tasktracker.app.web.dal.settings.WebAppPropertiesProvider
import com.jamesellerbee.tasktracker.lib.interfaces.MultiplatformLogger
import com.jamesellerbee.tasktracker.lib.util.ConsoleLogger
import com.jamesellerbee.tasktracker.lib.util.RegistrationStrategy
import com.jamesellerbee.tasktracker.lib.util.ServiceLocator

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val serviceLocator = ServiceLocator.instance
    serviceLocator.register(
        RegistrationStrategy.Singleton(
            type = MultiplatformLogger::class,
            service = ConsoleLogger()
        )
    )

    serviceLocator.register(
        RegistrationStrategy.Singleton(
            type = AppPropertiesProvider::class,
            service = WebAppPropertiesProvider()
        )
    )

    serviceLocator.register(
        RegistrationStrategy.Singleton(
            type = HttpClientProvider::class,
            service = WebHttpClientProvider(serviceLocator)
        )
    )

    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        App(serviceLocator)
    }
}