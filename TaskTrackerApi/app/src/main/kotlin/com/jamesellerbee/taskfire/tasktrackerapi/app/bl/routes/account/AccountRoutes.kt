package com.jamesellerbee.taskfire.tasktrackerapi.app.bl.routes.account

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.jamesellerbee.taskfire.tasktrackerapi.app.dal.entites.Account
import com.jamesellerbee.taskfire.tasktrackerapi.app.dal.properties.ApplicationProperties
import com.jamesellerbee.taskfire.tasktrackerapi.app.interfaces.AccountRepository
import com.jamesellerbee.taskfire.tasktrackerapi.app.util.ResolutionStrategy
import com.jamesellerbee.taskfire.tasktrackerapi.app.util.ServiceLocator
import io.ktor.http.Cookie
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
import org.mindrot.jbcrypt.BCrypt
import org.slf4j.LoggerFactory

/**
 * Routes related to accounts.
 */
fun Routing.accountRoutes() {
    val logger = LoggerFactory.getLogger("accountRoutes")

    val serviceLocator = ServiceLocator.instance

    val accountRepository = serviceLocator.resolve<AccountRepository>(
        ResolutionStrategy.ByType(type = AccountRepository::class)
    )!!

    val applicationProperties = serviceLocator.resolve<ApplicationProperties>(
        ResolutionStrategy.ByType(type = ApplicationProperties::class)
    )!!

    authenticate("auth-jwt") {

        get(path = "/accounts") {
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

    post(path = "/auth") {
        val account = call.receive<Account>()

        val existingAccount =
            accountRepository.getAccounts().firstOrNull {
                it.name == account.name
                        && BCrypt.checkpw(account.password, it.password)
            }

        if (existingAccount != null) {
            logger.info("Creating JWT token with account id claim \"${existingAccount.id}\"")
            val token = JWT.create()
                .withIssuer("https://0.0.0.0")
                .withAudience("https://0.0.0.0")
                .withClaim("accountId", existingAccount.id)
                .sign(Algorithm.HMAC256(applicationProperties["secret"] as String))


            call.response.cookies.append(
                Cookie(
                    name = "Authorization",
                    value = token,
                    path = "/",
                    httpOnly = false,
                    secure = true,
                    domain = "taskfireapi.jamesellerbee.com",
                    extensions = mapOf("SameSite" to "None", "Partitioned" to "")
                )
            )
            call.respond(hashMapOf("id" to existingAccount.id))
        } else {
            call.respond(HttpStatusCode.Unauthorized)
        }
    }

    post(path = "/register") {
        val newAccount = call.receive<Account>()

        if (newAccount.password.isBlank()) {
            call.respond(HttpStatusCode.NotAcceptable, "Password cannot be blank")
        }

        if (accountRepository.getAccounts().none { account ->
                account.name == newAccount.name
            }) {
            val accountId = UUID.randomUUID().toString()
            logger.info("Created new account with ID $accountId")

            val amendedAccount = newAccount.copy(
                id = accountId,
                password = BCrypt.hashpw(newAccount.password, BCrypt.gensalt())
            )

            accountRepository.addAccount(amendedAccount)
            call.respond(amendedAccount.copy(password = ""))
        } else {
            call.respond(HttpStatusCode.Conflict, "Account already exists with that name")
        }
    }
}