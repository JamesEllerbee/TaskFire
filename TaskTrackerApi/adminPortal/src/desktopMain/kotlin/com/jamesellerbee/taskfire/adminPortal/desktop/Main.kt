package com.jamesellerbee.taskfire.adminPortal.desktop

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.jamesellerbee.taskfire.adminPortal.desktop.dal.http.DesktopHttpClientProvider
import com.jamesellerbee.taskfire.adminPortal.desktop.dal.settings.DesktopAppPropertiesProvider
import com.jamesellerbee.taskfire.tasktracker.adminPortal.app.ui.App
import com.jamesellerbee.taskfire.tasktracker.adminPortal.app.ui.interfaces.AppPropertiesProvider
import com.jamesellerbee.taskfire.tasktracker.adminPortal.app.ui.interfaces.HttpClientProvider
import com.jamesellerbee.tasktracker.lib.interfaces.MultiplatformLogger
import com.jamesellerbee.tasktracker.lib.util.ConsoleLogger
import com.jamesellerbee.tasktracker.lib.util.RegistrationStrategy
import com.jamesellerbee.tasktracker.lib.util.ServiceLocator
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.serialization.kotlinx.json.json
import java.nio.file.Paths

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

            serviceLocator.register(RegistrationStrategy.Singleton(type = MultiplatformLogger::class, service = ConsoleLogger()))
        }
    }

    Window(onCloseRequest = ::exitApplication, title = "Taskfire admin portal") {
        App(serviceLocator)
    }
}