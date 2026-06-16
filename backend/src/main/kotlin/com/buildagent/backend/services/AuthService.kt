package com.buildagent.backend.services

import com.buildagent.backend.auth.LocalJwtService
import com.buildagent.backend.db.dbQuery
import com.buildagent.backend.db.tables.AgenciesTable
import com.buildagent.backend.db.tables.UserCredentialsTable
import com.buildagent.backend.db.tables.UsersTable
import com.buildagent.shared.models.AuthResponse
import com.buildagent.shared.models.AuthUser
import com.buildagent.shared.models.LoginRequest
import com.buildagent.shared.models.RegisterRequest
import com.buildagent.shared.models.UserRole
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class AuthService(private val jwtService: LocalJwtService) {

    suspend fun register(req: RegisterRequest): AuthResponse = dbQuery {
        val exists = UsersTable.selectAll()
            .where { UsersTable.email eq req.email }
            .firstOrNull()
        require(exists == null) { "Email already registered" }

        val now: Instant = Clock.System.now()
        val saltBytes = generateSalt()
        val hash = hashPassword(req.password, saltBytes)

        val newAgencyId = AgenciesTable.insertAndGetId {
            it[AgenciesTable.name] = req.agencyName
            it[AgenciesTable.contactEmail] = req.email
            it[AgenciesTable.createdAt] = now
            it[AgenciesTable.updatedAt] = now
        }

        val newUserId = UsersTable.insertAndGetId {
            it[UsersTable.agencyId] = newAgencyId
            it[UsersTable.auth0Sub] = "local|${UUID.randomUUID()}"
            it[UsersTable.email] = req.email
            it[UsersTable.fullName] = req.fullName
            it[UsersTable.role] = UserRole.ADMIN
            it[UsersTable.phone] = req.phone
            it[UsersTable.createdAt] = now
            it[UsersTable.updatedAt] = now
        }

        UserCredentialsTable.insertAndGetId {
            it[UserCredentialsTable.userId] = newUserId
            it[UserCredentialsTable.passwordHash] = hash
            it[UserCredentialsTable.salt] = Base64.getEncoder().encodeToString(saltBytes)
        }

        val token = jwtService.issue(
            userId = newUserId.value.toString(),
            agencyId = newAgencyId.value.toString(),
            email = req.email,
            role = UserRole.ADMIN.name
        )

        AuthResponse(
            token = token,
            user = AuthUser(
                id = newUserId.value.toString(),
                agencyId = newAgencyId.value.toString(),
                email = req.email,
                fullName = req.fullName,
                role = UserRole.ADMIN.name
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

        val salt = Base64.getDecoder().decode(creds[UserCredentialsTable.salt])
        val expected = hashPassword(req.password, salt)
        require(expected == creds[UserCredentialsTable.passwordHash]) { "Invalid credentials" }

        val token = jwtService.issue(
            userId = foundUserId.value.toString(),
            agencyId = userRow[UsersTable.agencyId].value.toString(),
            email = userRow[UsersTable.email],
            role = userRow[UsersTable.role].name
        )

        AuthResponse(
            token = token,
            user = AuthUser(
                id = foundUserId.value.toString(),
                agencyId = userRow[UsersTable.agencyId].value.toString(),
                email = userRow[UsersTable.email],
                fullName = userRow[UsersTable.fullName],
                role = userRow[UsersTable.role].name
            )
        )
    }

    suspend fun me(userId: String): AuthUser = dbQuery {
        val userRow = UsersTable.selectAll()
            .where { UsersTable.id eq UUID.fromString(userId) and (UsersTable.isActive eq true) }
            .firstOrNull()
            ?: throw NoSuchElementException("User not found")

        AuthUser(
            id = userRow[UsersTable.id].value.toString(),
            agencyId = userRow[UsersTable.agencyId].value.toString(),
            email = userRow[UsersTable.email],
            fullName = userRow[UsersTable.fullName],
            role = userRow[UsersTable.role].name
        )
    }

    private fun generateSalt(): ByteArray {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes
    }

    private fun hashPassword(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, 310_000, 256)
        val key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec)
        return Base64.getEncoder().encodeToString(key.encoded)
    }
}
