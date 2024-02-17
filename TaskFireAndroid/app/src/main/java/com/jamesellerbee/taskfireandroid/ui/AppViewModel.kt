package com.jamesellerbee.taskfireandroid.ui

import com.jamesellerbee.taskfireandroid.dal.taskfire.TaskFireApi
import com.jamesellerbee.taskfireandroid.util.ResolutionStrategy
import com.jamesellerbee.taskfireandroid.util.ServiceLocator

class AppViewModel(serviceLocator: ServiceLocator) {
    private val taskFireApi by
    serviceLocator.resolveLazy<TaskFireApi>(ResolutionStrategy.ByType(TaskFireApi::class))

    val authenticated get() = taskFireApi.authenticated
}