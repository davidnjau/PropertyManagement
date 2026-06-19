# Stage 1: Build (gradle image already has Gradle installed — no gradlew needed)
FROM gradle:8.7-jdk21 AS builder

WORKDIR /build

# Copy version catalog and build scripts first (layer cache for deps)
COPY gradle/libs.versions.toml gradle/libs.versions.toml
COPY gradle.properties .
COPY settings.gradle.kts .
COPY build.gradle.kts .
COPY shared/build.gradle.kts shared/
COPY backend/build.gradle.kts backend/
COPY composeApp/build.gradle.kts composeApp/

# Warm dependency cache
RUN gradle :backend:dependencies --no-daemon --quiet || true

# Copy full source
COPY shared/ shared/
COPY backend/ backend/

# Build the installable distribution
RUN gradle :backend:installDist --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=builder /build/backend/build/install/backend/ .

EXPOSE 3001

HEALTHCHECK --interval=20s --timeout=10s --retries=5 --start-period=30s \
    CMD curl -sf http://localhost:3001/health || exit 1

CMD ["bin/backend"]
