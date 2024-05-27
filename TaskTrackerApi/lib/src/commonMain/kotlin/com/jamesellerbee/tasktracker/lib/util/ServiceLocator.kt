package com.jamesellerbee.tasktracker.lib.util

sealed class RegistrationStrategy(open val type: Any, open val service: Any) {
    data class Singleton(override val type: Any, override val service: Any) : RegistrationStrategy(type, service)
    data class Named(override val type: Any, val name: String, override val service: Any) :
        RegistrationStrategy(type, service)
}



sealed class ResolutionStrategy {
    data class Singleton(val type: Any) : ResolutionStrategy()
    data class ByType(val type: Any) : ResolutionStrategy()
    data class Named(val type: Any, val name: String) : ResolutionStrategy()
}

class ServiceLocator {
    private val singletonObject = Any()
    private val services = mutableMapOf<Any, MutableMap<Any, Any>>()

    fun register(registrationStrategy: RegistrationStrategy) {
        val tag = when (registrationStrategy) {
            is RegistrationStrategy.Singleton -> {
                singletonObject
            }

            is RegistrationStrategy.Named -> {
                registrationStrategy.name
            }
        }

        if (!services.containsKey(registrationStrategy.type)) {
            services[registrationStrategy.type] = mutableMapOf()
        }

        services[registrationStrategy.type]!![tag] = registrationStrategy.service
    }

    fun <T> resolve(resolutionStrategy: ResolutionStrategy): T? {
        return when (resolutionStrategy) {
            is ResolutionStrategy.Singleton -> services[resolutionStrategy.type]?.get(singletonObject) as T?
            is ResolutionStrategy.ByType -> services[resolutionStrategy.type]?.values?.firstOrNull() as T?
            is ResolutionStrategy.Named -> services[resolutionStrategy.type]?.get(resolutionStrategy.name) as T?
        }
    }

    fun remove(resolutionStrategy: ResolutionStrategy) {
        when (resolutionStrategy) {
            is ResolutionStrategy.ByType -> services.remove(resolutionStrategy.type)
            is ResolutionStrategy.Named -> services[resolutionStrategy.type]?.remove(resolutionStrategy.name)
            is ResolutionStrategy.Singleton -> services[resolutionStrategy.type]?.remove(singletonObject)
        }
    }

    fun <T> resolveAll(resolutionStrategy: ResolutionStrategy.ByType): List<T> {
        return services[resolutionStrategy.type]?.values?.mapNotNull { it as? T } ?: emptyList()
    }

    fun <T> resolveLazy(resolutionStrategy: ResolutionStrategy): Lazy<T> {
        return lazy { resolve(resolutionStrategy)!! }
    }

    companion object {
        val instance = ServiceLocator()
    }
}