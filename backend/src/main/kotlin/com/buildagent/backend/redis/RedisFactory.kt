package com.buildagent.backend.redis

import io.ktor.server.config.*
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import org.slf4j.LoggerFactory

object RedisFactory {
    private val log = LoggerFactory.getLogger("RedisFactory")
    private lateinit var client: RedisClient
    private lateinit var connection: StatefulRedisConnection<String, String>

    val commands: RedisCommands<String, String> get() = connection.sync()

    fun init(config: ApplicationConfig) {
        val url = config.property("redis.url").getString()
        client = RedisClient.create(url)
        connection = client.connect()
        log.info("Redis connected: $url")
    }

    suspend fun get(key: String): String? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        commands.get(key)
    }

    suspend fun set(key: String, value: String, ttlSeconds: Long = 30) =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            commands.setex(key, ttlSeconds, value)
        }

    suspend fun del(key: String) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        commands.del(key)
    }

    suspend fun incr(key: String): Long = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        commands.incr(key)
    }

    fun close() {
        connection.close()
        client.shutdown()
        log.info("Redis disconnected")
    }
}
