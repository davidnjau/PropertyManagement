package com.buildagent.backend.routes

import com.buildagent.backend.auth.AgentPrincipal
import com.buildagent.backend.services.BuildingService
import com.buildagent.shared.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.buildingRoutes(service: BuildingService) {
    authenticate("auth0") {
        route("/buildings") {
            get {
                val principal = call.principal<AgentPrincipal>()!!
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
                val (buildings, total) = service.list(UUID.fromString(principal.agencyId), page, limit)
                call.respond(ApiResponse(buildings, PaginationMeta(total, page, limit, (total + limit - 1) / limit)))
            }

            post {
                val principal = call.principal<AgentPrincipal>()!!
                val req = call.receive<CreateBuildingRequest>()
                val building = service.create(UUID.fromString(principal.agencyId), req)
                call.respond(HttpStatusCode.Created, ApiResponse(building))
            }

            get("/{id}") {
                val principal = call.principal<AgentPrincipal>()!!
                val id = UUID.fromString(call.parameters["id"]!!)
                val building = service.get(UUID.fromString(principal.agencyId), id)
                    ?: throw NoSuchElementException("Building not found")
                call.respond(ApiResponse(building))
            }

            get("/{id}/summary") {
                val principal = call.principal<AgentPrincipal>()!!
                val id = UUID.fromString(call.parameters["id"]!!)
                val summary = service.summary(UUID.fromString(principal.agencyId), id)
                call.respond(ApiResponse(summary))
            }
        }
    }
}
