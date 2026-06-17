package com.buildagent.backend.routes

import com.buildagent.backend.auth.AgentPrincipal
import com.buildagent.backend.services.TenantPortalService
import com.buildagent.shared.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.tenantPortalRoutes(service: TenantPortalService) {
    authenticate("local-auth") {
        route("/tenant") {
            get("/overview") {
                val principal = call.principal<AgentPrincipal>()!!
                val overview = service.getOverview(principal.userId)
                call.respond(ApiResponse(overview))
            }

            get("/lease") {
                val principal = call.principal<AgentPrincipal>()!!
                val lease = service.getActiveLease(principal.userId)
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "No active lease found"))
                call.respond(ApiResponse(lease))
            }

            get("/payments") {
                val principal = call.principal<AgentPrincipal>()!!
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
                val (payments, total) = service.getPayments(principal.userId, page, limit)
                call.respond(ApiResponse(payments, PaginationMeta(total, page, limit, (total + limit - 1) / limit)))
            }

            post("/payments") {
                val principal = call.principal<AgentPrincipal>()!!
                val req = call.receive<RecordPaymentRequest>()
                val payment = service.recordPayment(UUID.fromString(principal.agencyId), principal.userId, req)
                call.respond(HttpStatusCode.Created, ApiResponse(payment))
            }

            get("/maintenance") {
                val principal = call.principal<AgentPrincipal>()!!
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
                val (requests, total) = service.getMaintenance(UUID.fromString(principal.agencyId), principal.userId, page, limit)
                call.respond(ApiResponse(requests, PaginationMeta(total, page, limit, (total + limit - 1) / limit)))
            }

            post("/maintenance") {
                val principal = call.principal<AgentPrincipal>()!!
                val req = call.receive<CreateMaintenanceRequest>()
                val result = service.submitMaintenance(UUID.fromString(principal.agencyId), principal.userId, req)
                call.respond(HttpStatusCode.Created, ApiResponse(result))
            }
        }
    }
}
