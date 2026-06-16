package com.buildagent.backend

import com.buildagent.backend.auth.LocalJwtService
import com.buildagent.backend.auth.configureAuthentication
import com.buildagent.backend.db.DatabaseFactory
import com.buildagent.backend.di.backendModule
import com.buildagent.backend.kafka.KafkaFactory
import com.buildagent.backend.plugins.*
import com.buildagent.backend.redis.RedisFactory
import com.buildagent.backend.scheduler.ScheduledJobs
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.slf4j.LoggerFactory

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    val log = LoggerFactory.getLogger("Application")

    val jwtSecret = environment.config.property("jwt.secret").getString()
    val localJwtService = LocalJwtService(jwtSecret)

    install(Koin) { modules(backendModule(environment.config, localJwtService)) }

    DatabaseFactory.init(
        url = environment.config.property("database.url").getString(),
        user = environment.config.property("database.user").getString(),
        password = environment.config.property("database.password").getString(),
        maxPoolSize = environment.config.propertyOrNull("database.maxPoolSize")?.getString()?.toIntOrNull() ?: 20
    )
    RedisFactory.init(environment.config)
    KafkaFactory.init(environment.config)

    configureSerialization()
    configureHTTP()
    configureAuthentication(localJwtService)
    configureRouting()
    configureStatusPages()

    val scheduledJobs by inject<ScheduledJobs>()
    scheduledJobs.start()

    log.info("BuildAgent API started on port ${environment.config.property("ktor.deployment.port").getString()}")

    environment.monitor.subscribe(ApplicationStopped) {
        RedisFactory.close()
        KafkaFactory.close()
        log.info("BuildAgent API stopped")
    }
}
