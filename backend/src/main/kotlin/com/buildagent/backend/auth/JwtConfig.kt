package com.buildagent.backend.auth

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.concurrent.TimeUnit

data class AgentPrincipal(
    val userId: String,
    val agencyId: String,
    val email: String,
    val roles: List<String>
) : Principal

private val logger = LoggerFactory.getLogger("JwtConfig")

fun Application.configureAuthentication(localJwtService: LocalJwtService) {
    val domain = environment.config.property("auth0.domain").getString()
    val audience = environment.config.property("auth0.audience").getString()
    val namespace = environment.config.property("auth0.namespace").getString()
    val issuer = "https://$domain/"

    val jwkProvider = JwkProviderBuilder(URL("https://$domain/.well-known/jwks.json"))
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    install(Authentication) {
        jwt("auth0") {
            realm = "BuildAgent API"
            verifier(jwkProvider, issuer) {
                acceptLeeway(3)
                withAudience(audience)
            }
            validate { credential ->
                val agencyId = credential.payload.getClaim("${namespace}agency_id").asString()
                val userId = credential.payload.getClaim("${namespace}user_id").asString()
                val roles = credential.payload.getClaim("${namespace}roles").asList(String::class.java)
                    ?: listOf(credential.payload.getClaim("${namespace}role").asString() ?: "")
                val email = credential.payload.getClaim("email").asString() ?: ""

                if (agencyId.isNullOrBlank() || userId.isNullOrBlank() || roles.isEmpty()) {
                    logger.warn("JWT missing custom claims for subject=${credential.payload.subject}")
                    null
                } else {
                    AgentPrincipal(userId = userId, agencyId = agencyId, email = email, roles = roles)
                }
            }
            challenge { _, _ ->
                call.respondText(
                    text = """{"error":"Invalid or expired token"}""",
                    contentType = ContentType.Application.Json,
                    status = HttpStatusCode.Unauthorized
                )
            }
        }

        jwt("local-auth") {
            realm = "BuildAgent API"
            verifier(localJwtService.verifier())
            validate { credential ->
                val userId = credential.payload.subject
                val agencyId = credential.payload.getClaim("agency_id").asString()
                val email = credential.payload.getClaim("email").asString() ?: ""
                val roles = credential.payload.getClaim("roles").asList(String::class.java)
                    ?: listOf(credential.payload.getClaim("role").asString() ?: "")

                if (userId.isNullOrBlank() || agencyId.isNullOrBlank() || roles.isEmpty()) {
                    logger.warn("Local JWT missing claims for subject=$userId")
                    null
                } else {
                    AgentPrincipal(userId = userId, agencyId = agencyId, email = email, roles = roles)
                }
            }
            challenge { _, _ ->
                call.respondText(
                    text = """{"error":"Invalid or expired token"}""",
                    contentType = ContentType.Application.Json,
                    status = HttpStatusCode.Unauthorized
                )
            }
        }
    }
}
