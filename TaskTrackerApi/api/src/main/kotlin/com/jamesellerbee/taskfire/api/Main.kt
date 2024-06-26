package com.jamesellerbee.taskfire.api

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.jamesellerbee.taskfire.api.bl.account.AccountResetService
import com.jamesellerbee.taskfire.api.bl.routes.account.accountRoutes
import com.jamesellerbee.taskfire.api.bl.routes.task.taskRoutes
import com.jamesellerbee.taskfire.api.dal.properties.ApplicationProperties
import com.jamesellerbee.taskfire.api.dal.repository.account.ExposedAccountRepository
import com.jamesellerbee.taskfire.api.dal.repository.account.ExposedAdminRepository
import com.jamesellerbee.taskfire.api.dal.repository.account.InMemoryAccountRepository
import com.jamesellerbee.taskfire.api.dal.repository.account.InMemoryAdminRepository
import com.jamesellerbee.taskfire.api.dal.repository.task.ExposedTaskRepository
import com.jamesellerbee.taskfire.api.dal.repository.task.InMemoryTaskRepository
import com.jamesellerbee.taskfire.api.dal.stmp.GoogleSmtpEmailSender
import com.jamesellerbee.taskfire.api.interfaces.AccountRepository
import com.jamesellerbee.taskfire.api.interfaces.AdminRepository
import com.jamesellerbee.taskfire.api.interfaces.TaskRepository
import com.jamesellerbee.tasktracker.lib.entities.Account
import com.jamesellerbee.tasktracker.lib.util.RegistrationStrategy
import com.jamesellerbee.tasktracker.lib.util.ResolutionStrategy
import com.jamesellerbee.tasktracker.lib.util.ServiceLocator
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.auth.parseAuthorizationHeader
import io.ktor.network.tls.certificates.buildKeyStore
import io.ktor.network.tls.certificates.saveToFile
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.http.content.singlePageApplication
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.routing.routing
import java.io.File
import java.security.KeyStore
import java.util.UUID
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import org.mindrot.jbcrypt.BCrypt
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
    val parser = ArgParser("tasktrackerapi")

    val propertiesPath by parser.option(
        type = ArgType.String,
        fullName = "propertiesPath",
        shortName = "p",
        description = "Specify path to find application properties file."
    ).default("./taskfireApi.properties")

    val inMemory by parser.option(
        type = ArgType.Boolean,
        fullName = "inMemory",
        description = "Use in memory repositories",
    ).default(false)

    val noEmail by parser.option(
        type = ArgType.Boolean,
        fullName = "noEmail",
        description = "Disable email functionality"
    ).default(false)

    parser.parse(args)

    val logger = LoggerFactory.getLogger("main")
    logger.info("Starting up")
    logger.debug("Configuration: propertiesPath = {}, inMemory = {}, noEmail = {}", propertiesPath, inMemory, noEmail)

    val serviceLocator = ServiceLocator.instance

    val applicationProperties = ApplicationProperties(propertiesPath)

    serviceLocator.register(
        RegistrationStrategy.Singleton(
            type = ApplicationProperties::class,
            service = applicationProperties
        )
    )

    // Create repositories
    val accountRepository: AccountRepository = if (inMemory) {
        InMemoryAccountRepository()
    } else {
        ExposedAccountRepository(serviceLocator)
    }

    val taskRepository: TaskRepository = if (inMemory) {
        InMemoryTaskRepository()
    } else {
        ExposedTaskRepository(serviceLocator)
    }

    val adminRepository: AdminRepository = if (inMemory) {
        InMemoryAdminRepository()
    } else {
        ExposedAdminRepository(serviceLocator)
    }

    serviceLocator.register(
        RegistrationStrategy.Singleton(
            type = AccountRepository::class,
            service = accountRepository
        )
    )

    serviceLocator.register(
        RegistrationStrategy.Singleton(
            type = TaskRepository::class,
            service = taskRepository
        )
    )

    serviceLocator.register(
        RegistrationStrategy.Singleton(
            type = AdminRepository::class,
            service = adminRepository
        )
    )

    serviceLocator.register(
        RegistrationStrategy.Singleton(
            type = AccountResetService::class,
            service = AccountResetService()
        )
    )

    logger.info("Setting up SSL cert")

    // Set up keystore if it does not exist
    val keyStoreFile = File("keystore.jks")
    val keyStore = if (!keyStoreFile.exists()) {
        val keyStore = buildKeyStore {
            certificate("taskfireapi") {
                password = applicationProperties["certificatePassword"] as String
                domains = listOf("taskfireapi.jamesellerbee.com", "localhost")
            }
        }

        keyStore.saveToFile(keyStoreFile, applicationProperties["keystorePassword"] as String)
        keyStore
    } else {
        KeyStore.getInstance(keyStoreFile, (applicationProperties["keystorePassword"] as String).toCharArray())
    }

    logger.info("Setting up admin")
    val existingAdminAccount = accountRepository.getAccounts().firstOrNull() {
        it.name == applicationProperties["adminUsername"] as String
    }

    val updatedAdminAccount = if (existingAdminAccount != null
        && !BCrypt.checkpw(
            applicationProperties["adminPassword"] as String,
            existingAdminAccount.password
        )
    ) {
        logger.info("Updating admin account password")
        Account(
            name = applicationProperties["adminUsername"] as String,
            password = BCrypt.hashpw(applicationProperties["adminPassword"] as String, BCrypt.gensalt()),
            email = "",
            id = existingAdminAccount.id,
            created = existingAdminAccount.created,
            verified = true
        )
    } else if (existingAdminAccount == null) {
        logger.info("Creating admin account")
        Account(
            name = applicationProperties["adminUsername"] as String,
            password = BCrypt.hashpw(applicationProperties["adminPassword"] as String, BCrypt.gensalt()),
            email = "",
            id = UUID.randomUUID().toString(),
            created = System.currentTimeMillis(),
            verified = true
        )
    } else {
        logger.info("Admin account already up to date")
        existingAdminAccount
    }

    if (updatedAdminAccount != existingAdminAccount) {
        accountRepository.addAccount(updatedAdminAccount)
        adminRepository.addAdmin(updatedAdminAccount.id)
    }

    if (!noEmail) {
        logger.info("setting up email service")

        val emailSender = GoogleSmtpEmailSender(serviceLocator)
        serviceLocator.register(
            RegistrationStrategy.Singleton(
                type = GoogleSmtpEmailSender::class,
                service = emailSender
            )
        )
    }

    val environment = applicationEngineEnvironment {
        if(applicationProperties.get("useSsl", "true").toBooleanStrict()) {
            logger.info("Configured to use SSL")
            sslConnector(
                keyStore = keyStore,
                keyAlias = "taskfireapi",
                keyStorePassword = { (applicationProperties["keystorePassword"] as String).toCharArray() },
                privateKeyPassword = { (applicationProperties["certificatePassword"] as String).toCharArray() }) {
                port = applicationProperties.get("sslPort", "8443").toInt()
                keyStorePath = keyStoreFile
            }
        } else {
            logger.info("Configured to use plain HTTP")
            connector {
                port = applicationProperties.get("port", "8080").toInt()
            }
        }

        module(Application::module)
    }

    embeddedServer(
        factory = Netty,
        environment = environment
    ).start(wait = true)
}

