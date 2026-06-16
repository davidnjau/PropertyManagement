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
    authenticate("local-auth") {
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
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Building not found"))
                call.respond(ApiResponse(building))
            }

            patch("/{id}") {
                val principal = call.principal<AgentPrincipal>()!!
                val id = UUID.fromString(call.parameters["id"]!!)
                val req = call.receive<UpdateBuildingRequest>()
                val building = service.update(UUID.fromString(principal.agencyId), id, req)
                    ?: return@patch call.respond(HttpStatusCode.NotFound, mapOf("error" to "Building not found"))
                call.respond(ApiResponse(building))
            }

            delete("/{id}") {
                val principal = call.principal<AgentPrincipal>()!!
                val id = UUID.fromString(call.parameters["id"]!!)
                val deleted = service.delete(UUID.fromString(principal.agencyId), id)
                if (!deleted) {
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to "Cannot delete building with active leases"))
                } else {
                    call.respond(HttpStatusCode.NoContent)
                }
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
