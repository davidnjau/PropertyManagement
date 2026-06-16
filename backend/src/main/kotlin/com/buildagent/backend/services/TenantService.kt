package com.buildagent.backend.services

import com.buildagent.backend.db.dbQuery
import com.buildagent.backend.db.extensions.toTenant
import com.buildagent.backend.db.tables.TenantsTable
import com.buildagent.shared.models.CreateTenantRequest
import com.buildagent.shared.models.Tenant
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.*
import java.util.UUID

class TenantService {

    suspend fun listTenants(agencyId: String, page: Int = 1, limit: Int = 20): Pair<List<Tenant>, Int> = dbQuery {
        val offset = ((page - 1) * limit).toLong()
        val query = TenantsTable.selectAll().where {
            (TenantsTable.agencyId eq UUID.fromString(agencyId)) and
            (TenantsTable.isActive eq true)
        }
        val total = query.count().toInt()
        val tenants = query
            .orderBy(TenantsTable.fullName, SortOrder.ASC)
            .limit(limit, offset)
            .map { it.toTenant() }
        tenants to total
    }

    suspend fun getTenant(agencyId: String, tenantId: String): Tenant? = dbQuery {
        TenantsTable.selectAll().where {
            (TenantsTable.id eq UUID.fromString(tenantId)) and
            (TenantsTable.agencyId eq UUID.fromString(agencyId))
        }.firstOrNull()?.toTenant()
    }

    suspend fun createTenant(agencyId: String, request: CreateTenantRequest): Tenant = dbQuery {
        val now: Instant = Clock.System.now()
        val id = TenantsTable.insertAndGetId {
            it[TenantsTable.agencyId] = UUID.fromString(agencyId)
            it[fullName] = request.fullName
            it[email] = request.email
            it[phone] = request.phone
            it[dateOfBirth] = request.dateOfBirth?.let { d -> LocalDate.parse(d) }
            it[idType] = request.idType
            it[idReference] = request.idReference
            it[emergencyContactName] = request.emergencyContactName
            it[emergencyContactPhone] = request.emergencyContactPhone
            it[notes] = request.notes
            it[createdAt] = now
            it[updatedAt] = now
        }
        TenantsTable.selectAll().where { TenantsTable.id eq id }.first().toTenant()
    }

    suspend fun updateTenant(agencyId: String, tenantId: String, request: CreateTenantRequest): Tenant = dbQuery {
        TenantsTable.selectAll().where {
            (TenantsTable.id eq UUID.fromString(tenantId)) and
            (TenantsTable.agencyId eq UUID.fromString(agencyId))
        }.firstOrNull() ?: error("Tenant not found")

        val nowUpdate: Instant = Clock.System.now()
        TenantsTable.update({ TenantsTable.id eq UUID.fromString(tenantId) }) {
            it[fullName] = request.fullName
            it[email] = request.email
            it[phone] = request.phone
            it[notes] = request.notes
            it[updatedAt] = nowUpdate
        }
        TenantsTable.selectAll().where { TenantsTable.id eq UUID.fromString(tenantId) }.first().toTenant()
    }
}
