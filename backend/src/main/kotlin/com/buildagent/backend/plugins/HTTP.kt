package com.buildagent.backend.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.ratelimit.*
import org.slf4j.event.Level
import kotlin.time.Duration.Companion.minutes

fun Application.configureHTTP() {
    install(CallLogging) { level = Level.INFO }

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowCredentials = true
        anyHost() // tighten in production
    }

    install(RateLimit) {
        register(RateLimitName("user")) {
            rateLimiter(limit = 100, refillPeriod = 1.minutes)
        }
    }
}
