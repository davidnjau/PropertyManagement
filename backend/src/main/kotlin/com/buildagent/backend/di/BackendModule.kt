package com.buildagent.backend.di

import com.buildagent.backend.auth.LocalJwtService
import com.buildagent.backend.kafka.producers.DomainEventProducer
import com.buildagent.backend.scheduler.ScheduledJobs
import com.buildagent.backend.services.*
import io.ktor.server.config.*
import org.koin.dsl.module

fun backendModule(config: ApplicationConfig, localJwtService: LocalJwtService) = module {
    single { localJwtService }
    single { AuthService(get()) }
    single { AdminService() }
    single { AuditService() }
    single { BuildingService() }
    single { UnitService() }
    single { TenantService() }
    single { LeaseService() }
    single { PaymentService(get()) }
    single { MaintenanceService() }
    single { DashboardService() }
    single { PaymentMethodsService() }
    single { DocumentsService() }
    single { AlertsService() }
    single { LeaseExtensionService() }
    single { TenantPortalService() }
    single { ContactService() }
    single { DomainEventProducer() }
    single { ScheduledJobs() }
}
