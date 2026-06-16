package com.buildagent.backend.routes

import com.buildagent.backend.auth.AgentPrincipal
import com.buildagent.backend.services.PaymentMethodsService
import com.buildagent.shared.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.paymentMethodsRoutes(service: PaymentMethodsService) {
    authenticate("local-auth") {
        route("/admin/payment-methods") {
            get {
                val principal = call.principal<AgentPrincipal>()!!
                val config = service.getConfig(UUID.fromString(principal.agencyId))
                call.respond(ApiResponse(config))
            }

            put("/{methodId}/toggle") {
                val principal = call.principal<AgentPrincipal>()!!
                val methodId = call.parameters["methodId"]!!
                val req = call.receive<ToggleMethodRequest>()
                service.toggleMethod(UUID.fromString(principal.agencyId), methodId, req.enabled)
                call.respond(HttpStatusCode.NoContent)
            }

            put("/mpesa/config") {
                val principal = call.principal<AgentPrincipal>()!!
                val req = call.receive<UpdateMpesaConfigRequest>()
                service.updateMpesaConfig(UUID.fromString(principal.agencyId), req)
                call.respond(HttpStatusCode.NoContent)
            }

            put("/paypal/config") {
                val principal = call.principal<AgentPrincipal>()!!
                val req = call.receive<UpdatePaypalConfigRequest>()
                service.updatePaypalConfig(UUID.fromString(principal.agencyId), req)
                call.respond(HttpStatusCode.NoContent)
            }

            put("/banks/{bankId}/toggle") {
                val principal = call.principal<AgentPrincipal>()!!
                val bankId = call.parameters["bankId"]!!
                val req = call.receive<ToggleMethodRequest>()
                service.toggleBank(UUID.fromString(principal.agencyId), bankId, req.enabled)
                call.respond(HttpStatusCode.NoContent)
            }

            put("/banks") {
                val principal = call.principal<AgentPrincipal>()!!
                val req = call.receive<BulkUpdateBanksRequest>()
                service.bulkUpdateBanks(UUID.fromString(principal.agencyId), req.banks)
                call.respond(HttpStatusCode.NoContent)
            }
        }

        route("/tenant/payment-methods") {
            get {
                val principal = call.principal<AgentPrincipal>()!!
                val config = service.getConfig(UUID.fromString(principal.agencyId))
                call.respond(ApiResponse(config))
            }
        }
    }
}
