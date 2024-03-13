package com.jamesellerbee.taskfire.tasktrackerapi.app.bl.routes.account

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.jamesellerbee.taskfire.tasktrackerapi.app.bl.account.AccountResetService
import com.jamesellerbee.taskfire.tasktrackerapi.app.dal.entites.Account
import com.jamesellerbee.taskfire.tasktrackerapi.app.dal.entites.Email
import com.jamesellerbee.taskfire.tasktrackerapi.app.dal.entites.Password
import com.jamesellerbee.taskfire.tasktrackerapi.app.dal.properties.ApplicationProperties
import com.jamesellerbee.taskfire.tasktrackerapi.app.dal.stmp.GoogleSmtpEmailSender
import com.jamesellerbee.taskfire.tasktrackerapi.app.interfaces.AccountRepository
import com.jamesellerbee.taskfire.tasktrackerapi.app.interfaces.AdminRepository
import com.jamesellerbee.taskfire.tasktrackerapi.app.util.ResolutionStrategy
import com.jamesellerbee.taskfire.tasktrackerapi.app.util.ServiceLocator
import io.ktor.http.Cookie
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import java.util.UUID
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.apache.commons.validator.routines.EmailValidator
import org.mindrot.jbcrypt.BCrypt
import org.slf4j.LoggerFactory
import java.security.MessageDigest

/**
 * Routes related to accounts.
 */
