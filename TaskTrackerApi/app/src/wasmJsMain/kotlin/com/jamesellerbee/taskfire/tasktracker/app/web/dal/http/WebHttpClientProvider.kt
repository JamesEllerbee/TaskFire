package com.jamesellerbee.taskfire.tasktracker.app.web.dal.http

import com.jamesellerbee.taskfire.app.interfaces.AppPropertiesProvider
import com.jamesellerbee.taskfire.app.interfaces.HttpClientProvider
import com.jamesellerbee.tasktracker.lib.util.ResolutionStrategy
import com.jamesellerbee.tasktracker.lib.util.ServiceLocator
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.serialization.kotlinx.json.json

class WebHttpClientProvider(serviceLocator: ServiceLocator) : HttpClientProvider {
    private val appPropertiesProvider by serviceLocator.resolveLazy<AppPropertiesProvider>(
        ResolutionStrategy.ByType(
            type = AppPropertiesProvider::class
        )
    )

    override val httpClient: HttpClient by lazy {
        HttpClient(Js) {
            install(HttpCookies)
            install(ContentNegotiation) {
                json()
            }
        }
    }
}