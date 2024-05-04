package com.jamesellerbee.taskfire.tasktracker.adminPortal.app.ui.interfaces

import io.ktor.client.HttpClient

interface HttpClientProvider {
    val httpClient: HttpClient
}