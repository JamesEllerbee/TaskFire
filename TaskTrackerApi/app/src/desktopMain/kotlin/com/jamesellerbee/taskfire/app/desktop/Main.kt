package com.jamesellerbee.taskfire.app.desktop

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.jamesellerbee.taskfire.app.desktop.dal.http.DesktopHttpClientProvider
import com.jamesellerbee.taskfire.app.desktop.dal.settings.DesktopAppPropertiesProvider
import com.jamesellerbee.taskfire.app.interfaces.AppPropertiesProvider
import com.jamesellerbee.taskfire.app.interfaces.HttpClientProvider
import com.jamesellerbee.taskfire.app.ui.App
import com.jamesellerbee.tasktracker.lib.interfaces.MultiplatformLogger
import com.jamesellerbee.tasktracker.lib.util.ConsoleLogger
import com.jamesellerbee.tasktracker.lib.util.RegistrationStrategy
import com.jamesellerbee.tasktracker.lib.util.ServiceLocator

fun main(args: Array<String>) = application {
    val serviceLocator = remember {
        ServiceLocator().also { serviceLocator ->
            serviceLocator.register(
                RegistrationStrategy.Singleton(
                    type = AppPropertiesProvider::class,
                    service = DesktopAppPropertiesProvider()
                )
            )

            serviceLocator.register(
                RegistrationStrategy.Singleton(
                    type = HttpClientProvider::class,
                    service = DesktopHttpClientProvider(serviceLocator = serviceLocator)
                )
            )

            serviceLocator.register(
                RegistrationStrategy.Singleton(
                    type = MultiplatformLogger::class,
                    service = ConsoleLogger()
                )
            )
        }
    }

    Window(onCloseRequest = ::exitApplication, title = "Taskfire") {
        App(serviceLocator)
    }
}