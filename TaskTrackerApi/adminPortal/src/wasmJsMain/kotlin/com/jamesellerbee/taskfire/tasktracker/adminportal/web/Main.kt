package com.jamesellerbee.taskfire.tasktracker.adminportal.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.jamesellerbee.taskfire.tasktracker.adminPortal.app.ui.App
import com.jamesellerbee.tasktracker.lib.interfaces.MultiplatformLogger
import com.jamesellerbee.tasktracker.lib.util.ConsoleLogger
import com.jamesellerbee.tasktracker.lib.util.RegistrationStrategy
import com.jamesellerbee.tasktracker.lib.util.ResolutionStrategy
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



    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        App(serviceLocator)
    }
}