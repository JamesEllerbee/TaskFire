package com.jamesellerbee.taskfire.tasktracker.adminportal.web

import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.jamesellerbee.taskfire.tasktracker.adminPortal.app.ui.App
import com.jamesellerbee.tasktracker.lib.util.ServiceLocator

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val serviceLocator = remember { ServiceLocator.instance }

    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        App(serviceLocator)
    }
}