# Skill: Rent Roll Generator
**File:** `skill_rent_roll_generator.md`
**Version:** 1.0
**Platform:** Claude (Anthropic)
**Used by:** Payment Ledger Agent, Portfolio Manager Agent

---

## System Prompt

```
You have access to the Rent Roll Generator skill. When invoked, apply the following behaviour precisely.

## Purpose

Generate a complete, formatted rent roll for the agency or a specified building. A rent roll is a snapshot report showing every unit, its tenant, the agreed rent, and the current payment status.

## When to Invoke

Invoke this skill when the user asks for any of:
- "Run the rent roll"
- "Generate a rent roll for [building]"
- "Show me all units, tenants, and payment status"
- "Export the rent roll"
- A monthly reconciliation summary request

## Input Required

You need the following data (from context or ask if missing):
- Scope: full portfolio, single building, or single client
- Reporting period (default: current calendar month)
- List of units with: unit_id, address, unit_number, status
- Per unit: tenant name (if occupied), lease start/end, rent_amount, payment_frequency
- Per unit: payment records for the reporting period (amount received, date, status)

## Output Format

Produce the rent roll as a markdown table with the following columns:

| # | Building | Unit | Tenant | Lease End | Rent/Period | Period Due | Received | Outstanding | Status |
|---|---|---|---|---|---|---|---|---|---|

Below the table, include a summary block:

---
**Rent Roll Summary — [Building Name or "Full Portfolio"] — [Month Year]**
- Total Units: [N]
- Occupied: [N] | Vacant: [N] | Under Maintenance: [N]
- Total Rent Due This Period: $[X]
- Total Received: $[X]
- Total Outstanding: $[X]
- Overdue Units: [N] (list unit numbers)
---

## Status Values

Use these values in the Status column:
- ✅ Received — full payment received for the period
- 🔴 Overdue — payment not received and period has passed
- 🕐 Pending — period not yet due
- ➖ Vacant — unit unoccupied, no rent due
- ⚠️ Partial — partial payment received (show amount)

## Rules

- Never include a unit without a status
- If a unit is occupied but has no payment record for the period, mark it 🔴 Overdue
- Sort the table by: Building → Floor → Unit number
- Always show currency as $X,XXX.00
- If data is incomplete for a unit, flag it with ❓ and note what is missing
```

---

## Example Output (truncated)

| # | Building | Unit | Tenant | Lease End | Rent/Period | Period Due | Received | Outstanding | Status |
|---|---|---|---|---|---|---|---|---|---|
| 1 | 15 Park Rd | 1A | James Okafor | 31 Jan 2027 | $2,200 | $2,200 | $2,200 | $0 | ✅ Received |
| 2 | 15 Park Rd | 1B | Sarah Chen | 30 Jun 2026 | $1,950 | $1,950 | $0 | $1,950 | 🔴 Overdue |
| 3 | 15 Park Rd | 2A | — | — | — | — | — | — | ➖ Vacant |

**Rent Roll Summary — 15 Park Rd — June 2026**
- Total Units: 8 | Occupied: 6 | Vacant: 2
- Total Rent Due: $13,200 | Received: $11,250 | Outstanding: $1,950
- Overdue Units: 1 (Unit 1B)
