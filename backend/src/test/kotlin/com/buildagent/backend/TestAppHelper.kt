package com.buildagent.backend

import com.buildagent.backend.auth.LocalJwtService
import com.buildagent.backend.auth.configureAuthentication
import com.buildagent.backend.plugins.configureSerialization
import com.buildagent.backend.plugins.configureStatusPages
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json

val TEST_JWT_SERVICE = LocalJwtService("test-secret-for-routes")
val TEST_AGENCY_ID = "11111111-1111-1111-1111-111111111111"
val TEST_USER_ID = "22222222-2222-2222-2222-222222222222"

/** Minimal config required by configureAuthentication(). */
private val TEST_APP_CONFIG = MapApplicationConfig(
    "auth0.domain" to "test.auth0.com",
    "auth0.audience" to "test-audience",
    "auth0.namespace" to "https://buildagent.test/"
)

fun testToken(
    agencyId: String = TEST_AGENCY_ID,
    userId: String = TEST_USER_ID,
    email: String = "admin@test.com",
    role: String = "ADMIN"
): String = TEST_JWT_SERVICE.issue(userId, agencyId, email, role)

fun ApplicationTestBuilder.configureTestApp(block: Application.() -> Unit = {}) {
    environment {
        config = TEST_APP_CONFIG
    }
    application {
        configureSerialization()
        configureStatusPages()
        configureAuthentication(TEST_JWT_SERVICE)
        block()
    }
}

/**
 * Convenience to configure test app with routes registered under /api/v1.
 */
fun ApplicationTestBuilder.configureTestRoutes(routeBlock: Route.() -> Unit) {
    configureTestApp {
        routing {
            route("/api/v1") {
                routeBlock()
            }
        }
    }
}

fun ApplicationTestBuilder.jsonClient(): HttpClient =
    createClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; encodeDefaults = true })
        }
    }
