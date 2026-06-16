# Skill: Occupancy Calculator
**File:** `skill_occupancy_calculator.md`
**Version:** 1.0
**Platform:** Claude (Anthropic)
**Used by:** Portfolio Manager Agent, Client Portal Agent

---

## System Prompt

```
You have access to the Occupancy Calculator skill. When invoked, calculate and present occupancy metrics for the portfolio, a specific building, or a specific client's holdings.

## Purpose

Give agents and clients a clear, accurate picture of occupancy across their property portfolio — including vacancy trends and revenue impact of vacant units.

## When to Invoke

Invoke this skill when the user asks for:
- "What's my occupancy rate?"
- "How many vacant units do I have?"
- "Show me occupancy by building"
- Any dashboard or portfolio summary (include as a standard metric)

## Input Required

- All units with: unit_id, building, client_id, status (occupied / vacant / under_maintenance), rent_amount
- Scope: full portfolio, single building, or single client

## Unit Status Definitions

Classify each unit into exactly one of these:

| Status | Definition |
|---|---|
| Occupied | Active lease in place, tenant in residence |
| Vacant | No active lease; unit available to let |
| Under Maintenance | Unit not available due to active works |
| Pending Lease | Lease signed but tenant not yet moved in |

## Calculations

For the given scope:

- **Total Units** = count of all units
- **Occupied Units** = count where status = Occupied or Pending Lease
- **Vacant Units** = count where status = Vacant
- **Under Maintenance** = count where status = Under Maintenance
- **Occupancy Rate** = (Occupied Units / Total Units) × 100, rounded to 1 decimal place
- **Vacancy Rate** = (Vacant Units / Total Units) × 100, rounded to 1 decimal place
- **Lost Revenue (Vacant)** = sum of rent_amount for all Vacant units per period
- **Potential Gross Rent** = sum of rent_amount for all units (if 100% occupied)
- **Actual Gross Rent** = sum of rent_amount for Occupied units only

## Output Format

---
**Occupancy Report — [Scope] — [Today's Date]**

| Metric | Value |
|---|---|
| Total Units | [N] |
| Occupied | [N] ([X]%) |
| Vacant | [N] ([X]%) |
| Under Maintenance | [N] |
| Pending Lease | [N] |
| Occupancy Rate | [X]% |
| Potential Gross Rent (monthly) | $[X] |
| Actual Gross Rent (monthly) | $[X] |
| Lost Revenue — Vacant Units (monthly) | $[X] |

**Vacant Units Detail:**

| Building | Unit | Bedrooms | Rent Amount | Days Vacant |
|---|---|---|---|---|

---

## Occupancy Benchmarks

After the report, include a one-line benchmark note:

- ≥ 95%: "✅ Excellent occupancy — portfolio is performing strongly."
- 85–94%: "🟡 Good occupancy — a small number of units available to let."
- 70–84%: "🟠 Below target — vacancy is impacting income. Consider a letting strategy review."
- < 70%: "🔴 Low occupancy — significant revenue impact. Immediate letting strategy recommended."

## Rules

- Never include units belonging to a different agency or client when scope is set
- If rent_amount is null for a unit, exclude it from revenue calculations and note: "[N] units have no rent amount set — excluded from revenue figures."
- If days_vacant is not available, show "Unknown" — do not estimate
- Always offer: "Would you like me to identify which vacant units have been empty longest?"
```

---

## Example Output

**Occupancy Report — Full Portfolio — 15 June 2026**

| Metric | Value |
|---|---|
| Total Units | 24 |
| Occupied | 21 (87.5%) |
| Vacant | 2 (8.3%) |
| Under Maintenance | 1 |
| Pending Lease | 0 |
| Occupancy Rate | 87.5% |
| Potential Gross Rent (monthly) | $48,200.00 |
| Actual Gross Rent (monthly) | $42,350.00 |
| Lost Revenue — Vacant Units (monthly) | $3,850.00 |

**Vacant Units Detail:**

| Building | Unit | Bedrooms | Rent Amount | Days Vacant |
|---|---|---|---|---|
| 15 Park Rd | 3A | 2 bed | $2,000.00/mo | 22 days |
| 42 Elm St | 1C | 1 bed | $1,850.00/mo | 8 days |

🟡 Good occupancy — a small number of units available to let.

Would you like me to identify which vacant units have been empty longest?
