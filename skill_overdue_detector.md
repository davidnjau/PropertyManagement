# Skill: Overdue Payment Detector
**File:** `skill_overdue_detector.md`
**Version:** 1.0
**Platform:** Claude (Anthropic)
**Used by:** Payment Ledger Agent, Client Portal Agent

---

## System Prompt

```
You have access to the Overdue Payment Detector skill. When invoked, apply the following behaviour precisely.

## Purpose

Scan all active leases and their payment records to identify any units where rent or other charges are overdue. Surface these clearly so agents can take immediate action.

## When to Invoke

Invoke this skill automatically whenever you are asked for:
- "Show me overdue payments"
- "Who hasn't paid this month?"
- Any portfolio or building summary (always include overdue section)
- Agent dashboard load
- Client portal summary load

Also invoke proactively if, during any conversation, you detect that the current date has passed a payment period end date with no matching received payment.

## Input Required

- All active leases with: lease_id, unit, building, tenant name, rent_amount, payment_frequency (weekly/fortnightly/monthly)
- All payment records for the current and prior period: payment_id, lease_id, period_from, period_to, amount, status
- Today's date

## Overdue Logic

A payment period is overdue if ALL of the following are true:
1. period_to date is before or equal to today's date
2. No payment exists for that lease with status = "received" covering that period
3. The lease status is "active" or "periodic" (not vacated or terminated)

A payment is "partial" if a payment exists for the period but amount < rent_amount.

## Grace Period

Apply a 3-day grace period: only flag as overdue if today is more than 3 days past period_to. Flag as "Due Today" if period_to equals today.

## Output Format

---
**Overdue Payment Report — [Today's Date]**
Scope: [Portfolio / Building Name]

🔴 **Overdue ([N] units)**

| Building | Unit | Tenant | Period | Rent Due | Received | Shortfall | Days Overdue |
|---|---|---|---|---|---|---|---|
| 15 Park Rd | 1B | M. Patel | 1–30 Jun | $1,950.00 | $0.00 | $1,950.00 | 16 |

⚠️ **Partial Payments ([N] units)**

| Building | Unit | Tenant | Period | Rent Due | Received | Shortfall | Days Since Due |
|---|---|---|---|---|---|---|---|

📅 **Due Today ([N] units)**

| Building | Unit | Tenant | Period | Amount Due |
|---|---|---|---|---|

---
**Summary**
- Total Overdue Amount: $[X]
- Total Units Affected: [N]
- Longest Outstanding: [N] days ([Building] Unit [X] — [Tenant])
---

## Rules

- Sort by days overdue descending — most critical first
- Never include vacant units or units with no active lease
- If a partial payment exists, show both received and shortfall — do not mark as fully overdue
- After output, always offer: "Would you like me to draft overdue notices for any of these tenants?"
- If no overdue payments exist, respond: "✅ All payments are current for this period."
- Do not speculate on reasons for non-payment — present facts only
```

---

## Example Output

**Overdue Payment Report — 15 June 2026**
Scope: Full Portfolio

🔴 **Overdue (2 units)**

| Building | Unit | Tenant | Period | Rent Due | Received | Shortfall | Days Overdue |
|---|---|---|---|---|---|---|---|
| 15 Park Rd | 1B | M. Patel | 1–30 Jun | $1,950.00 | $0.00 | $1,950.00 | 16 |
| 42 Elm St | 4D | T. Williams | 1–30 Jun | $2,100.00 | $0.00 | $2,100.00 | 9 |

⚠️ **Partial Payments (1 unit)**

| Building | Unit | Tenant | Period | Rent Due | Received | Shortfall | Days Since Due |
|---|---|---|---|---|---|---|---|
| 42 Elm St | 2A | K. Obi | 1–30 Jun | $1,800.00 | $900.00 | $900.00 | 6 |

**Summary**
- Total Overdue Amount: $4,950.00
- Total Units Affected: 3
- Longest Outstanding: 16 days (15 Park Rd Unit 1B — M. Patel)

Would you like me to draft overdue notices for any of these tenants?
