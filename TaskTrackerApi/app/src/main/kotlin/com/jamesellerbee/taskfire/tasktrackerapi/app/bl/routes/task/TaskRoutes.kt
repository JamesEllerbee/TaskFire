package com.jamesellerbee.taskfire.tasktrackerapi.app.bl.routes.task

import com.jamesellerbee.taskfire.tasktrackerapi.app.dal.entites.Task
import com.jamesellerbee.taskfire.tasktrackerapi.app.interfaces.TaskRepository
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
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import java.util.UUID
import org.slf4j.LoggerFactory

fun Routing.taskRoutes() {
    val logger = LoggerFactory.getLogger("taskRoutes")
    val serviceLocator = ServiceLocator.instance

    val taskRepository = serviceLocator.resolve<TaskRepository>(
        ResolutionStrategy.ByType(
            type = TaskRepository::class
        )
    )!!

    authenticate("auth-jwt") {
        get("/tasks/{accountId}") {
            val principal = call.principal<JWTPrincipal>()!!
            val accountIdClaim = principal.getClaim("accountId", String::class)
            val accountId = call.parameters["accountId"]

            if (accountId == null) {
                call.respond(HttpStatusCode.BadRequest, "An account ID was not provided")
                return@get
            }

            if (accountId != accountIdClaim) {
                logger.warn("Account id was $accountId but account id claim was $accountIdClaim")
                call.respond(HttpStatusCode.Unauthorized, "Account ID claim does not match provided account ID")
                return@get
            }

            val tasks = taskRepository.getTasksByAccountId(accountId)
            call.respond(tasks)
        }

        post(path = "/tasks/{accountId}") {
            val principal = call.principal<JWTPrincipal>()!!
            val accountIdClaim = principal.getClaim("accountId", String::class)
            val accountId = call.parameters["accountId"]

            if (accountId == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            if (accountId != accountIdClaim) {
                logger.warn("Account id was $accountId but account id claim was $accountIdClaim")
                call.respond(HttpStatusCode.Unauthorized, "Account ID claim does not match provided account ID")
                return@post
            }

            val task = call.receive<Task>()

            val amendedTask = if (task.taskId.isBlank()) {
                logger.info("Creating new task")
                task.copy(taskId = UUID.randomUUID().toString(), modified = System.currentTimeMillis())
            } else {
                logger.info("Update existing task with id ${task.taskId}")
                task.copy(modified = System.currentTimeMillis())
            }

            taskRepository.addTask(
                accountId,
                amendedTask
            )

            call.respond(HttpStatusCode.OK)
        }

        delete(path = "/tasks/{accountId}/{taskId}") {
            val principal = call.principal<JWTPrincipal>()!!
            val accountIdClaim = principal.getClaim("accountId", String::class)
            val accountId = call.parameters["accountId"]
            val taskId = call.parameters["taskId"]

            if (accountId == null || taskId == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }

            if (accountId != accountIdClaim) {
                logger.warn("Account id was $accountId but account id claim was $accountIdClaim")
                call.respond(HttpStatusCode.Unauthorized, "Account ID claim does not match provided account ID")
                return@delete
            }

            taskRepository.removeTask(accountId, taskId)
            call.respond(HttpStatusCode.OK)
        }
    }
}