package com.jamesellerbee.taskfire.app.bl

import com.jamesellerbee.taskfire.app.dal.rest.RestRequestService
import com.jamesellerbee.tasktracker.lib.util.ResolutionStrategy
import com.jamesellerbee.tasktracker.lib.util.ServiceLocator

class LogoutUseCase(serviceLocator: ServiceLocator) {
    private val restRequestService by
    serviceLocator.resolveLazy<RestRequestService>(ResolutionStrategy.ByType(type = RestRequestService::class))

    operator fun invoke() {
        restRequestService.logout()
    }
}