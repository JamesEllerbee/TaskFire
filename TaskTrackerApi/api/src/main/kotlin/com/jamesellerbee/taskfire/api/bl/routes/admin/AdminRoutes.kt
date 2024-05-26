package com.jamesellerbee.taskfire.api.bl.routes.admin

import com.jamesellerbee.taskfire.api.interfaces.AdminRepository
import com.jamesellerbee.tasktracker.lib.util.ResolutionStrategy
import com.jamesellerbee.tasktracker.lib.util.ServiceLocator
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get

fun Routing.adminRoutes() {
    val serviceLocator = ServiceLocator.instance

    val adminRepository = serviceLocator.resolve<AdminRepository>(
        ResolutionStrategy.ByType(type = AdminRepository::class)
    )!!

    authenticate("auth-jwt") {
        get(path = "/admin/diskSpace") {
            val principal = call.principal<JWTPrincipal>()!!
            val accountIdClaim = principal.getClaim("accountId", String::class)

            if(accountIdClaim == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }

            if(adminRepository.isAdmin(accountIdClaim)) {
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }


        }
    }
}