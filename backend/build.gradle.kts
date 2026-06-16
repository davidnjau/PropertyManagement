plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    application
}

group = "com.buildagent"
version = "1.0.0"

kotlin { jvmToolchain(21) }

application {
    mainClass.set("com.buildagent.backend.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}

dependencies {
    implementation(project(":shared"))

    // Ktor Server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.rate.limit)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.request.validation)
    implementation(libs.ktor.server.swagger)
    implementation(libs.ktor.server.openapi)
    implementation(libs.ktor.serialization.kotlinx.json)

    // Database
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.exposed.json)
    implementation(libs.postgresql)
    implementation(libs.hikaricp)

    // Redis
    implementation(libs.lettuce.core)

    // Kafka
    implementation(libs.kafka.clients)

    // Auth0
    implementation(libs.auth0.java.jwt)
    implementation(libs.auth0.jwks.rsa)

    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.ktor)

    // Logging
    implementation(libs.logback.classic)
    implementation(libs.slf4j.api)

    // Serialization
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
}
