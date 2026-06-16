# BuildAgent Platform — System Design Concept (SDC)

**Version:** 1.0 — Initial Draft
**Date:** June 2026
**Classification:** Confidential

---

## 1. Executive Summary

BuildAgent is a full-stack property management platform designed for building agents, portfolio owners, and their clients. The system provides a unified workspace to manage buildings, tenants, lease agreements, maintenance tasks, and payment tracking — all accessible from a single, role-based interface.

> **Vision Statement:** To deliver a modern, AI-assisted property management platform that replaces fragmented spreadsheets and paper-based workflows with a real-time, collaborative, and auditable system — enabling building agents to manage more properties with less effort, and clients to maintain complete visibility over their portfolio.

---

## 2. Problem Statement

### 2.1 Current Pain Points

Building agents and property managers currently face several operational challenges:

- No centralized view of all buildings, units, and tenancy status
- Payment tracking spread across bank statements, spreadsheets, and emails
- Manual, error-prone rent roll reconciliation each month
- Poor client visibility — clients must chase agents for updates
- No audit trail for maintenance work, inspections, or financial decisions
- Fragmented communication between agents, tenants, and contractors

### 2.2 Stakeholders

| Stakeholder | Role | Key Needs |
|---|---|---|
| Building Agent | Primary operator of the platform | Manage portfolio, track payments, schedule maintenance, report to clients |
| Property Owner / Client | Receives reports and oversight | Portfolio visibility, payment summaries, document access |
| Tenant | Occupies units, makes payments | Pay rent, log maintenance requests, view lease |
| Contractor / Tradesperson | Completes maintenance work | Receive job orders, update job status, submit invoices |
| System Admin | Maintains platform | User management, billing, system health |

---

## 3. Scope

### 3.1 In Scope — Phase 1 (MVP)

- Building and unit registration with property details
- Client (owner) profile management and portfolio linking
- Tenant management: leases, move-in/out, contact info
- Payment tracking: rent, deposits, fees — with status indicators
- Dashboard with portfolio overview per agent and per client
- Maintenance request lifecycle (log, assign, complete, close)
- Document storage: leases, inspection reports, invoices
- Role-based access control: Agent, Client, Tenant, Admin

### 3.2 In Scope — Phase 2

- Automated rent reminders and overdue payment escalation
- AI-assisted lease renewal suggestions and risk scoring
- Contractor portal for job management and invoicing
- Financial reporting and owner statements (PDF export)
- Integration with payment gateways (Stripe / bank feeds)
- Mobile application for agents and tenants

### 3.3 Out of Scope

- Full accounting or ERP functionality (QuickBooks integration planned for Phase 3)
- Building automation / IoT sensor integration
- Legal document drafting beyond template generation

---

## 4. System Overview

BuildAgent is structured around five core modules, each corresponding to a domain of property management:

| Module | Name | Description |
|---|---|---|
| M1 | Portfolio Manager | Register buildings, units, and link them to owner clients |
| M2 | Tenancy Manager | Manage tenant records, leases, occupancy status |
| M3 | Payment Ledger | Track all rent, deposit, and fee transactions with audit trail |
| M4 | Maintenance Hub | Log and manage maintenance requests and contractor jobs |
| M5 | Client Portal | Owner-facing dashboard with portfolio summaries and reports |

---

## 5. User Roles & Permissions

| Permission | Admin | Agent | Client | Tenant |
|---|---|---|---|---|
| Add/Edit Buildings | Yes | Yes | View Only | No |
| Manage Tenants | Yes | Yes | View Only | Own Record |
| View All Payments | Yes | Yes | Own Portfolio | Own Payments |
| Record Payments | Yes | Yes | No | No |
| Maintenance Requests | Yes | Yes | View Only | Create/View Own |
| Generate Reports | Yes | Yes | View Own Reports | No |
| Manage Users | Yes | No | No | No |

---

## 6. Key User Journeys

### 6.1 Agent: Onboarding a New Building

1. Agent logs in and selects "Add Building" from the Portfolio Manager
2. Enters building details: address, type, number of units, council rates, insurance details
3. Links building to an existing client (owner) or creates a new client profile
4. Adds individual units with attributes (size, floor, parking, rent amount)
5. Uploads building documents (insurance certificate, compliance records)
6. Building appears in agent dashboard and client portal immediately

### 6.2 Agent: Recording a Payment

1. Agent navigates to the Payment Ledger for a specific unit or tenant
2. Clicks "Record Payment" and selects payment type: Rent, Bond, Water, Maintenance Fee
3. Enters amount, date, reference number, and notes
4. System marks the period as paid; updates outstanding balance; logs audit entry
5. Client sees updated payment status in their portal in real time

### 6.3 Client: Viewing Portfolio

- Client logs in to the Client Portal (web or mobile)
- Sees summary dashboard: total buildings, occupied vs vacant units, monthly income, outstanding payments
- Drills into a specific building to see individual unit status
- Views month-by-month payment history and downloads statements
- Reviews maintenance activity and outstanding work orders

---

## 7. Non-Functional Requirements

| Category | Requirement | Target |
|---|---|---|
| Performance | Dashboard load time | < 2 seconds (95th percentile) |
| Availability | Platform uptime | 99.9% monthly SLA |
| Security | Data encryption | AES-256 at rest, TLS 1.3 in transit |
| Compliance | Data privacy | GDPR / local tenancy law compliant |
| Scalability | Concurrent users | Up to 10,000 concurrent users (Phase 1) |
| Audit | All financial transactions | Full immutable audit log retained 7 years |
| Accessibility | Web interface | WCAG 2.1 AA compliant |
| Backup | Data recovery | RPO 1 hour, RTO 4 hours |

---

## 8. Assumptions & Constraints

### 8.1 Assumptions

- Building agents have reliable internet access for day-to-day operations
- Clients (property owners) will interact via web portal primarily
- Tenants will primarily interact via mobile-friendly web interface
- Initial deployment targets single-country tenancy law jurisdiction
- Payment recording is manual in Phase 1; automated reconciliation in Phase 2

### 8.2 Constraints

- Phase 1 budget limits the use of third-party payment gateway integration
- System must be deployable on-premise or cloud (multi-tenant SaaS model)
- Document storage limited to 10 GB per agency in Phase 1 (expandable)

---

## 9. Success Metrics

| Metric | Phase 1 Target | Phase 2 Target |
|---|---|---|
| Time to onboard a new building | < 10 minutes | < 5 minutes (auto-fill) |
| Monthly payment reconciliation time | < 30 minutes | < 5 minutes (automated) |
| Client portal adoption rate | 70% of clients active | 90% of clients active |
| Payment overdue detection latency | Same day | Real-time |
| Agent-reported satisfaction score | > 7/10 | > 9/10 |

---

## 10. Document Approval

| Role | Name | Signature / Date |
|---|---|---|
| Product Owner | | |
| Lead Architect | | |
| QA Lead | | |
| Client Representative | | |

---

*BuildAgent SDC v1.0 — Confidential*
