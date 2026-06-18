package com.buildagent.backend.auth

import io.ktor.server.application.*
import io.ktor.server.auth.*

data class UserPrincipal(
    val userId: String,
    val agencyId: String,
    val email: String,
    val roles: List<String>
)

fun ApplicationCall.userPrincipal(): UserPrincipal {
    val agent = principal<AgentPrincipal>() ?: error("Unauthenticated")
    return UserPrincipal(
        userId = agent.userId,
        agencyId = agent.agencyId,
        email = agent.email,
        roles = agent.roles
    )
}

fun UserPrincipal.requireRole(vararg allowed: String) {
    require(roles.any { it in allowed }) { "Forbidden: requires ${allowed.joinToString()}, got $roles" }
}
