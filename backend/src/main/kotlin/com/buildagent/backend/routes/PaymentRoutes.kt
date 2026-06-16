package com.buildagent.backend.routes

import com.buildagent.backend.auth.AgentPrincipal
import com.buildagent.backend.services.PaymentService
import com.buildagent.shared.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.paymentRoutes(service: PaymentService) {
    authenticate("auth0") {
        route("/payments") {
            get {
                val principal = call.principal<AgentPrincipal>()!!
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
                val leaseId = call.request.queryParameters["leaseId"]?.let { UUID.fromString(it) }
                val (payments, total) = service.list(UUID.fromString(principal.agencyId), page, limit, leaseId)
                call.respond(ApiResponse(payments, PaginationMeta(total, page, limit, (total + limit - 1) / limit)))
            }

            post {
                val principal = call.principal<AgentPrincipal>()!!
                val req = call.receive<RecordPaymentRequest>()
                val payment = service.record(UUID.fromString(principal.agencyId), UUID.fromString(principal.userId), req)
                call.respond(HttpStatusCode.Created, ApiResponse(payment))
            }

            get("/overdue") {
                val principal = call.principal<AgentPrincipal>()!!
                val payments = service.overdue(UUID.fromString(principal.agencyId))
                call.respond(ApiResponse(payments))
            }

            post("/{id}/adjust") {
                val principal = call.principal<AgentPrincipal>()!!
                val id = UUID.fromString(call.parameters["id"]!!)
                val req = call.receive<AdjustPaymentRequest>()
                val adjustment = service.adjust(UUID.fromString(principal.agencyId), UUID.fromString(principal.userId), id, req)
                call.respond(HttpStatusCode.Created, ApiResponse(adjustment))
            }
        }
    }
}
