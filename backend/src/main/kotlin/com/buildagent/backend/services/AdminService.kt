package com.buildagent.backend.services

import com.buildagent.backend.auth.PasswordHasher
import com.buildagent.backend.db.dbQuery
import com.buildagent.backend.db.tables.UserCredentialsTable
import com.buildagent.backend.db.tables.UsersTable
import com.buildagent.shared.models.AdminUserResponse
import com.buildagent.shared.models.CreateUserRequest
import com.buildagent.shared.models.toRole
import com.buildagent.shared.models.toUserType
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import java.util.Base64
import java.util.UUID

class AdminService {

    suspend fun listUsers(agencyId: String): List<AdminUserResponse> = dbQuery {
        UsersTable.selectAll()
            .where { UsersTable.agencyId eq UUID.fromString(agencyId) and (UsersTable.isActive eq true) }
            .orderBy(UsersTable.createdAt)
            .map { row ->
                val role = row[UsersTable.role]
                AdminUserResponse(
                    id = row[UsersTable.id].value.toString(),
                    agencyId = agencyId,
                    email = row[UsersTable.email],
                    fullName = row[UsersTable.fullName],
                    role = role.name,
                    userType = role.toUserType().name,
                    phone = row[UsersTable.phone],
                    isActive = row[UsersTable.isActive],
                    createdAt = row[UsersTable.createdAt].toString()
                )
            }
    }

    suspend fun createUser(agencyId: String, req: CreateUserRequest): AdminUserResponse = dbQuery {
        val exists = UsersTable.selectAll()
            .where { UsersTable.email eq req.email }
            .firstOrNull()
        require(exists == null) { "Email already registered" }

        val now: Instant = Clock.System.now()
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash(req.password, salt)
        val role = req.userType.toRole()

        val newUserId = UsersTable.insertAndGetId {
            it[UsersTable.agencyId] = UUID.fromString(agencyId)
            it[UsersTable.auth0Sub] = "local|${UUID.randomUUID()}"
            it[UsersTable.email] = req.email
            it[UsersTable.fullName] = req.fullName
            it[UsersTable.role] = role
            it[UsersTable.phone] = req.phone
            it[UsersTable.createdAt] = now
            it[UsersTable.updatedAt] = now
        }

        UserCredentialsTable.insertAndGetId {
            it[UserCredentialsTable.userId] = newUserId
            it[UserCredentialsTable.passwordHash] = hash
            it[UserCredentialsTable.salt] = Base64.getEncoder().encodeToString(salt)
        }

        AdminUserResponse(
            id = newUserId.value.toString(),
            agencyId = agencyId,
            email = req.email,
            fullName = req.fullName,
            role = role.name,
            userType = req.userType.name,
            phone = req.phone,
            isActive = true,
            createdAt = now.toString()
        )
    }
}
