package com.jamesellerbee.taskfire.tasktrackerapi.app.bl.routes.account

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.jamesellerbee.taskfire.tasktrackerapi.app.dal.entites.Account
import com.jamesellerbee.taskfire.tasktrackerapi.app.interfaces.AccountRepository
import com.jamesellerbee.taskfire.tasktrackerapi.app.util.ResolutionStrategy
import com.jamesellerbee.taskfire.tasktrackerapi.app.util.ServiceLocator
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import java.util.UUID

/**
 * Routes related to accounts.
 */
fun Routing.accountRoutes() {
    val serviceLocator = ServiceLocator.instance

    val accountRepository = serviceLocator.resolve<AccountRepository>(
        ResolutionStrategy.ByType(type = AccountRepository::class)
    )!!

    authenticate("auth-jwt") {
        get("/accounts") {
            val principal = call.principal<JWTPrincipal>()!!

            val accountIdClaim = principal.getClaim("accountId", String::class)

            val message = if (call.request.queryParameters["name"] == null) {
                accountRepository.getAccounts()
                    .filter { it.id == accountIdClaim }
                    .map { it.copy(password = "") }
            } else {
                accountRepository.getAccounts()
                    .filter { it.name == call.request.queryParameters["name"] }
                    .filter { it.id == accountIdClaim }
                    .map { it.copy(password = "") }
            }

            call.respond(message)
        }
    }

    post("/auth") {
        val account = call.receive<Account>()

        val existingAccount =
            accountRepository.getAccounts().firstOrNull {
                it.name == account.name
                        && it.password == account.password
            }

        if (existingAccount != null) {
            val token = JWT.create()
                .withIssuer("https://0.0.0.0:8080")
                .withAudience("https://0.0.0.0:8080")
                .withClaim("accountId", existingAccount.id)
                .sign(Algorithm.HMAC256("secret"))

            call.respond(hashMapOf("token" to token, "id" to existingAccount.id))
        } else {
            call.respond(HttpStatusCode.Unauthorized)
        }
    }

    post("/register") {
        val newAccount = call.receive<Account>()

        if (newAccount.password.isBlank()) {
            call.respond(HttpStatusCode.NotAcceptable, "Password cannot be blank")
        }

        if (accountRepository.getAccounts().none { account ->
                account.name == newAccount.name
            }) {
            val accountId = UUID.randomUUID().toString()
            val amendedAccount = newAccount.copy(id = accountId)
            accountRepository.addAccount(amendedAccount)
            call.respond(amendedAccount.copy(password = ""))
        } else {
            call.respond(HttpStatusCode.Conflict, "Account already exists with that name")
        }
    }
}