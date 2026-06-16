package com.buildagent.backend.di

import com.buildagent.backend.kafka.producers.DomainEventProducer
import com.buildagent.backend.scheduler.ScheduledJobs
import com.buildagent.backend.services.*
import io.ktor.server.config.*
import org.koin.dsl.module

fun backendModule(config: ApplicationConfig) = module {
    single { AuditService() }
    single { BuildingService() }
    single { UnitService() }
    single { TenantService() }
    single { LeaseService() }
    single { PaymentService(get()) }
    single { MaintenanceService() }
    single { DashboardService() }
    single { DomainEventProducer() }
    single { ScheduledJobs() }
}
