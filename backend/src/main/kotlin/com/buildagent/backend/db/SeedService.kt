package com.buildagent.backend.db

import com.buildagent.backend.auth.PasswordHasher
import com.buildagent.backend.db.tables.AgenciesTable
import com.buildagent.backend.db.tables.UserCredentialsTable
import com.buildagent.backend.db.tables.UsersTable
import com.buildagent.shared.models.UserRole
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.util.Base64
import java.util.UUID

object SeedService {

    private val log = LoggerFactory.getLogger("SeedService")

    private const val SUPER_ADMIN_EMAIL = "davidnjau21@gmail.com"
    private const val SUPER_ADMIN_NAME  = "David Njau"
    private const val SUPER_ADMIN_PASS  = "Sc281-6736/2014"
    private const val AGENCY_NAME       = "BuildAgent HQ"

    fun seed() {
        transaction {
            val existing = UsersTable.selectAll()
                .where { UsersTable.email eq SUPER_ADMIN_EMAIL }
                .firstOrNull()

            if (existing != null) {
                log.info("Super admin already exists — skipping seed")
                return@transaction
            }

            val now = Clock.System.now()

            // Create (or reuse) the default agency
            val agencyId = AgenciesTable.selectAll()
                .where { AgenciesTable.name eq AGENCY_NAME }
                .firstOrNull()
                ?.get(AgenciesTable.id)
                ?.value
                ?: AgenciesTable.insertAndGetId {
                    it[name]         = AGENCY_NAME
                    it[contactEmail] = SUPER_ADMIN_EMAIL
                    it[createdAt]    = now
                    it[updatedAt]    = now
                }.value

            val salt = PasswordHasher.generateSalt()
            val hash = PasswordHasher.hash(SUPER_ADMIN_PASS, salt)

            val userId = UsersTable.insertAndGetId {
                it[UsersTable.agencyId]  = agencyId
                it[auth0Sub]  = "local|${UUID.randomUUID()}"
                it[email]     = SUPER_ADMIN_EMAIL
                it[fullName]  = SUPER_ADMIN_NAME
                it[roles]     = listOf(UserRole.ADMIN.name)
                it[isActive]  = true
                it[createdAt] = now
                it[updatedAt] = now
            }

            UserCredentialsTable.insertAndGetId {
                it[UserCredentialsTable.userId]       = userId
                it[passwordHash] = hash
                it[UserCredentialsTable.salt]         = Base64.getEncoder().encodeToString(salt)
            }

            log.info("Super admin seeded: $SUPER_ADMIN_EMAIL (agencyId=$agencyId)")
        }
    }
}
