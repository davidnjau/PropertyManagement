# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## Project Status

This repository is currently **documentation-only**. No source code exists yet. The two reference documents are:

- `technical_doc.MD` — Full technical architecture (stack, data model, API design, security, infra, CI/CD)
- `sdc.MD` — System Design Concept (business requirements, user roles, user journeys, NFRs)

---

## What Is BuildAgent

A multi-tenant SaaS property management platform for building agents, property owners (clients), and tenants. It replaces manual spreadsheet-based workflows with a unified, auditable system.

**Five core modules:**
| Module | Purpose |
|---|---|
| M1 Portfolio Manager | Register buildings/units, link to owner clients |
| M2 Tenancy Manager | Manage tenants, leases, occupancy |
| M3 Payment Ledger | Track rent/bond/fee transactions with immutable audit trail |
| M4 Maintenance Hub | Log and track maintenance requests |
| M5 Client Portal | Owner-facing dashboard, statements, reports |

---

## Planned Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 18 + TypeScript, Tailwind CSS + Shadcn/UI |
| State | Zustand (global) + React Query (server state) |
| Backend | Node.js 20 + Express |
| ORM | Prisma (schema-first, type-safe) |
| Database | PostgreSQL 16 (AWS RDS, Multi-AZ) |
| Cache/Queue | Redis 7 (ElastiCache) — sessions + BullMQ |
| Auth | Auth0 OIDC (JWT RS256, MFA for agents/admins) |
| File Storage | AWS S3 + CloudFront (signed URLs) |
| API Gateway | Kong (routing, rate limiting, JWT validation) |
| Email/SMS | SendGrid + Twilio |
| Monitoring | Datadog APM + Sentry |
| IaC | Terraform (AWS) |
| CI/CD | GitHub Actions + ArgoCD (GitOps, blue/green to EKS) |

---

## Data Model (Key Rules)

- **All tables scoped to `agency_id`** — multi-tenancy enforced via PostgreSQL Row-Level Security (RLS). Every query must include the `agency_id` from the JWT.
- **Immutable audit log** — `audit_events` table has a DB-level trigger blocking UPDATE/DELETE. All financial mutations append a row with `actor_id`, `action`, `entity_type`, `entity_id`, `diff_json`.
- **Documents are polymorphic** — linked via `(entity_type, entity_id)` to any entity.
- **One active lease per unit** at a time; units have a `status` field (vacant/occupied).

Primary entities: `agencies`, `users`, `clients`, `buildings`, `units`, `tenants`, `leases`, `payments`, `maintenance_requests`, `documents`, `audit_events`.

---

## API Design

- RESTful JSON, versioned at `/api/v1/`
- All requests require `Authorization: Bearer <JWT>`
- JWT claims carry: `agency_id`, `user_id`, `role` (admin | agent | client | tenant)
- Kong validates JWT before forwarding; PostgreSQL RLS enforces agency isolation at the DB layer
- Rate limits: 100 req/min per user, 1000/min per agency

---

## Frontend Structure (Planned)

```
src/
  components/     # Shared UI (Button, Table, Modal, Badge)
  features/       # Domain modules: portfolio/, tenancy/, payments/, maintenance/, reports/, client-portal/
  hooks/          # Custom hooks per domain (useBuildings, usePayments…)
  services/       # Axios + React Query API client layer
  store/          # Zustand global state
  router/         # React Router v6 with role-based route guards
  utils/          # Formatters, validators, currency helpers
```

---

## Security Requirements

- Authorize on every action using the `role` JWT claim — do not rely on authentication alone
- Never bypass PostgreSQL RLS — always pass `agency_id` in DB queries
- Server-side input validation with Zod schemas; parameterized queries only (no string interpolation in SQL)
- Secrets via AWS Secrets Manager only — no `.env` files containing real secrets in any environment
- SAST (Snyk) + DAST (OWASP ZAP) run in CI; critical findings block merge

---

## Roles & Permissions Summary

| Action | Admin | Agent | Client | Tenant |
|---|---|---|---|---|
| Add/Edit Buildings | Yes | Yes | View | No |
| Manage Tenants | Yes | Yes | View | Own record |
| Record Payments | Yes | Yes | No | No |
| Maintenance Requests | Yes | Yes | View | Create/view own |
| Manage Users | Yes | No | No | No |

---

## Delivery Phases

- **Alpha (Weeks 1–6):** Auth, buildings, units, tenants, manual payments
- **Beta (Weeks 7–14):** Client portal, maintenance hub, document storage, reporting MVP
- **GA v1 (Weeks 15–18):** Full Phase 1, UAT, production launch
- **v2.0 (Q4 2026):** Auto-reminders, AI lease scoring, Stripe, React Native mobile
