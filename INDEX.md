# BuildAgent Platform — Agents & Skills Index
**Version:** 1.0 | **Platform:** Claude (Anthropic) | **June 2026**

This index lists all agent and skill files for the BuildAgent platform. Each file contains a ready-to-paste Claude system prompt.

---

## Agents

Agents are domain-specific Claude instances. Each handles a distinct area of the platform and hands off to other agents when a request falls outside its scope.

| File | Agent Name | Role | Primary Users |
|---|---|---|---|
| `agent_portfolio_manager.md` | Portfolio Manager | Register and manage buildings, units, and client links | Agent, Admin |
| `agent_tenancy_manager.md` | Tenancy Manager | Tenant profiles, leases, move-in/out, renewals | Agent, Admin |
| `agent_payment_ledger.md` | Payment Ledger Agent | Record and track all payments, reconciliation, owner financials | Agent, Admin |
| `agent_maintenance_hub.md` | Maintenance Hub Agent | Maintenance request lifecycle, contractor jobs, SLA tracking | Agent, Tenant |
| `agent_client_portal.md` | Client Portal Agent | Owner-facing portfolio summaries, statements, and Q&A | Client (Owner) |

### Agent Handoff Map

```
Portfolio Manager ──────→ Tenancy Manager (lease/tenant queries)
                    └───→ Payment Ledger (financial queries)
                    └───→ Maintenance Hub (maintenance queries)

Tenancy Manager ────────→ Payment Ledger (rent/bond payments)
                    └───→ Maintenance Hub (tenant-raised requests)

Payment Ledger ─────────→ Tenancy Manager (lease context)
                    └───→ Portfolio Manager (unit/building context)

Maintenance Hub ────────→ Payment Ledger (invoice recording)
                    └───→ Tenancy Manager (tenant contact details)

Client Portal ──────────→ [All agents — read-only delegation]
                    └───→ Flags agent action to Portfolio Manager / Payment Ledger
```

---

## Skills

Skills are reusable instruction sets that agents invoke to perform specific tasks. A skill defines exactly what data it needs, how to process it, and what to output.

| File | Skill Name | Purpose | Used By |
|---|---|---|---|
| `skill_rent_roll_generator.md` | Rent Roll Generator | Full rent roll report: units, tenants, payment status | Payment Ledger, Portfolio Manager |
| `skill_owner_statement_builder.md` | Owner Statement Builder | Monthly owner statement: income, deductions, net to owner | Payment Ledger, Client Portal |
| `skill_lease_expiry_checker.md` | Lease Expiry Checker | Scan leases, classify by expiry urgency, surface renewal actions | Tenancy Manager, Portfolio Manager |
| `skill_overdue_detector.md` | Overdue Payment Detector | Identify overdue and partial payments across portfolio | Payment Ledger, Client Portal |
| `skill_tenant_communicator.md` | Tenant Communicator | Draft professional emails/letters for common tenancy scenarios | Tenancy Manager, Payment Ledger, Maintenance Hub |
| `skill_sla_monitor.md` | SLA Monitor | Check open maintenance jobs against priority SLAs, escalate breaches | Maintenance Hub |
| `skill_occupancy_calculator.md` | Occupancy Calculator | Calculate occupancy rate, vacancy, and lost revenue by scope | Portfolio Manager, Client Portal |

---

## How to Use These Files

### Option 1 — Single Agent, No Skills
Copy the content inside the ` ``` ` code block from an agent file and paste it as the system prompt for your Claude instance. The agent will operate within its defined scope.

### Option 2 — Agent + Skills (Recommended)
1. Start with the agent system prompt
2. Append the system prompts from any skills the agent uses (see "Suggested Skills to Attach" in each agent file)
3. Paste the combined prompt as a single system prompt

### Option 3 — Orchestrator Pattern
Use a top-level orchestrator Claude instance with a brief routing prompt, then delegate to specialist agents:

```
You are the BuildAgent Orchestrator. You receive requests from building agents and route them to the correct specialist agent based on the topic:

- Buildings, units, clients → Portfolio Manager Agent
- Tenants, leases, move-in/out → Tenancy Manager Agent
- Payments, rent, financials → Payment Ledger Agent
- Maintenance, repairs, contractors → Maintenance Hub Agent
- Owner/client queries → Client Portal Agent

Identify the topic, state which agent you are routing to, then respond using that agent's instructions.
```

---

## File Naming Convention

```
agent_[domain].md       — Full agent system prompt
skill_[function].md     — Reusable skill invoked by one or more agents
```

---

## Planned Additions (Phase 2)

| File | Description |
|---|---|
| `agent_contractor_portal.md` | Contractor-facing agent for job orders and invoice submission |
| `skill_ai_lease_scorer.md` | AI-assisted lease renewal risk scoring |
| `skill_rent_reminder_drafter.md` | Automated rent reminder generation on schedule |
| `skill_inspection_report_builder.md` | Structured property inspection report generator |
| `skill_document_classifier.md` | Classify and tag uploaded documents by type |
