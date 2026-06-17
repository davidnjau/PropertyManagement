package com.buildagent.backend.routes

import com.buildagent.backend.auth.requireRole
import com.buildagent.backend.auth.userPrincipal
import com.buildagent.backend.kafka.producers.DomainEventProducer
import com.buildagent.backend.services.MaintenanceService
import com.buildagent.shared.events.MaintenanceCreatedEvent
import com.buildagent.shared.events.MaintenanceStatusChangedEvent
import com.buildagent.shared.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import java.util.UUID

fun Route.maintenanceRoutes(maintenanceService: MaintenanceService, kafka: DomainEventProducer) {
    authenticate("local-auth") {
        route("/maintenance") {
            get {
                val p = call.userPrincipal()
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
                val status = call.request.queryParameters["status"]?.let { MaintenanceStatus.valueOf(it) }
                val priority = call.request.queryParameters["priority"]?.let { MaintenancePriority.valueOf(it) }
                val (requests, total) = maintenanceService.listMaintenance(p.agencyId, status, priority, page, limit)
                call.respond(ApiResponse(data = requests, meta = PaginationMeta(total, page, limit, (total + limit - 1) / limit)))
            }
            post {
                val p = call.userPrincipal()
                val request = call.receive<CreateMaintenanceRequest>()
                val result = maintenanceService.createRequest(p.agencyId, p.userId, request)
                kafka.maintenanceCreated(MaintenanceCreatedEvent(
                    eventId = UUID.randomUUID().toString(),
                    agencyId = p.agencyId,
                    requestId = result.id,
                    unitId = result.unitId,
                    title = result.title,
                    priority = result.priority.name,
                    category = result.category.name,
                    slaTargetDate = result.slaTargetDate ?: "",
                    occurredAt = Clock.System.now().toString()
                ))
                call.respond(HttpStatusCode.Created, ApiResponse(data = result))
            }
            patch("/{id}") {
                val p = call.userPrincipal()
                p.requireRole("ADMIN", "AGENT")
                val request = call.receive<UpdateMaintenanceRequest>()
                val before = maintenanceService.getRequest(p.agencyId, call.parameters["id"]!!)
                val result = maintenanceService.updateStatus(p.agencyId, call.parameters["id"]!!, request)
                if (request.status != null && before?.status != request.status) {
                    kafka.maintenanceStatusChanged(MaintenanceStatusChangedEvent(
                        eventId = UUID.randomUUID().toString(),
                        agencyId = p.agencyId,
                        requestId = result.id,
                        previousStatus = before?.status?.name ?: "",
                        newStatus = result.status.name,
                        actorId = p.userId,
                        occurredAt = Clock.System.now().toString()
                    ))
                }
                call.respond(ApiResponse(data = result))
            }
            post("/{id}/close") {
                val p = call.userPrincipal()
                p.requireRole("ADMIN", "AGENT")
                val result = maintenanceService.closeRequest(p.agencyId, p.userId, call.parameters["id"]!!, call.receive<CloseMaintenanceRequest>())
                call.respond(ApiResponse(data = result))
            }
        }
    }
}
