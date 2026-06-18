package com.buildagent.backend.services

import com.buildagent.backend.auth.LocalJwtService
import com.buildagent.backend.auth.PasswordHasher
import com.buildagent.backend.db.dbQuery
import com.buildagent.backend.db.tables.AgenciesTable
import com.buildagent.backend.db.tables.UserCredentialsTable
import com.buildagent.backend.db.tables.UsersTable
import com.buildagent.shared.models.AuthResponse
import com.buildagent.shared.models.AuthUser
import com.buildagent.shared.models.LoginRequest
import com.buildagent.shared.models.RegisterRequest
import com.buildagent.shared.models.UserRole
import com.buildagent.shared.models.UserType
import com.buildagent.shared.models.primaryUserType
import com.buildagent.shared.models.toRoles
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import java.util.Base64
import java.util.UUID

class AuthService(private val jwtService: LocalJwtService) {

    suspend fun register(req: RegisterRequest): AuthResponse = dbQuery {
        // Validate required fields per user type
        when (req.userType) {
            UserType.AGENCY -> require(!req.agencyName.isNullOrBlank()) {
                "agencyName is required when registering as an agency"
            }
            else -> require(!req.agencyId.isNullOrBlank()) {
                "agencyId is required when registering as ${req.userType.name.lowercase()}"
            }
        }

        val exists = UsersTable.selectAll()
            .where { UsersTable.email eq req.email }
            .firstOrNull()
        require(exists == null) { "Email already registered" }

        val now: Instant = Clock.System.now()
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash(req.password, salt)
        val roles = req.userType.toRoles()

        val resolvedAgencyId: UUID = when (req.userType) {
            UserType.AGENCY -> {
                AgenciesTable.insertAndGetId {
                    it[AgenciesTable.name] = req.agencyName!!
                    it[AgenciesTable.contactEmail] = req.email
                    it[AgenciesTable.createdAt] = now
                    it[AgenciesTable.updatedAt] = now
                }.value
            }
            else -> UUID.fromString(req.agencyId!!)
        }

        val newUserId = UsersTable.insertAndGetId {
            it[UsersTable.agencyId] = resolvedAgencyId
            it[UsersTable.auth0Sub] = "local|${UUID.randomUUID()}"
            it[UsersTable.email] = req.email
            it[UsersTable.fullName] = req.fullName
            it[UsersTable.roles] = roles.map { r -> r.name }
            it[UsersTable.phone] = req.phone
            it[UsersTable.createdAt] = now
            it[UsersTable.updatedAt] = now
        }

        UserCredentialsTable.insertAndGetId {
            it[UserCredentialsTable.userId] = newUserId
            it[UserCredentialsTable.passwordHash] = hash
            it[UserCredentialsTable.salt] = Base64.getEncoder().encodeToString(salt)
        }

        val roleNames = roles.map { it.name }
        val token = jwtService.issue(
            userId = newUserId.value.toString(),
            agencyId = resolvedAgencyId.toString(),
            email = req.email,
            roles = roleNames
        )

        AuthResponse(
            token = token,
            user = AuthUser(
                id = newUserId.value.toString(),
                agencyId = resolvedAgencyId.toString(),
                email = req.email,
                fullName = req.fullName,
                roles = roleNames,
                userType = req.userType.name
            )
        )
    }

    suspend fun login(req: LoginRequest): AuthResponse = dbQuery {
        val userRow = UsersTable.selectAll()
            .where { UsersTable.email eq req.email and (UsersTable.isActive eq true) }
            .firstOrNull()
            ?: throw IllegalArgumentException("Invalid credentials")

        val foundUserId = userRow[UsersTable.id]

        val creds = UserCredentialsTable.selectAll()
            .where { UserCredentialsTable.userId eq foundUserId }
            .firstOrNull()
            ?: throw IllegalArgumentException("Invalid credentials")

        val valid = PasswordHasher.verify(
            password = req.password,
            saltBase64 = creds[UserCredentialsTable.salt],
            storedHash = creds[UserCredentialsTable.passwordHash]
        )
        require(valid) { "Invalid credentials" }

        val roles = userRow[UsersTable.roles]
        val token = jwtService.issue(
            userId = foundUserId.value.toString(),
            agencyId = userRow[UsersTable.agencyId].value.toString(),
            email = userRow[UsersTable.email],
            roles = roles
        )

        AuthResponse(
            token = token,
            user = AuthUser(
                id = foundUserId.value.toString(),
                agencyId = userRow[UsersTable.agencyId].value.toString(),
                email = userRow[UsersTable.email],
                fullName = userRow[UsersTable.fullName],
                roles = roles,
                userType = roles.map { UserRole.valueOf(it) }.primaryUserType().name
            )
        )
    }

    suspend fun me(userId: String): AuthUser = dbQuery {
        val userRow = UsersTable.selectAll()
            .where { UsersTable.id eq UUID.fromString(userId) and (UsersTable.isActive eq true) }
            .firstOrNull()
            ?: throw NoSuchElementException("User not found")

        val roles = userRow[UsersTable.roles]
        AuthUser(
            id = userRow[UsersTable.id].value.toString(),
            agencyId = userRow[UsersTable.agencyId].value.toString(),
            email = userRow[UsersTable.email],
            fullName = userRow[UsersTable.fullName],
            roles = roles,
            userType = roles.map { UserRole.valueOf(it) }.primaryUserType().name
        )
    }
}
