package com.jamesellerbee.taskfire.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomAppBar
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.jamesellerbee.taskfire.app.bl.LogoutUseCase
import com.jamesellerbee.taskfire.app.dal.rest.RestRequestService
import com.jamesellerbee.taskfire.app.interfaces.OverflowTopBarAction
import com.jamesellerbee.taskfire.app.ui.auth.Login
import com.jamesellerbee.taskfire.app.ui.dashboard.Dashboard
import com.jamesellerbee.taskfire.app.ui.theme.AppTheme
import com.jamesellerbee.taskfire.app.ui.topbar.LogoutOverflowTopBarAction
import com.jamesellerbee.tasktracker.lib.util.RegistrationStrategy
import com.jamesellerbee.tasktracker.lib.util.ResolutionStrategy
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
    val overflowTopBarActions = remember { mutableStateListOf<OverflowTopBarAction>() }

    remember(authed) {
        if (authed) {
            serviceLocator.register(
                RegistrationStrategy.Named(
                    type = OverflowTopBarAction::class,
                    name = LogoutOverflowTopBarAction::class.simpleName!!,
                    service = LogoutOverflowTopBarAction(serviceLocator)
                )
            )
        } else {
            serviceLocator.remove(
                ResolutionStrategy.Named(
                    type = OverflowTopBarAction::class,
                    name = LogoutOverflowTopBarAction::class.simpleName!!
                )
            )
        }

        overflowTopBarActions.clear()
        overflowTopBarActions.addAll(serviceLocator.resolveAll(ResolutionStrategy.ByType(type = OverflowTopBarAction::class)))
    }


    val useDarkTheme = viewModel.useDarkTheme.collectAsState().value
    AppTheme(useDarkTheme = useDarkTheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Taskfire") },
                    actions = {
                        if (overflowTopBarActions.isNotEmpty()) {
                            var showMenu by remember { mutableStateOf(false) }

                            IconButton(onClick = {
                                showMenu = true
                            }) {
                                Icon(Icons.Default.MoreVert, contentDescription = null)
                            }

                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                overflowTopBarActions.forEach { action ->
                                    DropdownMenuItem(onClick = {
                                        action.onClick()
                                        showMenu = false
                                    }) {
                                        Text(action.text)
                                    }
                                }
                            }
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
                    Dashboard(serviceLocator)
                }
            }
        }
    }
}