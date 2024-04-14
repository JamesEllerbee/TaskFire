package com.jamesellerbee.taskfire.tasktracker.adminPortal.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jamesellerbee.taskfire.tasktracker.adminPortal.app.ui.auth.Login
import com.jamesellerbee.tasktracker.lib.util.ServiceLocator

@Composable
fun App(serviceLocator: ServiceLocator) {
    val viewModel = remember { AppViewModel() }
    val authed = viewModel.authed.collectAsState().value

    Scaffold(
        topBar = { TopAppBar(title = { Text("Taskfire admin portal") }) }
    ) { paddingValues ->
        Column(Modifier.fillMaxSize().padding(paddingValues)) {
            if (!authed) {
                Login()
            } else {
                // Show dashboard
            }
        }
    }

}