fun Application.module() {
    val logger = LoggerFactory.getLogger("main")
    val serviceLocator = ServiceLocator.instance

    val applicationProperties =
        serviceLocator.resolve<ApplicationProperties>(ResolutionStrategy.ByType(type = ApplicationProperties::class))!!

    install(ContentNegotiation) {
        json()
    }

    install(CallLogging) {
        this.logger = LoggerFactory.getLogger("taskfireapi")
    }

    install(CORS) {
        anyHost()

        allowCredentials = true
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowHeader(HttpHeaders.Authorization)

        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
    }

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT.require(Algorithm.HMAC256(applicationProperties["secret"] as String))
                    .withIssuer("https://0.0.0.0")
                    .withAudience("https://0.0.0.0")
                    .build()
            )

            authHeader { call ->
                val cookieValue = call.request.cookies["Authorization"] ?: return@authHeader null

                try {
                    parseAuthorizationHeader("Bearer $cookieValue")
                } catch (ex: Exception) {
                    logger.error("Error:", ex)
                    null
                }
            }

            validate { credential ->
                if (credential.payload.getClaim("accountId").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

    routing {
        applicationProperties["openApiPath"]?.let {
            openAPI(path = "/openapi", swaggerFile = it as String)
        } ?: run {
            logger.warn("openApiPath property not set")
        }

        applicationProperties["adminPortalAppPath"]?.let { adminPortalAppPath ->
            logger.debug("Serving admin portal from path {}", adminPortalAppPath)
            singlePageApplication {
                applicationRoute = "/admin-portal"
                filesPath = adminPortalAppPath as String
            }
        } ?: run {
            logger.warn("adminPortalReactAppPath property not set")
        }

        accountRoutes()
        taskRoutes()
    }
}