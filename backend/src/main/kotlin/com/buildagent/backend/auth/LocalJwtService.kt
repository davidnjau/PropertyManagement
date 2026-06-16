package com.buildagent.backend.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import kotlinx.datetime.Clock
import java.util.Date

class LocalJwtService(secret: String, private val issuer: String = "buildagent-local") {
    private val algorithm = Algorithm.HMAC256(secret)

    fun issue(userId: String, agencyId: String, email: String, role: String): String =
        JWT.create()
            .withIssuer(issuer)
            .withSubject(userId)
            .withClaim("agency_id", agencyId)
            .withClaim("email", email)
            .withClaim("role", role)
            .withExpiresAt(Date(Clock.System.now().toEpochMilliseconds() + 86_400_000L * 30))
            .sign(algorithm)

    fun verifier(): JWTVerifier = JWT.require(algorithm).withIssuer(issuer).build()
}
