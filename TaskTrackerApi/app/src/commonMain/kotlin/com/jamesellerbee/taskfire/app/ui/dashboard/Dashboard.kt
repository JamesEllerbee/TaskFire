package com.jamesellerbee.taskfire.app.ui.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.jamesellerbee.tasktracker.lib.util.ServiceLocator

@Composable
fun Dashboard(serviceLocator: ServiceLocator, modifier: Modifier = Modifier) {
    val dashboardViewModel = remember { DashboardViewModel(serviceLocator) }

    Column {
        Text(dashboardViewModel.tasks.collectAsState().value.toString())
    }
}