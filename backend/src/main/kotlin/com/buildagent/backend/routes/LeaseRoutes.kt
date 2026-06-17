package com.buildagent.backend.routes

import com.buildagent.backend.auth.requireRole
import com.buildagent.backend.auth.userPrincipal
import com.buildagent.backend.kafka.producers.DomainEventProducer
import com.buildagent.backend.services.LeaseService
import com.buildagent.shared.events.LeaseCreatedEvent
import com.buildagent.shared.events.LeaseTerminatedEvent
import com.buildagent.shared.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import java.util.UUID

fun Route.leaseRoutes(leaseService: LeaseService, kafka: DomainEventProducer) {
    authenticate("local-auth") {
        route("/leases") {
            get {
                val p = call.userPrincipal()
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
                val (leases, total) = leaseService.listLeases(p.agencyId, page, limit)
                call.respond(ApiResponse(data = leases, meta = PaginationMeta(total, page, limit, (total + limit - 1) / limit)))
            }
            post {
                val p = call.userPrincipal()
                p.requireRole("ADMIN", "AGENT")
                val request = call.receive<CreateLeaseRequest>()
                val lease = leaseService.createLease(p.agencyId, request)
                kafka.leaseCreated(LeaseCreatedEvent(
                    eventId = UUID.randomUUID().toString(),
                    agencyId = p.agencyId,
                    leaseId = lease.id,
                    unitId = lease.unitId,
                    tenantId = lease.tenantId,
                    tenantEmail = "",
                    startDate = lease.startDate,
                    endDate = lease.endDate,
                    rentAmount = lease.rentAmount,
                    occurredAt = Clock.System.now().toString()
                ))
                call.respond(HttpStatusCode.Created, ApiResponse(data = lease))
            }
            get("/{id}") {
                val p = call.userPrincipal()
                val lease = leaseService.getLease(p.agencyId, call.parameters["id"]!!)
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Lease not found"))
                call.respond(ApiResponse(data = lease))
            }
            post("/{id}/terminate") {
                val p = call.userPrincipal()
                p.requireRole("ADMIN", "AGENT")
                val request = call.receive<TerminateLeaseRequest>()
                val lease = leaseService.terminateLease(p.agencyId, call.parameters["id"]!!, request.moveOutDate)
                kafka.leaseTerminated(LeaseTerminatedEvent(
                    eventId = UUID.randomUUID().toString(),
                    agencyId = p.agencyId,
                    leaseId = lease.id,
                    unitId = lease.unitId,
                    tenantId = lease.tenantId,
                    moveOutDate = request.moveOutDate,
                    actorId = p.userId,
                    occurredAt = Clock.System.now().toString()
                ))
                call.respond(ApiResponse(data = lease))
            }
        }
    }
}
