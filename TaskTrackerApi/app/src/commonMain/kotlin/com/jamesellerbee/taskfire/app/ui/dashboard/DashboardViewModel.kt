package com.jamesellerbee.taskfire.app.ui.dashboard

import com.jamesellerbee.taskfire.app.bl.GetLoggedInAccountIdUseCase
import com.jamesellerbee.taskfire.app.bl.GetTasksUseCase
import com.jamesellerbee.tasktracker.lib.entities.Task
import com.jamesellerbee.tasktracker.lib.util.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class DashboardViewModel(
    serviceLocator: ServiceLocator,
    val getLoggedInAccountIdUseCase: GetLoggedInAccountIdUseCase = GetLoggedInAccountIdUseCase(serviceLocator),
    val getTasksUseCase: GetTasksUseCase = GetTasksUseCase(serviceLocator)
) {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks = _tasks.asStateFlow()

    init {
        getTasksUseCase(getLoggedInAccountIdUseCase(), this::onGetTasks)
    }

    fun onGetTasks(tasks: List<Task>, success: Boolean) {
        if (!success) {
            // alert user on failure to retrieve tasks
        }

        _tasks.value = tasks
    }
}