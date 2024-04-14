package com.jamesellerbee.tasktracker.lib.util

sealed class RegistrationStrategy {
    data class Singleton(val type: Any, val service: Any) : RegistrationStrategy()
}

sealed class ResolutionStrategy {
    data class ByType(val type: Any) : ResolutionStrategy()
}

class ServiceLocator {
    private val singletonServices = mutableMapOf<Any, Any>()

    fun register(registrationStrategy: RegistrationStrategy) {
        when (registrationStrategy) {
            is RegistrationStrategy.Singleton -> {
                singletonServices[registrationStrategy.type] = registrationStrategy.service
            }
        }
    }

    fun <T> resolve(resolutionStrategy: ResolutionStrategy): T? {
        return when (resolutionStrategy) {
            is ResolutionStrategy.ByType -> singletonServices[resolutionStrategy.type] as T?
        }
    }

    fun <T> resolveLazy(resolutionStrategy: ResolutionStrategy): Lazy<T> {
        return lazy { resolve(resolutionStrategy)!! }
    }


    companion object {
        val instance = ServiceLocator()
    }
}