fun Routing.accountRoutes() {
    val logger = LoggerFactory.getLogger("accountRoutes")

    val serviceLocator = ServiceLocator.instance

    val accountRepository = serviceLocator.resolve<AccountRepository>(
        ResolutionStrategy.ByType(type = AccountRepository::class)
    )!!

    val adminRepository = serviceLocator.resolve<AdminRepository>(
        ResolutionStrategy.ByType(type = AdminRepository::class)
    )!!

    val applicationProperties = serviceLocator.resolve<ApplicationProperties>(
        ResolutionStrategy.ByType(type = ApplicationProperties::class)
    )!!

    val emailSender = serviceLocator.resolve<GoogleSmtpEmailSender>(
        ResolutionStrategy.ByType(type = GoogleSmtpEmailSender::class)
    )

    val accountResetService = serviceLocator.resolve<AccountResetService>(
        ResolutionStrategy.ByType(type = AccountResetService::class)
    )!!

    authenticate("auth-jwt") {

        get(path = "/accounts") {
            val principal = call.principal<JWTPrincipal>()!!
            val accountIdClaim = principal.getClaim("accountId", String::class)

            val message = if (call.request.queryParameters["name"] == null) {
                accountRepository.getAccounts()
                    .filter { it.id == accountIdClaim || adminRepository.isAdmin(accountIdClaim ?: "") }
                    .map { it.copy(password = "") }
            } else {
                accountRepository.getAccounts()
                    .filter { it.name == call.request.queryParameters["name"] }
                    .filter { it.id == accountIdClaim || adminRepository.isAdmin(accountIdClaim ?: "") }
                    .map { it.copy(password = "") }
            }

            call.respond(message)
        }

        /**
         * Modifies the account. Use this route to update the username or password of an account.
         */
        post(path = "/accounts/{accountId}") {
            val principal = call.principal<JWTPrincipal>()!!
            val accountIdClaim = principal.getClaim("accountId", String::class)
            val accountId = call.parameters["accountId"]
            val modifiedAccount = call.receive<Account>()

            if (modifiedAccount.password.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Password cannot be empty")
                return@post
            }

            if (accountId == null) {
                call.respond(HttpStatusCode.BadRequest, "An account ID was not provided in path")
                return@post
            }

            if (accountId != accountIdClaim && !adminRepository.isAdmin(accountIdClaim ?: "")) {
                call.respond(HttpStatusCode.Unauthorized, "Account Id claim does not permit this action")
                return@post
            }

            val account = accountRepository.getAccount(accountId)
            if (account == null) {
                call.respond(HttpStatusCode.NotFound, "No account exists with that ID.")
                return@post
            }

            accountRepository.addAccount(
                account.copy(
                    name = modifiedAccount.name,
                    password = BCrypt.hashpw(modifiedAccount.password, BCrypt.gensalt())
                )
            )

            call.respond(HttpStatusCode.OK)
        }

        delete(path = "/accounts/{accountId}") {
            val principal = call.principal<JWTPrincipal>()!!
            val accountIdClaim = principal.getClaim("accountId", String::class)
            val accountId = call.parameters["accountId"]

            if (accountId == null) {
                call.respond(HttpStatusCode.BadRequest, "An account ID was not provided in path")
                return@delete
            }

            if (accountId != accountIdClaim && !adminRepository.isAdmin(accountIdClaim ?: "")) {
                call.respond(HttpStatusCode.Unauthorized, "Account Id claim does not permit this action")
                return@delete
            }

            val account = accountRepository.getAccount(accountId)
            if (account == null) {
                call.respond(HttpStatusCode.NotFound, "No account exists with that ID.")
                return@delete
            }

            accountRepository.deleteAccount(accountId)
            call.respond(HttpStatusCode.OK)
        }
    }

    post(path = "/auth") {
        val account = call.receive<Account>()

        val existingAccount =
            accountRepository.getAccounts().firstOrNull {
                it.name == account.name
                        && BCrypt.checkpw(account.password, it.password)
            }

        if (existingAccount == null) {
            call.respond(HttpStatusCode.Unauthorized, "Account could not be found with provided data.")
            return@post
        }

        if (!existingAccount.verified) {
            call.respond(HttpStatusCode.Unauthorized, "Account has not been verified yet.")
            return@post
        }

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
                domain = applicationProperties["domain"] as String
            )
        )

        call.respond(hashMapOf("id" to existingAccount.id))
    }

    post(path = "/register") {
        val newAccount = call.receive<Account>()

        if (newAccount.name.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Name cannot be blank")
            return@post
        }

        if (newAccount.password.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Password cannot be blank")
            return@post
        }

        if (newAccount.email.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Email cannot be blank")
            return@post
        }

        if (!EmailValidator.getInstance().isValid(newAccount.email)) {
            call.respond(HttpStatusCode.BadRequest, "Invalid email address")
            return@post
        }

        if (accountRepository.getAccounts().any { account -> account.email == newAccount.email }) {
            call.respond(HttpStatusCode.Conflict, "Email already in use")
            return@post
        }

        if (accountRepository.getAccounts().any { account -> account.name == newAccount.name }) {
            call.respond(HttpStatusCode.Conflict, "Name already in use")
            return@post
        }

        val accountId = UUID.randomUUID().toString()
        logger.info("Creating new account with ID $accountId")

        val amendedAccount = newAccount.copy(
            id = accountId,
            password = BCrypt.hashpw(newAccount.password, BCrypt.gensalt()),
            created = System.currentTimeMillis()
        )

        emailSender?.let {
            CoroutineScope(SupervisorJob()).launch(CoroutineExceptionHandler { coroutineContext, throwable ->
                logger.error("Error: ", throwable)
            }) {
                emailSender.sendVerificationEmail(
                    newAccount.email,
                    "https://taskfireapi.jamesellerbee.com/verify/$accountId"
                )
            }
        }

        accountRepository.addAccount(amendedAccount)
        call.respond(amendedAccount.copy(password = ""))
    }

    get(path = "/verify/{accountId}") {
        val accountId = call.parameters["accountId"]

        if (accountId == null) {
            call.respond(HttpStatusCode.BadRequest, "An account ID was not provided in path")
            return@get
        }

        val account = accountRepository.getAccount(accountId)
        if (account == null) {
            call.respond(HttpStatusCode.NotFound, "No accounts exist with that id.")
            return@get
        }

        accountRepository.addAccount(account.copy(verified = true))
        call.respondRedirect("https://taskfire.jamesellerbee.com/")
    }

    post(path = "/passwordReset") {
        val email = call.receive<Email>()

        // send reset link to the email associated with the account id
        val account = accountRepository.getAccounts().firstOrNull { account ->
            account.email == email.email
        }

        if (account == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        // generate reset key and stash it with account reset service.
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val digest = messageDigest.digest(account.hashCode().toString().toByteArray())
        val resetKey = digest.fold("") { str, byte -> str + "%02x".format(byte) }

        accountResetService.putResetKey(account.id, resetKey)

        // generate reset link
        val resetLink = "https://taskfire.jamesellerbee.com/resetPassword/${account.id}/$resetKey"

        logger.debug("Reset link is $resetLink")

        emailSender?.let {
            CoroutineScope(SupervisorJob()).launch(CoroutineExceptionHandler { coroutineContext, throwable ->
                logger.error("Error: ", throwable)
            }) {
                emailSender.sendVerificationEmail(account.email, resetLink)
            }
        }
    }

    post(path = "/passwordReset/{accountId}/{resetKey}") {
        val password = call.receive<Password>()

        if (password.password.isBlank()) {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val accountId = call.parameters["accountId"]
        val resetKey = call.parameters["resetKey"]

        if (accountId == null) {
            logger.warn("Account was null, responding with https status code bad request")
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        if (resetKey == null) {
            logger.warn("Provided reset key was null, responding with https status code bad request")
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        // Check that the provided reset key and matches the reset key mapped to the account id
        if (accountResetService.getKey(accountId) != resetKey) {
            logger.warn("Provided reset key does not match any reset mapped to the provided account id.")
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        accountRepository.getAccount(accountId)?.let { account ->
            accountRepository.addAccount(
                account.copy(
                    password = BCrypt.hashpw(password.password, BCrypt.gensalt())
                )
            )

            logger.info("Password was updated.")

            call.respond(HttpStatusCode.OK)

        } ?: run {
            logger.warn("Could not find an existing account with the provided account id.")
            call.respond(HttpStatusCode.BadRequest)
        }
    }
}