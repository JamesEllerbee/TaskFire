package com.jamesellerbee.taskfire.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.jamesellerbee.taskfire.app.dal.rest.RestRequestService
import com.jamesellerbee.taskfire.app.ui.auth.Login
import com.jamesellerbee.taskfire.app.ui.admin.dashboard.AdminDashboard
import com.jamesellerbee.taskfire.app.ui.dashboard.Dashboard
import com.jamesellerbee.taskfire.app.ui.theme.AppTheme
import com.jamesellerbee.tasktracker.lib.util.RegistrationStrategy
import com.jamesellerbee.tasktracker.lib.util.ServiceLocator

@Composable
fun App(serviceLocator: ServiceLocator) {
    remember {
        serviceLocator.register(
            RegistrationStrategy.Singleton(
                type = RestRequestService::class,
                service = RestRequestService(serviceLocator)
            )
        )
    }

    val isSystemInDarkTheme = isSystemInDarkTheme()
    val viewModel = remember { AppViewModel(serviceLocator, isSystemInDarkTheme) }
    val authed = viewModel.authed.collectAsState().value

    val useDarkTheme = viewModel.useDarkTheme.collectAsState().value
    AppTheme(useDarkTheme = useDarkTheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Taskfire") },
                    actions = {
                        IconButton(onClick = {

                        }) {
                            Icon(Icons.Default.Settings, "Show app context menu")
                        }
                    })
            },
            bottomBar = {
                BottomAppBar {
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = {
                        viewModel.setTheme(!useDarkTheme)
                    }) {
                        if (useDarkTheme) {
                            Icon(Icons.Filled.LightMode, "Toggle to light theme")
                        } else {
                            Icon(Icons.Filled.DarkMode, "Toggle to dark theme")
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(Modifier.fillMaxSize().padding(paddingValues)) {
                if (!authed) {
                    Login(serviceLocator, Modifier.fillMaxSize())
                } else {
                    Dashboard()
                }
            }
        }
    }
}