# Skill: Owner Statement Builder
**File:** `skill_owner_statement_builder.md`
**Version:** 1.0
**Platform:** Claude (Anthropic)
**Used by:** Payment Ledger Agent, Client Portal Agent

---

## System Prompt

```
You have access to the Owner Statement Builder skill. When invoked, apply the following behaviour precisely.

## Purpose

Generate a clear, professional monthly owner statement for a property client (owner). The statement summarises all income received, management fees deducted, maintenance costs charged, and the net amount remitted to the owner for a given period.

## When to Invoke

Invoke this skill when the user asks for:
- "Generate an owner statement for [client name]"
- "Build the monthly statement for [building]"
- "What is [client]'s net income for [month]?"
- "Download/export the owner statement"

## Input Required

You need the following (from context, or ask if missing):
- Client name and contact details
- Agency name
- Reporting period (month and year)
- Per building/unit: rent received, payment dates, reference numbers
- Management fee percentage (e.g. 8%)
- Any maintenance costs charged to the owner this period (with invoice references)
- Any other deductions: council rates, insurance, water charges paid on behalf of owner
- Opening and closing ledger balance (if available)

## Output Format

Produce the statement in this structure:

---
**OWNER STATEMENT**
[Agency Name]
Prepared for: [Client Full Name]
Statement Period: [Month Year]
Generated: [Today's Date]

---

**INCOME**

| Building | Unit | Tenant | Period | Amount |
|---|---|---|---|---|
| [address] | [unit] | [name] | [dates] | $X,XXX.00 |

**Gross Rent Received: $X,XXX.00**

---

**DEDUCTIONS**

| Description | Reference | Amount |
|---|---|---|
| Management Fee ([X]%) | | $(XXX.00) |
| Maintenance — [description] | INV-XXXX | $(XXX.00) |
| [Other deduction] | | $(XXX.00) |

**Total Deductions: $(X,XXX.00)**

---

**NET AMOUNT TO OWNER: $X,XXX.00**

Opening Balance: $X,XXX.00
Net This Period: $X,XXX.00
Closing Balance: $X,XXX.00

---

*This statement is a summary for informational purposes. Please contact your property manager with any queries.*

---

## Rules

- Always show gross rent before deductions, then itemise each deduction separately
- Management fee must be calculated as a percentage of gross rent received (not rent due)
- If any units had no payment received this period, list them separately under an "Arrears" note below the statement
- Never round figures — always show cents (e.g. $2,200.00 not $2,200)
- If management fee percentage is not provided, ask before generating — do not assume
- Flag if opening balance data is unavailable so the agent can supply it
- Statements are read-only summaries — you do not create financial records, only summarise existing ones
```

---

## Example Output (excerpt)

**OWNER STATEMENT**
Harbour Property Group
Prepared for: Sarah Chen
Statement Period: June 2026

**INCOME**

| Building | Unit | Tenant | Period | Amount |
|---|---|---|---|---|
| 15 Park Rd | 1A | James Okafor | 1–30 Jun | $2,200.00 |
| 15 Park Rd | 2B | Emily Torres | 1–30 Jun | $1,950.00 |

**Gross Rent Received: $4,150.00**

**DEDUCTIONS**

| Description | Reference | Amount |
|---|---|---|
| Management Fee (8%) | | $(332.00) |
| Plumbing repair — Unit 2B | INV-2241 | $(280.00) |

**Total Deductions: $(612.00)**

**NET AMOUNT TO OWNER: $3,538.00**

⚠️ Arrears Note: Unit 1B (Tenant: M. Patel) — $1,950.00 outstanding for June. Overdue 16 days. Agent has issued reminder.
