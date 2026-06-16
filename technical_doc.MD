# BuildAgent Platform — Technical Architecture Document

**Version:** 1.0 — Initial Draft
**Stack:** React / Node.js / PostgreSQL / AWS
**Date:** June 2026
**Classification:** Confidential

---

## 1. Architecture Overview

BuildAgent follows a layered, service-oriented architecture (SOA) with clear separation between the presentation layer, application services, and data storage. The system is designed for multi-tenant SaaS deployment on AWS, with all components containerized via Docker and orchestrated with Kubernetes.

> **Architecture Pattern:** Multi-tier, domain-driven microservices. Each bounded context (Portfolio, Tenancy, Payments, Maintenance) is an independently deployable service with its own data store, communicating over a shared internal API gateway.

### 1.1 High-Level Component Architecture

The platform consists of five primary layers:

- **Presentation Layer** — React SPA (web) + React Native (mobile, Phase 2)
- **API Gateway** — Kong Gateway for routing, rate limiting, and authentication
- **Application Services** — Node.js microservices (one per domain module)
- **Data Layer** — PostgreSQL (relational), Redis (cache/sessions), S3 (documents)
- **Infrastructure** — AWS EKS (Kubernetes), RDS, ElastiCache, CloudFront CDN

### 1.2 Deployment Model

| Environment | Purpose | Infrastructure |
|---|---|---|
| Development | Feature development & unit testing | Local Docker Compose |
| Staging | Integration testing & UAT | AWS EKS — dev cluster |
| Production | Live customer traffic | AWS EKS — prod cluster (multi-AZ) |
| DR | Disaster recovery failover | AWS secondary region (cold standby) |

---

## 2. Technology Stack

| Layer | Technology | Rationale |
|---|---|---|
| Frontend | React 18 + TypeScript | Component reuse, strong typing, ecosystem maturity |
| UI Library | Tailwind CSS + Shadcn/UI | Rapid prototyping, accessible components |
| State Mgmt | Zustand + React Query | Lightweight global state; server-state caching |
| API Gateway | Kong Gateway | Auth, rate limiting, routing, plugin ecosystem |
| Backend | Node.js 20 + Express | JavaScript consistency across stack, async I/O |
| ORM | Prisma | Type-safe DB access, migrations, schema-first |
| Primary DB | PostgreSQL 16 (AWS RDS) | ACID transactions, strong relational model |
| Cache / Queue | Redis 7 (ElastiCache) | Session storage, job queues, real-time pub/sub |
| Document Store | AWS S3 + CloudFront | Scalable file storage with signed URL delivery |
| Auth | Auth0 (OIDC/JWT) | Managed auth, MFA, social login, RBAC |
| Search | PostgreSQL Full-Text Search | Phase 1; Elasticsearch planned for Phase 2 |
| Email / SMS | SendGrid + Twilio | Transactional notifications and rent reminders |
| Monitoring | Datadog APM + Logs | Unified observability, alerting, SLO tracking |
| CI/CD | GitHub Actions + ArgoCD | GitOps deployment pipeline to Kubernetes |
| IaC | Terraform | Reproducible AWS infrastructure as code |

---

## 3. Core Data Model

The following describes the primary entities and their key relationships in the BuildAgent platform. The database is PostgreSQL with row-level security (RLS) policies enforcing multi-tenancy at the data layer.

### 3.1 Entity Overview

| Entity | Table Name | Key Attributes |
|---|---|---|
| Agency | `agencies` | id, name, subscription_tier, created_at |
| Agent (User) | `users` | id, agency_id, role, email, auth0_sub |
| Client (Owner) | `clients` | id, agency_id, full_name, email, phone |
| Building | `buildings` | id, agency_id, client_id, address, type, unit_count |
| Unit | `units` | id, building_id, number, floor, bedrooms, rent_amount, status |
| Tenant | `tenants` | id, agency_id, full_name, email, phone, emergency_contact |
| Lease | `leases` | id, unit_id, tenant_id, start_date, end_date, rent, bond |
| Payment | `payments` | id, lease_id, amount, type, status, period_from, period_to |
| Maintenance | `maintenance_requests` | id, unit_id, reported_by, status, priority, description |
| Document | `documents` | id, entity_type, entity_id, s3_key, filename, uploaded_by |
| Audit Log | `audit_events` | id, actor_id, action, entity_type, entity_id, diff_json |

### 3.2 Key Relationships

- Agency (1) → (N) Users, Buildings, Clients, Tenants
- Client (1) → (N) Buildings
- Building (1) → (N) Units
- Unit (1) → (N) Leases [one active at a time], Maintenance Requests
- Lease (1) → (N) Payments
- Documents are polymorphic — linked to any entity via `(entity_type, entity_id)`
- All financial mutations write an immutable row to `audit_events`

