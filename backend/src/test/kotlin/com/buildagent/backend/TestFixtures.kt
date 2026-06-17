package com.buildagent.backend

import com.buildagent.backend.auth.PasswordHasher
import com.buildagent.backend.db.tables.*
import com.buildagent.shared.models.BuildingType
import com.buildagent.shared.models.RentFrequency
import com.buildagent.shared.models.UnitStatus
import com.buildagent.shared.models.UserRole
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.Base64
import java.util.UUID

/**
 * Helpers to seed test data into the H2 test database.
 * All IDs returned as UUID strings.
 */
object TestFixtures {

    const val DEFAULT_PASSWORD = "TestPass123!"

    fun createAgency(name: String = "Test Agency", email: String = "agency@test.com"): String {
        val now = Clock.System.now()
        return transaction {
            AgenciesTable.insertAndGetId {
                it[AgenciesTable.name] = name
                it[AgenciesTable.contactEmail] = email
                it[AgenciesTable.createdAt] = now
                it[AgenciesTable.updatedAt] = now
            }.value.toString()
        }
    }

    fun createUser(
        agencyId: String,
        email: String = "agent@test.com",
        fullName: String = "Test Agent",
        role: UserRole = UserRole.ADMIN,
        password: String = DEFAULT_PASSWORD
    ): String {
        val now = Clock.System.now()
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash(password, salt)
        return transaction {
            val uid = UsersTable.insertAndGetId {
                it[UsersTable.agencyId] = UUID.fromString(agencyId)
                it[UsersTable.auth0Sub] = "local|${UUID.randomUUID()}"
                it[UsersTable.email] = email
                it[UsersTable.fullName] = fullName
                it[UsersTable.role] = role
                it[UsersTable.createdAt] = now
                it[UsersTable.updatedAt] = now
            }
            UserCredentialsTable.insertAndGetId {
                it[UserCredentialsTable.userId] = uid
                it[UserCredentialsTable.passwordHash] = hash
                it[UserCredentialsTable.salt] = Base64.getEncoder().encodeToString(salt)
            }
            uid.value.toString()
        }
    }

    fun createBuilding(agencyId: String, name: String = "Test Building"): String {
        val now = Clock.System.now()
        return transaction {
            BuildingsTable.insertAndGetId {
                it[BuildingsTable.agencyId] = UUID.fromString(agencyId)
                it[BuildingsTable.name] = name
                it[BuildingsTable.address] = "123 Test St"
                it[BuildingsTable.suburb] = "Testville"
                it[BuildingsTable.state] = "VIC"
                it[BuildingsTable.postcode] = "3000"
                it[BuildingsTable.country] = "Australia"
                it[BuildingsTable.buildingType] = BuildingType.RESIDENTIAL
                it[BuildingsTable.createdAt] = now
                it[BuildingsTable.updatedAt] = now
            }.value.toString()
        }
    }

    fun createUnit(buildingId: String, unitNumber: String = "1A", rentAmount: Double = 1500.0): String {
        val now = Clock.System.now()
        return transaction {
            UnitsTable.insertAndGetId {
                it[UnitsTable.buildingId] = UUID.fromString(buildingId)
                it[UnitsTable.unitNumber] = unitNumber
                it[UnitsTable.bedrooms] = 2
                it[UnitsTable.bathrooms] = 1
                it[UnitsTable.rentAmount] = rentAmount.toBigDecimal()
                it[UnitsTable.rentFrequency] = RentFrequency.MONTHLY
                it[UnitsTable.status] = UnitStatus.VACANT
                it[UnitsTable.createdAt] = now
                it[UnitsTable.updatedAt] = now
            }.value.toString()
        }
    }

    fun createTenant(agencyId: String, email: String = "tenant@test.com", fullName: String = "Test Tenant"): String {
        val now = Clock.System.now()
        return transaction {
            TenantsTable.insertAndGetId {
                it[TenantsTable.agencyId] = UUID.fromString(agencyId)
                it[TenantsTable.fullName] = fullName
                it[TenantsTable.email] = email
                it[TenantsTable.createdAt] = now
                it[TenantsTable.updatedAt] = now
            }.value.toString()
        }
    }
}
