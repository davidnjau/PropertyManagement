# BuildAgent

A multi-tenant SaaS property management platform for building agents, property owners, and tenants. Replaces manual spreadsheet-based workflows with a unified, auditable system.

---

## What It Does

Five core modules:

| Module | Purpose |
|---|---|
| Portfolio Manager | Register buildings and units, link to owner clients |
| Tenancy Manager | Manage tenants, leases, and occupancy |
| Payment Ledger | Track rent, bond, and fee transactions with an immutable audit trail |
| Maintenance Hub | Log and track maintenance requests with SLA monitoring |
| Dashboard | Portfolio overview — occupancy, overdue payments, expiring leases |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Kotlin + Ktor 2.3.12 (Netty) |
| ORM | Jetbrains Exposed 0.54.0 + exposed-kotlin-datetime |
| Database | PostgreSQL 16 (HikariCP connection pool) |
| Cache | Redis 7 (Lettuce client) |
| Messaging | Apache Kafka 3.7.0 |
| Auth | Auth0 OIDC — JWT RS256 via `ktor-server-auth-jwt` |
| DI | Koin 4.0.0 |
| Frontend | Kotlin Multiplatform Compose (desktop + Android + iOS targets) |
| Navigation | Voyager 1.1.0-beta03 |
| HTTP Client | Ktor Client (OkHttp on Android/desktop, Darwin on iOS) |
| Serialization | kotlinx.serialization |
| Date/Time | kotlinx.datetime |
| Build | Gradle 8.7 (Kotlin DSL), version catalog (`gradle/libs.versions.toml`) |
| Infra | Docker Compose (Postgres, Redis, Zookeeper, Kafka, Kafka UI) |

---

## Project Layout

```
BuildAgent/
├── backend/                        # Ktor server (JVM only)
│   ├── src/main/kotlin/com/buildagent/backend/
│   │   ├── Application.kt          # Entry point — wires Koin, DB, Redis, Kafka, routes
│   │   ├── auth/
│   │   │   └── JwtConfig.kt        # Auth0 JWT validation, AgentPrincipal
│   │   ├── db/
│   │   │   ├── DatabaseFactory.kt  # HikariCP + Exposed schema creation
│   │   │   └── tables/Tables.kt    # All Exposed table definitions
│   │   ├── di/BackendModule.kt     # Koin module for all services
│   │   ├── kafka/                  # KafkaFactory + DomainEventProducer
│   │   ├── plugins/                # Serialization, HTTP (CORS/rate-limit), StatusPages, Routing
│   │   ├── redis/RedisFactory.kt   # Lettuce connection
│   │   ├── routes/                 # One file per domain: buildings, units, tenants, leases, payments, maintenance, dashboard
│   │   ├── scheduler/ScheduledJobs.kt  # Lease expiry + SLA breach background jobs
│   │   └── services/               # Business logic: Building, Unit, Tenant, Lease, Payment, Maintenance, Dashboard, Audit
│   ├── build.gradle.kts
│   ├── Dockerfile                  # Multi-stage: gradle:8.7-jdk17 builder → eclipse-temurin:17-jre runtime
│   └── src/main/resources/application.conf
│
├── shared/                         # Kotlin Multiplatform library (JVM + desktop + Android + iOS)
│   └── src/commonMain/kotlin/com/buildagent/shared/
│       ├── api/BuildAgentClient.kt # Ktor HTTP client for all API calls
│       ├── di/SharedModule.kt      # Koin shared module
│       ├── domain/                 # Formatters, LeaseUtils, SlaUtils
│       ├── events/DomainEvents.kt  # Kafka event payload models
│       └── models/                 # Models.kt, Dto.kt, Enums.kt
│
├── composeApp/                     # Kotlin Multiplatform Compose frontend
│   └── src/
│       ├── commonMain/kotlin/com/buildagent/ui/
│       │   ├── App.kt              # Root composable + Voyager navigator
│       │   ├── components/         # AppDrawer, LoadingContent, StatCard, StatusBadge
│       │   ├── di/AppModule.kt     # Koin UI module (ViewModels)
│       │   ├── screens/
│       │   │   ├── dashboard/      # DashboardScreen + DashboardViewModel
│       │   │   ├── portfolio/      # BuildingsScreen + PortfolioViewModel
│       │   │   ├── tenancy/        # TenantsScreen + LeasesScreen + TenancyViewModel
│       │   │   ├── payments/       # PaymentLedgerScreen + PaymentsViewModel
│       │   │   └── maintenance/    # MaintenanceHubScreen + MaintenanceViewModel
│       │   ├── state/AuthState.kt
│       │   └── theme/              # Color.kt, Theme.kt (Material 3)
│       ├── desktopMain/            # Desktop entry point (main.kt — Compose window)
│       ├── androidMain/            # Android MainActivity
│       └── iosMain/                # iOS MainViewController
│
├── docker-compose.yml              # Postgres, Redis, Zookeeper, Kafka, Kafka UI, backend
├── Makefile                        # Convenience targets (see below)
├── gradle/libs.versions.toml       # Central version catalog
├── settings.gradle.kts
├── build.gradle.kts
└── gradlew / gradlew.bat
```

---

## Running the Stack

### Prerequisites

- Docker Desktop (with Compose v2)
- JDK 17+
- Gradle 8.7 (or use the included `gradlew`)

### 1. Start infrastructure + backend

```bash
make up
```

Starts: PostgreSQL (5433), Redis (6380), Zookeeper (2181), Kafka (9092), Kafka UI (8090), backend API (3001).

First run — create Kafka topics after services are healthy:

```bash
make topics-create
```

### 2. Run the desktop frontend

```bash
./gradlew :composeApp:run
```

