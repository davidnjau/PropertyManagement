# Stage 1: Build the Compose for Web (WasmJS) app
FROM gradle:8.7-jdk17 AS builder

WORKDIR /build

# Copy version catalog and build scripts first (layer cache for deps)
COPY gradle/libs.versions.toml gradle/libs.versions.toml
COPY gradle.properties .
COPY settings.gradle.kts .
COPY build.gradle.kts .
COPY shared/build.gradle.kts shared/
COPY backend/build.gradle.kts backend/
COPY composeApp/build.gradle.kts composeApp/

# Warm Gradle dependency cache
RUN gradle :composeApp:dependencies --no-daemon --quiet || true

# Copy full source
COPY shared/ shared/
COPY composeApp/ composeApp/

# Build the WasmJS distribution
RUN gradle :composeApp:wasmJsBrowserDistribution --no-daemon

# Stage 2: Serve with nginx (alpine supports arm64)
FROM nginx:1.27-alpine

RUN rm /etc/nginx/conf.d/default.conf

COPY composeApp/nginx.conf /etc/nginx/nginx.conf

COPY --from=builder /build/composeApp/build/dist/wasmJs/productionExecutable/ /usr/share/nginx/html/

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD wget -q --spider http://localhost/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
