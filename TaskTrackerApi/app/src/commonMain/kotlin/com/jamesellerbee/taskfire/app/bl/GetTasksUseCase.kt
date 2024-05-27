package com.jamesellerbee.taskfire.app.bl

import com.jamesellerbee.taskfire.app.dal.rest.Request
import com.jamesellerbee.taskfire.app.dal.rest.RestRequestService
import com.jamesellerbee.tasktracker.lib.entities.Task
import com.jamesellerbee.tasktracker.lib.util.ResolutionStrategy
import com.jamesellerbee.tasktracker.lib.util.ServiceLocator

class GetTasksUseCase(serviceLocator: ServiceLocator) {
    private val restRequestService by
    serviceLocator.resolveLazy<RestRequestService>(ResolutionStrategy.ByType(type = RestRequestService::class))

    operator fun invoke(accountId: String, callback: (List<Task>, Boolean) -> Unit) {
        restRequestService.enqueueRequest(request = Request.GetTasks(accountId = accountId, callback = callback))
    }
}