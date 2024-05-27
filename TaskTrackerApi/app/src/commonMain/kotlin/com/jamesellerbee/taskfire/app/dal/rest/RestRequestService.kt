package com.jamesellerbee.taskfire.app.dal.rest

import com.jamesellerbee.taskfire.app.interfaces.AppPropertiesProvider
import com.jamesellerbee.taskfire.app.interfaces.HttpClientProvider
import com.jamesellerbee.tasktracker.lib.entities.Account
import com.jamesellerbee.tasktracker.lib.entities.Task
import com.jamesellerbee.tasktracker.lib.interfaces.MultiplatformLogger
import com.jamesellerbee.tasktracker.lib.util.ResolutionStrategy
import com.jamesellerbee.tasktracker.lib.util.ServiceLocator
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class Request {
    data class Auth(val username: String, val password: String, val authResult: (Boolean) -> Unit) : Request()

    data class Register(
        val email: String,
        val username: String,
        val password: String,
        val callback: (Boolean) -> Unit
    ) : Request()

    data class GetTasks(val accountId: String, val callback: (List<Task>, Boolean) -> Unit) : Request()
    data class GetAccounts(val callback: (List<Account>) -> Unit) : Request()
    data class UpdateAccount(val updatedAccount: Account, val onComplete: (Boolean) -> Unit) : Request()
    data class DeleteAccount(val accountId: String, val onComplete: (Boolean) -> Unit) : Request()
}

class RestRequestService(serviceLocator: ServiceLocator) {
    private val logger by serviceLocator.resolveLazy<MultiplatformLogger>(
        ResolutionStrategy.ByType(type = MultiplatformLogger::class)
    )

    private val httpClientProvider by serviceLocator.resolveLazy<HttpClientProvider>(
        ResolutionStrategy.ByType(type = HttpClientProvider::class)
    )

    private val appPropertiesProvider by serviceLocator.resolveLazy<AppPropertiesProvider>(
        ResolutionStrategy.ByType(
            type = AppPropertiesProvider::class
        )
    )

    private val _isAuthed = MutableStateFlow(false)
    val isAuthed = _isAuthed.asStateFlow()

    private val _accountId = MutableStateFlow("")
    val accountId = _accountId.asStateFlow()

    private val domain by lazy { appPropertiesProvider.get(AppPropertiesProvider.DOMAIN, "") }
    private val client get() = httpClientProvider.httpClient

    private val requests = Channel<Request>(BUFFERED)

    init {
        CoroutineScope(SupervisorJob()).launch {
            while (isActive) {
                val request = requests.receive()
                try {
                    when (request) {
                        is Request.Auth -> {
                            val response = client.post("${domain}/auth") {
                                header("Access-Control-Allow-Origin", "*")
                                contentType(ContentType.Application.Json)
                                setBody(Account(name = request.username, password = request.password))
                            }

                            logger.info(this::class.simpleName!!, "auth response: $response")

                            if (response.status.isSuccess()) {
                                val responseBody = response.body<Map<String, String>>()
                                _accountId.value = responseBody["id"] ?: ""
                            }

                            _isAuthed.value = response.status.isSuccess()
                            request.authResult(response.status.isSuccess())
                        }

                        is Request.Register -> {
                            val response = client.post("${domain}/register") {
                                header("Access-Control-Allow-Origin", "*")
                                contentType(ContentType.Application.Json)
                                setBody(Account(name = request.username, email = request.email, password = request.password))
                            }

                            if(response.status.isSuccess()) {
                                request.callback(true)
                            } else {
                                request.callback(false)
                            }
                        }

                        is Request.GetTasks -> {
                            val response = client.get("${domain}/tasks/${request.accountId}") {
                                contentType(ContentType.Application.Json)
                            }

                            if (response.status.isSuccess()) {
                                request.callback(response.body(), true)
                            } else {
                                request.callback(emptyList(), false)
                            }
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

                        is Request.Register -> {
                            request.callback(false)
                        }

                        is Request.GetTasks -> {
                            request.callback(emptyList(), false)
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

    fun logout() {
        _isAuthed.value = false
        _accountId.value = ""
    }
}