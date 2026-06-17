package com.buildagent.backend.routes

import com.buildagent.backend.auth.AgentPrincipal
import com.buildagent.backend.services.LeaseExtensionService
import com.buildagent.shared.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.leaseExtensionRoutes(service: LeaseExtensionService) {
    authenticate("local-auth") {
        route("/admin/lease-extension-requests") {
            get {
                val principal = call.principal<AgentPrincipal>()!!
                val status = call.request.queryParameters["status"]
                val requests = service.list(UUID.fromString(principal.agencyId), status)
                call.respond(ApiResponse(requests))
            }

            patch("/{id}") {
                val principal = call.principal<AgentPrincipal>()!!
                val id = UUID.fromString(call.parameters["id"]!!)
                val req = call.receive<ResolveLeaseExtensionRequest>()
                val result = service.resolve(
                    agencyId = UUID.fromString(principal.agencyId),
                    requestId = id,
                    actorId = UUID.fromString(principal.userId),
                    req = req
                ) ?: return@patch call.respond(HttpStatusCode.NotFound, mapOf("error" to "Extension request not found"))
                call.respond(ApiResponse(result))
            }
        }

        route("/tenant/lease") {
            post("/extension-request") {
                val principal = call.principal<AgentPrincipal>()!!
                val req = call.receive<CreateLeaseExtensionRequest>()
                val result = service.submitRequest(
                    agencyId = UUID.fromString(principal.agencyId),
                    tenantId = UUID.fromString(principal.userId),
                    req = req
                )
                call.respond(HttpStatusCode.Created, ApiResponse(result))
            }
        }
    }
}
