FROM gradle:8.7-jdk17-alpine AS build
WORKDIR /app
COPY gradle/ gradle/
COPY gradle.properties settings.gradle.kts build.gradle.kts ./
COPY shared/ shared/
COPY backend/ backend/
RUN gradle :backend:installDist --no-daemon -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/backend/build/install/backend/ .
EXPOSE 3001
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:3001/health || exit 1
CMD ["./bin/backend"]