### 3.3 Sample Schema: payments table

```sql
CREATE TABLE payments (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  lease_id        UUID NOT NULL REFERENCES leases(id),
  agency_id       UUID NOT NULL REFERENCES agencies(id),
  amount          NUMERIC(10,2) NOT NULL,
  payment_type    TEXT NOT NULL CHECK (payment_type IN ('rent','bond','water','fee','other')),
  status          TEXT NOT NULL DEFAULT 'pending'
                  CHECK (status IN ('pending','received','overdue','waived')),
  period_from     DATE NOT NULL,
  period_to       DATE NOT NULL,
  reference_no    TEXT,
  notes           TEXT,
  recorded_by     UUID REFERENCES users(id),
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  updated_at      TIMESTAMPTZ DEFAULT NOW()
);
```

---

## 4. API Design

### 4.1 API Style

BuildAgent exposes a RESTful JSON API following JSON:API conventions. All endpoints are versioned (`/api/v1/`) and require a valid JWT bearer token. Rate limiting is enforced at the gateway level.

### 4.2 Core Endpoints

| Method | Endpoint | Auth Role | Description |
|---|---|---|---|
| GET | `/api/v1/buildings` | Agent, Admin | List all buildings for the agency |
| POST | `/api/v1/buildings` | Agent, Admin | Register a new building |
| GET | `/api/v1/buildings/:id` | Agent, Client | Get building details with units |
| GET | `/api/v1/buildings/:id/units` | Agent, Client | List units for a building |
| GET | `/api/v1/units/:id/payments` | Agent, Client | Payment history for a unit |
| POST | `/api/v1/payments` | Agent, Admin | Record a new payment |
| PATCH | `/api/v1/payments/:id` | Agent, Admin | Update payment status/details |
| GET | `/api/v1/clients/:id/portfolio` | Agent, Client | Full portfolio summary for a client |
| GET | `/api/v1/leases/:id` | Agent, Tenant | Lease details and payment schedule |
| POST | `/api/v1/maintenance` | Agent, Tenant | Create maintenance request |
| GET | `/api/v1/dashboard/agent` | Agent | Agent's portfolio summary dashboard |
| GET | `/api/v1/dashboard/client` | Client | Client's portfolio summary dashboard |
| GET | `/api/v1/reports/rent-roll` | Agent, Admin | Full rent roll for the agency |

### 4.3 Authentication & Authorization

- Authentication via Auth0 OIDC; all API calls require `Bearer JWT` in `Authorization` header
- JWTs contain claims: `agency_id`, `user_id`, `role` (agent | client | tenant | admin)
- Kong validates JWT signature and expiry before forwarding to services
- Row-level security in PostgreSQL enforces `agency_id` isolation at DB level
- Clients (owners) can only query buildings/payments linked to their client record

---

## 5. Frontend Architecture

### 5.1 Application Structure

```
src/
  components/         # Shared UI components (Button, Table, Modal, Badge...)
  features/           # Domain feature modules
    portfolio/        # Buildings, units views
    tenancy/          # Tenants, leases
    payments/         # Payment ledger, recording, history
    maintenance/      # Requests, job tracking
    reports/          # Rent roll, owner statements
    client-portal/    # Owner-facing views
  hooks/              # Custom React hooks (useBuildings, usePayments...)
  services/           # API client layer (React Query + Axios)
  store/              # Zustand global state
  router/             # React Router v6 with role-based route guards
  utils/              # Formatters, validators, currency helpers
```

### 5.2 Key UI Views

| View | Path | Features |
|---|---|---|
| Agent Dashboard | `/dashboard` | Portfolio summary, overdue payments, pending maintenance |
| Buildings List | `/buildings` | All buildings with status, search, filter, add new |
| Building Detail | `/buildings/:id` | Units grid, occupancy status, documents |
| Payment Ledger | `/buildings/:id/payments` | Transaction list, record payment, export |
| Unit Detail | `/units/:id` | Lease, tenant, payment history, maintenance |
| Tenant Profile | `/tenants/:id` | Lease history, contacts, documents |
| Maintenance Hub | `/maintenance` | All requests by building, priority, status |
| Client Portal | `/portal` | Owner-facing: portfolio, income, statements |
| Reports | `/reports` | Rent roll, vacancy report, income summary |

---

## 6. Security Architecture

### 6.1 Security Controls

