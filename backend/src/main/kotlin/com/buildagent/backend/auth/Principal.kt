package com.buildagent.backend.auth

import io.ktor.server.application.*
import io.ktor.server.auth.*

data class UserPrincipal(
    val userId: String,
    val agencyId: String,
    val email: String,
    val role: String
)

fun ApplicationCall.userPrincipal(): UserPrincipal {
    val agent = principal<AgentPrincipal>() ?: error("Unauthenticated")
    return UserPrincipal(
        userId = agent.userId,
        agencyId = agent.agencyId,
        email = agent.email,
        role = agent.role
    )
}

fun UserPrincipal.requireRole(vararg roles: String) {
    require(role in roles) { "Forbidden: requires ${roles.joinToString()}, got $role" }
}
