package com.buildagent.backend.routes

import com.buildagent.backend.auth.requireRole
import com.buildagent.backend.auth.userPrincipal
import com.buildagent.backend.services.UnitService
import com.buildagent.shared.models.ApiResponse
import com.buildagent.shared.models.CreateUnitRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.unitRoutes(unitService: UnitService) {
    authenticate("auth0") {
        route("/buildings/{buildingId}/units") {
            get {
                val p = call.userPrincipal()
                val buildingId = call.parameters["buildingId"]!!
                val units = unitService.listUnits(p.agencyId, buildingId)
                call.respond(ApiResponse(data = units))
            }
            post {
                val p = call.userPrincipal()
                p.requireRole("ADMIN", "AGENT")
                val buildingId = call.parameters["buildingId"]!!
                val request = call.receive<CreateUnitRequest>()
                val unit = unitService.createUnit(p.agencyId, buildingId, request)
                call.respond(HttpStatusCode.Created, ApiResponse(data = unit))
            }
        }

        route("/units/{id}") {
            get {
                val p = call.userPrincipal()
                val unit = unitService.getUnit(p.agencyId, call.parameters["id"]!!)
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Unit not found"))
                call.respond(ApiResponse(data = unit))
            }
            patch {
                val p = call.userPrincipal()
                p.requireRole("ADMIN", "AGENT")
                val request = call.receive<CreateUnitRequest>()
                val unit = unitService.updateUnit(p.agencyId, call.parameters["id"]!!, request)
                call.respond(ApiResponse(data = unit))
            }
        }
    }
}
