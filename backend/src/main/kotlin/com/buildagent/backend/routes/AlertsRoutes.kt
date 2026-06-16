package com.buildagent.backend.routes

import com.buildagent.backend.auth.AgentPrincipal
import com.buildagent.backend.services.AlertsService
import com.buildagent.shared.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.alertsRoutes(service: AlertsService) {
    authenticate("local-auth") {
        route("/admin/alerts") {
            get {
                val principal = call.principal<AgentPrincipal>()!!
                val status = call.request.queryParameters["status"]
                val targetType = call.request.queryParameters["targetType"]
                val channel = call.request.queryParameters["channel"]
                val from = call.request.queryParameters["from"]
                val to = call.request.queryParameters["to"]
                val alerts = service.list(UUID.fromString(principal.agencyId), status, targetType, channel, from, to)
                call.respond(ApiResponse(alerts))
            }

            post {
                val principal = call.principal<AgentPrincipal>()!!
                val req = call.receive<CreateAlertRequest>()
                val alert = service.create(
                    agencyId = UUID.fromString(principal.agencyId),
                    actorId = UUID.fromString(principal.userId),
                    req = req
                )
                call.respond(HttpStatusCode.Created, ApiResponse(alert))
            }

            get("/{id}") {
                val principal = call.principal<AgentPrincipal>()!!
                val id = UUID.fromString(call.parameters["id"]!!)
                val alert = service.getById(UUID.fromString(principal.agencyId), id)
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Alert not found"))
                call.respond(ApiResponse(alert))
            }

            delete("/{id}") {
                val principal = call.principal<AgentPrincipal>()!!
                val id = UUID.fromString(call.parameters["id"]!!)
                val deleted = service.delete(UUID.fromString(principal.agencyId), id)
                if (!deleted) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Alert not found"))
                } else {
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}
