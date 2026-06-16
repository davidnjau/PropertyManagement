package com.buildagent.backend.plugins

import com.buildagent.backend.kafka.producers.DomainEventProducer
import com.buildagent.backend.routes.*
import com.buildagent.backend.services.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val authService by inject<AuthService>()
    val adminService by inject<AdminService>()
    val buildingService by inject<BuildingService>()
    val unitService by inject<UnitService>()
    val tenantService by inject<TenantService>()
    val leaseService by inject<LeaseService>()
    val paymentService by inject<PaymentService>()
    val maintenanceService by inject<MaintenanceService>()
    val dashboardService by inject<DashboardService>()
    val kafka by inject<DomainEventProducer>()

    routing {
        get("/health") {
            call.respond(mapOf("status" to "ok", "ts" to Clock.System.now().toString()))
        }

        route("/api/v1") {
            authRoutes(authService)
            adminRoutes(adminService)
            buildingRoutes(buildingService)
            unitRoutes(unitService)
            tenantRoutes(tenantService)
            leaseRoutes(leaseService, kafka)
            paymentRoutes(paymentService)
            maintenanceRoutes(maintenanceService, kafka)
            dashboardRoutes(dashboardService)
        }
    }
}