| Control | Implementation | Details |
|---|---|---|
| Authentication | Auth0 OIDC + MFA | JWT with RS256 signing; MFA enforced for agents/admins |
| Authorization | RBAC + PostgreSQL RLS | Role claims in JWT; DB row-level isolation by agency_id |
| Data in Transit | TLS 1.3 | Enforced on all endpoints; HSTS headers |
| Data at Rest | AES-256 | RDS storage encryption; S3 SSE-S3 |
| API Security | Kong rate limiting | 100 req/min per user; 1000/min per agency |
| Input Validation | Zod schemas | Server-side validation on all inputs; parameterized queries |
| Audit Logging | audit_events table | All financial mutations logged with actor, diff, timestamp |
| Secrets Mgmt | AWS Secrets Manager | No secrets in env files or code; rotated quarterly |
| SAST / DAST | Snyk + OWASP ZAP | Integrated into CI pipeline; blocks on critical findings |

---

## 7. Infrastructure & DevOps

### 7.1 AWS Architecture

- VPC with public (ALB, NAT), private (EKS nodes, RDS), and isolated (data) subnets
- EKS cluster with separate node groups: general, memory-optimised (Redis), and spot (batch jobs)
- RDS PostgreSQL Multi-AZ with read replica for reporting queries
- ElastiCache Redis cluster for sessions and BullMQ job queues
- S3 buckets per environment with versioning, lifecycle policies, and cross-region replication
- CloudFront CDN for static assets and document delivery via signed URLs
- Route 53 with health-check failover to DR region

### 7.2 CI/CD Pipeline

1. Developer pushes to feature branch → PR opened
2. GitHub Actions runs: lint, type-check, unit tests, SAST (Snyk)
3. PR merge to `main` → Docker images built and pushed to ECR
4. ArgoCD detects image tag change → deploys to staging EKS cluster
5. Integration + E2E tests run on staging (Playwright)
6. Manual approval gate → ArgoCD promotes to production (blue/green deploy)

---

## 8. Observability

### 8.1 Monitoring & Alerting

| Signal | Tool | Key Metrics / Alerts |
|---|---|---|
| Application Metrics | Datadog APM | P95 latency, error rate, throughput per service |
| Infrastructure | Datadog + CloudWatch | CPU, memory, disk, pod restarts, RDS connections |
| Logs | Datadog Log Mgmt | Structured JSON logs; correlated by trace_id |
| Uptime / SLO | Datadog Synthetics | Synthetic checks every 60s; alert if p95 > 2s |
| Error Tracking | Sentry | Frontend and backend error grouping, stack traces |
| Payments Alerts | Custom Datadog Monitor | Alert if payment recording drops > 50% vs prior week |

---

## 9. Phased Delivery Roadmap

| Phase | Scope | Timeline | Dependencies |
|---|---|---|---|
| Alpha | Internal dev build: auth, buildings, units, tenants, manual payments | Weeks 1–6 | Auth0 setup, AWS infra, DB schema |
| Beta | Client portal, maintenance hub, document storage, reporting MVP | Weeks 7–14 | S3, email integration |
| GA v1 | Full Phase 1 scope, UAT complete, production launch | Weeks 15–18 | Penetration test, SLA agreement |
| v2.0 | Auto-reminders, AI lease scoring, payment gateway, mobile app | Q4 2026 | Stripe, React Native |

---

## 10. Technical Risks & Mitigations

| Risk | Severity | Mitigation |
|---|---|---|
| Multi-tenant data isolation breach | Critical | PostgreSQL RLS + agency_id on every query; automated tests for cross-tenant access |
| Payment audit log tampering | High | Immutable append-only audit table; DB-level trigger prevents UPDATE/DELETE |
| Auth0 service outage | High | Session caching in Redis; planned fallback to in-house JWT issuance in Phase 2 |
| RDS performance under load | Medium | Read replica for reports; connection pooling via PgBouncer; query index reviews |
| File storage costs exceeding budget | Medium | S3 lifecycle policy: 30-day infrequent access after upload, Glacier after 1 year |
| Scope creep on Phase 1 | Medium | Strict SDC sign-off; change request process; weekly stakeholder demos |

---

## 11. Glossary

| Term | Definition |
|---|---|
| Agency | The property management business operating BuildAgent |
| Agent | A user within the agency who manages buildings and clients |
| Client | A property owner whose buildings are managed by the agency |
| Lease | A formal tenancy agreement between a tenant and a unit |
| RLS | Row-Level Security — PostgreSQL feature enforcing data isolation |
| Rent Roll | A report listing all units, tenants, rent amounts, and payment status |
| SLA | Service Level Agreement — uptime and performance commitments |
| JWT | JSON Web Token — signed token used for API authentication |
| SOA | Service-Oriented Architecture — modular service design pattern |
| RBAC | Role-Based Access Control — permissions assigned by user role |

---

*BuildAgent Technical Architecture Document v1.0 — Confidential*
