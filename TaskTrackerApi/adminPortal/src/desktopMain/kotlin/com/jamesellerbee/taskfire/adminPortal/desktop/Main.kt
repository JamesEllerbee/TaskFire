package com.jamesellerbee.taskfire.adminPortal.desktop

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.jamesellerbee.taskfire.adminPortal.desktop.dal.settings.DesktopAppPropertiesProvider
import com.jamesellerbee.taskfire.tasktracker.adminPortal.app.ui.App
import com.jamesellerbee.taskfire.tasktracker.adminPortal.app.ui.interfaces.AppPropertiesProvider
import com.jamesellerbee.tasktracker.lib.interfaces.MultiplatformLogger
import com.jamesellerbee.tasktracker.lib.util.ConsoleLogger
import com.jamesellerbee.tasktracker.lib.util.RegistrationStrategy
import com.jamesellerbee.tasktracker.lib.util.ServiceLocator

fun main() = application {
    val serviceLocator = remember {
        ServiceLocator().also {
            it.register(
                RegistrationStrategy.Singleton(
                    type = AppPropertiesProvider::class,
                    service = DesktopAppPropertiesProvider()
                )
            )

            it.register(RegistrationStrategy.Singleton(type = MultiplatformLogger::class, service = ConsoleLogger()))
        }
    }

    Window(onCloseRequest = ::exitApplication, title = "Taskfire admin portal") {
        App(serviceLocator)
    }
}