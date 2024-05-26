package com.jamesellerbee.taskfire.app.interfaces

import io.ktor.client.HttpClient

interface HttpClientProvider {
    val httpClient: HttpClient
}