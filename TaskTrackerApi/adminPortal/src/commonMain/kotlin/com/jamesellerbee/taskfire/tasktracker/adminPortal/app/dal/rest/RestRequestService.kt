package com.jamesellerbee.taskfire.tasktracker.adminPortal.app.dal.rest

import com.jamesellerbee.tasktracker.lib.entities.Account
import com.jamesellerbee.tasktracker.lib.interfaces.MultiplatformLogger
import com.jamesellerbee.tasktracker.lib.util.ResolutionStrategy
import com.jamesellerbee.tasktracker.lib.util.ServiceLocator
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class Request {
    data class Auth(val username: String, val password: String, val authResult: (Boolean) -> Unit) : Request()
    data class GetAccounts(val callback: (List<Account>) -> Unit) : Request()
    data class UpdateAccount(val updatedAccount: Account, val onComplete: (Boolean) -> Unit) : Request()
    data class DeleteAccount(val accountId: String, val onComplete: (Boolean) -> Unit) : Request()
}

class RestRequestService(serviceLocator: ServiceLocator, useLocalHost: Boolean = false) {
    private val logger by serviceLocator.resolveLazy<MultiplatformLogger>(ResolutionStrategy.ByType(type = MultiplatformLogger::class))

    private val _isAuthed = MutableStateFlow(false)
    val isAuthed = _isAuthed.asStateFlow()

    private val domain = if (useLocalHost) {
        "http://localhost:8080"
    } else {
        "https://taskfireapi.jamesellerbee.com"
    }

    private val client = HttpClient {
        install(HttpCookies)
        install(ContentNegotiation) {
            json()
        }
    }

    private val requests = Channel<Request>(BUFFERED)

    init {
        CoroutineScope(SupervisorJob()).launch(Dispatchers.Default) {
            while (isActive) {
                val request = requests.receive()
                try {
                    when (request) {
                        is Request.Auth -> {
                            val response = client.post("${domain}/auth") {
                                contentType(ContentType.Application.Json)
                                setBody(Account(name = request.username, password = request.password))
                            }

                            logger.info(this::class.simpleName!!, "auth response: $response")
                            _isAuthed.value = response.status.isSuccess()
                            request.authResult(response.status.isSuccess())
                        }

                        is Request.GetAccounts -> {
                            val response = client.get("${domain}/accounts")

                            val result = if (response.status.isSuccess()) {
                                response.body<List<Account>>()
                            } else {
                                logger.info(this::class.simpleName!!, "response status: ${response.status.value}")
                                emptyList()
                            }

                            request.callback(result)
                        }

                        is Request.DeleteAccount -> {
                            val response = client.delete("${domain}/accounts/${request.accountId}")
                            request.onComplete(response.status.isSuccess())
                        }

                        is Request.UpdateAccount -> {
                            val response = client.post("${domain}/accounts/${request.updatedAccount.id}") {
                                contentType(ContentType.Application.Json)
                                setBody(request.updatedAccount)
                            }

                            request.onComplete(response.status.isSuccess())
                        }
                    }
                } catch (ex: Exception) {
                    logger.info(this::class.simpleName!!, "error: $ex (${ex.message})\n${ex.stackTraceToString()}")
                    when (request) {
                        is Request.Auth -> {
                            request.authResult(false)
                        }

                        is Request.GetAccounts -> {
                            request.callback(emptyList())
                        }

                        is Request.DeleteAccount -> {
                            request.onComplete(false)
                        }

                        is Request.UpdateAccount -> {
                            request.onComplete(false)
                        }
                    }
                }
            }
        }
    }

    fun enqueueRequest(request: Request) {
        requests.trySend(request)
    }
}