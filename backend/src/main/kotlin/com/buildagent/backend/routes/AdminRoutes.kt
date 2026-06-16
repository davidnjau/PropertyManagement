package com.buildagent.backend.routes

import com.buildagent.backend.auth.AgentPrincipal
import com.buildagent.backend.services.AdminService
import com.buildagent.shared.models.ApiResponse
import com.buildagent.shared.models.CreateUserRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.adminRoutes(service: AdminService) {
    authenticate("local-auth") {
        route("/admin/users") {
            get {
                val principal = call.principal<AgentPrincipal>()!!
                require(principal.role == "ADMIN") { "Forbidden: requires ADMIN role" }
                val users = service.listUsers(principal.agencyId)
                call.respond(ApiResponse(users))
            }

            post {
                val principal = call.principal<AgentPrincipal>()!!
                require(principal.role == "ADMIN") { "Forbidden: requires ADMIN role" }
                val req = call.receive<CreateUserRequest>()
                val user = service.createUser(principal.agencyId, req)
                call.respond(HttpStatusCode.Created, ApiResponse(user))
            }
        }
    }
}
