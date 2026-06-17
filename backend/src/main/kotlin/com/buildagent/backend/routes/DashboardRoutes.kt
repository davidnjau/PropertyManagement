package com.buildagent.backend.routes

import com.buildagent.backend.auth.AgentPrincipal
import com.buildagent.backend.services.DashboardService
import com.buildagent.shared.models.ApiResponse
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.dashboardRoutes(service: DashboardService) {
    authenticate("local-auth") {
        route("/dashboard") {
            get("/agent") {
                val principal = call.principal<AgentPrincipal>()!!
                val data = service.agentDashboard(UUID.fromString(principal.agencyId))
                call.respond(ApiResponse(data))
            }

            get("/stats") {
                val principal = call.principal<AgentPrincipal>()!!
                val data = service.agentDashboard(UUID.fromString(principal.agencyId))
                call.respond(ApiResponse(data))
            }
        }
    }
}