Opens the BuildAgent desktop window. Reads `API_URL` (default: `http://localhost:3001`) and `API_TOKEN` from environment.

### 3. Run the backend locally (without Docker)

```bash
make backend-run
```

Requires Postgres/Redis/Kafka already running (e.g. via `make infra-up`).

### Makefile targets

| Target | What it does |
|---|---|
| `make infra-up` | Start Postgres, Redis, Zookeeper, Kafka, Kafka UI only |
| `make infra-down` | Stop all containers |
| `make up` | Start full stack including backend |
| `make down` | Stop all containers and remove volumes |
| `make logs` | Tail backend container logs |
| `make topics-create` | Create all required Kafka topics |
| `make docker-build` | Build the backend Docker image |
| `make backend-run` | Run backend locally via Gradle |
| `make web-dev` | Run Compose Web frontend locally (dev server, hot reload) |

### Service ports

| Service | Port |
|---|---|
| Backend API | 3001 |
| PostgreSQL | 5433 |
| Redis | 6380 |
| Kafka | 9092 |
| Kafka UI | 8090 |

---

## Backend API

All routes are under `/api/v1/` and require `Authorization: Bearer <JWT>`.

| Resource | Endpoints |
|---|---|
| Health | `GET /health` |
| Buildings | `GET/POST /buildings`, `GET/PUT/DELETE /buildings/{id}` |
| Units | `GET/POST /units`, `GET/PUT/DELETE /units/{id}` |
| Tenants | `GET/POST /tenants`, `GET/PUT/DELETE /tenants/{id}` |
| Leases | `GET/POST /leases`, `GET/PUT /leases/{id}`, `POST /leases/{id}/terminate` |
| Payments | `GET/POST /payments`, `GET /payments/{id}`, `POST /payments/{id}/adjust`, `GET /payments/overdue` |
| Maintenance | `GET/POST /maintenance`, `GET /maintenance/{id}`, `PUT /maintenance/{id}/status`, `POST /maintenance/{id}/close` |
| Dashboard | `GET /dashboard` |

JWT claims required: `agency_id`, `user_id`, `role`. All queries are scoped to `agency_id` from the token.

---

## Data Model

Primary tables (all scoped to `agency_id`):

`agencies` → `users`, `clients`, `buildings` → `units` → `leases` → `payments`

`tenants` → `leases`

`maintenance_requests` → linked to `units`

`audit_events` — append-only log of all mutations (actor, action, entity, diff JSON)

---

## Auth

Auth0 OIDC with RS256 JWT. The backend validates tokens against Auth0's JWKS endpoint:

```
https://<AUTH0_DOMAIN>/.well-known/jwks.json
```

Custom namespace claims carry `agency_id`, `user_id`, and `role`. Configured in `backend/src/main/resources/application.conf`:

```hocon
auth0 {
  domain = ${?AUTH0_DOMAIN}
  audience = ${?AUTH0_AUDIENCE}
  namespace = ${?AUTH0_NAMESPACE}
}
```

---

## Kafka Topics

| Topic | Partitions | Purpose |
|---|---|---|
| `buildagent.payments.recorded` | 3 | New payment recorded |
| `buildagent.payments.adjusted` | 3 | Payment adjustment |
| `buildagent.leases.created` | 3 | New lease |
| `buildagent.leases.expiring` | 3 | Lease expiring within 60 days (scheduler) |
| `buildagent.leases.terminated` | 3 | Lease terminated |
| `buildagent.maintenance.created` | 3 | New maintenance request |
| `buildagent.maintenance.status-changed` | 3 | Status update |
| `buildagent.maintenance.sla-breached` | 3 | SLA breach detected (scheduler) |
| `buildagent.notifications.email` | 1 | Email notification trigger |
| `buildagent.audit.events` | 1 | Audit log events |

---

## Scheduled Jobs

Two background jobs run on startup (via `ScheduledJobs`):

- **Lease expiry checker** — runs every 6 hours. Finds leases ending within 60 days, publishes to `buildagent.leases.expiring`.
- **SLA breach detector** — runs every hour. Finds open maintenance requests past their SLA target date, publishes to `buildagent.maintenance.sla-breached`.

---

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5433/buildagent_dev` | PostgreSQL JDBC URL |
| `DATABASE_USER` | `buildagent` | DB username |
| `DATABASE_PASSWORD` | `buildagent_dev` | DB password |
| `REDIS_URL` | `redis://localhost:6380` | Redis URL |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka brokers |
| `AUTH0_DOMAIN` | — | Auth0 domain (e.g. `yourapp.au.auth0.com`) |
| `AUTH0_AUDIENCE` | — | API audience identifier |
| `AUTH0_NAMESPACE` | — | Custom claims namespace prefix |
| `API_URL` | `http://localhost:3001` | Backend URL (frontend desktop only) |
| `API_TOKEN` | — | Bearer token (frontend desktop only) |

---

## Known Limitations

- **Compose Web (wasmJs) cannot build** — Ktor 2.3.x has no wasmJs artifact. wasmJs support requires upgrading to Ktor 3.x. The `make web-dev` target is present but will fail until that upgrade is done.
- **No automated tests yet** — the test scaffolding is not in place; all validation is manual.
- **No document storage** — file upload (leases, invoices) is modelled but not implemented.

---

## Delivery Roadmap

| Phase | Timeline | Scope |
|---|---|---|
| Alpha | Weeks 1–6 | Auth, portfolio, tenants, manual payments |
| Beta | Weeks 7–14 | Client portal, maintenance, document storage, reporting |
| GA v1 | Weeks 15–18 | Full Phase 1, UAT, production launch |
| v2.0 | Q4 2026 | Auto-reminders, AI lease scoring, Stripe, React Native mobile |
