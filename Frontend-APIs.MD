# Frontend API Requirements

This document describes every API endpoint the BuildAgent frontend requires, derived directly from the current frontend implementation. All forms and data tables are currently using local state only — this document defines the contracts needed to wire them to a real backend.

Base path: `/api/v1`
All requests require: `Authorization: Bearer <JWT>`
JWT claims must include: `agency_id`, `user_id`, `role` (`admin | agent | tenant`)

---

## Table of Contents

1. [Authentication](#1-authentication)
2. [Dashboard — Stats](#2-dashboard--stats)
3. [Buildings](#3-buildings) (create + admin management)
4. [Tenants](#4-tenants) (create + admin management)
5. [Payments](#5-payments) (log + admin management)
6. [Payment Methods Config (Admin)](#6-payment-methods-config-admin) (config + management)
7. [Maintenance Requests](#7-maintenance-requests) (create + admin management)
8. [Documents — Admin Upload](#8-documents--admin-upload) (upload + management)
9. [Alerts (Admin)](#9-alerts-admin) (send + management)
10. [Tenant — Overview](#10-tenant--overview)
11. [Tenant — Lease](#11-tenant--lease)
12. [Tenant — Pay Rent](#12-tenant--pay-rent)
13. [Tenant — Maintenance Requests](#13-tenant--maintenance-requests)
14. [Tenant — Documents](#14-tenant--documents)
15. [Contact / Demo Request](#15-contact--demo-request)
16. [Lease Extension Requests (Admin)](#16-lease-extension-requests-admin)

---

## 1. Authentication

**Page:** `src/pages/Auth.tsx`

### POST `/auth/signin`

Sign in an existing user.

**Request body:**
```json
{
  "email": "string",
  "password": "string",
  "role": "agent | admin | tenant"
}
```

**Response `200`:**
```json
{
  "token": "string (JWT)",
  "user": {
    "id": "string",
    "email": "string",
    "name": "string",
    "role": "agent | admin | tenant"
  }
}
```

**Frontend behaviour:** On success, stores the JWT and navigates to `/dashboard` (agent/admin) or `/tenant` (tenant).

---

### POST `/auth/signup`

Register a new user.

**Request body:**
```json
{
  "name": "string",
  "email": "string",
  "password": "string",
  "role": "agent | admin | tenant"
}
```

**Response `201`:**
```json
{
  "token": "string (JWT)",
  "user": {
    "id": "string",
    "email": "string",
    "name": "string",
    "role": "agent | admin | tenant"
  }
}
```

---

## 2. Dashboard — Stats

**Page:** `src/pages/dashboard/Overview.tsx`

### GET `/dashboard/stats`

Returns aggregated counts shown in the four stat cards on the Overview page.

**Response `200`:**
```json
{
  "buildings": 0,
  "tenants": 0,
  "payments": 0,
  "openMaintenance": 0
}
```

All values are scoped to the `agency_id` in the JWT.

---

## 3. Buildings

**Page:** `src/pages/dashboard/Buildings.tsx`

### GET `/buildings`

Returns all buildings for the authenticated agency. Also consumed by the Tenants page, Maintenance page, Alerts page, and admin Documents page to populate dropdown selectors.

**Query params:** `?city=string`

**Response `200`:**
```json
[
  {
    "id": "string",
    "name": "string",
    "address": "string",
    "city": "string",
    "units": 0,
    "occupiedUnits": 0,
    "vacantUnits": 0,
    "createdAt": "ISO 8601"
  }
]
```

> `occupiedUnits` and `vacantUnits` are derived server-side from active leases. `units` is the total capacity set at creation.

---

### POST `/buildings`

Creates a new building.

**Request body:**
```json
{
  "name": "string",
  "address": "string",
  "city": "string",
  "units": 0
}
```

**Response `201`:** Returns the created building object (same shape as GET list item).

---

### GET `/buildings/:id`

Returns a single building by ID, including a summary of its units and current tenants.

**Response `200`:**
```json
{
  "id": "string",
  "name": "string",
  "address": "string",
  "city": "string",
  "units": 0,
  "occupiedUnits": 0,
  "vacantUnits": 0,
  "createdAt": "ISO 8601",
  "tenants": [
    {
      "id": "string",
      "fullName": "string",
      "unit": "string",
      "leaseEnd": "YYYY-MM-DD",
      "leaseStatus": "Active | Expiring soon | Expired"
    }
  ]
}
```

---

### PATCH `/buildings/:id`

Updates building details. Used by agents/admins to correct or update a building's information.

**Request body (all fields optional — send only what is changing):**
```json
{
  "name": "string",
  "address": "string",
  "city": "string",
  "units": 0
}
```

| Field | Type | Notes |
|---|---|---|
| `name` | string | Building display name |
| `address` | string | Street address |
| `city` | string | City |
| `units` | number | Total unit capacity — must be ≥ current `occupiedUnits` |

**Response `200`:** Returns the full updated building object.

> **Constraint:** `units` cannot be reduced below the current number of occupied units. The backend should return a `422` with a descriptive error if this is violated.

---

### DELETE `/buildings/:id`

Deletes a building record. Only permitted when the building has no active tenants or open maintenance requests.

**Response `200`:**
```json
{ "id": "string", "deleted": true }
```

> The backend should return `409 Conflict` if the building still has active leases or unresolved maintenance requests, with a message indicating which constraint is blocking deletion.

---

## 4. Tenants

**Page:** `src/pages/dashboard/Tenants.tsx`

### GET `/tenants`

Returns all tenants for the authenticated agency. Also consumed by the Payments modal, Alerts compose panel, and admin Documents page.

**Query params:** `?buildingId=string` `&leaseStatus=Active|Expiring soon|Expired` `&search=string` (name/email prefix match)

**Response `200`:**
```json
[
  {
    "id": "string",
    "fullName": "string",
    "email": "string",
    "phone": "string",
    "unit": "string",
    "buildingId": "string",
    "buildingName": "string",
    "monthlyRent": 0,
    "deposit": 0,
    "leaseStart": "YYYY-MM-DD",
    "leaseEnd": "YYYY-MM-DD",
    "leaseStatus": "Active | Expiring soon | Expired",
    "createdAt": "ISO 8601"
  }
]
```

---

### POST `/tenants`

Creates a new tenant record together with their initial lease. `leaseStart`, `leaseEnd`, and `monthlyRent` are required; `deposit` is optional.

**Request body:**
```json
{
  "fullName": "string",
  "email": "string",
  "phone": "string",
  "unit": "string",
  "buildingId": "string",
  "monthlyRent": 0,
  "deposit": 0,
  "leaseStart": "YYYY-MM-DD",
  "leaseEnd": "YYYY-MM-DD"
}
```

**Response `201`:** Returns the created tenant object (same shape as GET list item).

> **Note:** The backend should derive `leaseStatus` on read from the current date vs `leaseEnd`: `Active` (>30 days remaining), `Expiring soon` (≤30 days), `Expired` (past end date).

---

### GET `/tenants/:id`

Returns a single tenant by ID with their full lease and payment history summary.

**Response `200`:**
```json
{
  "id": "string",
  "fullName": "string",
  "email": "string",
  "phone": "string",
  "unit": "string",
  "buildingId": "string",
  "buildingName": "string",
  "monthlyRent": 0,
  "deposit": 0,
  "leaseStart": "YYYY-MM-DD",
  "leaseEnd": "YYYY-MM-DD",
  "leaseStatus": "Active | Expiring soon | Expired",
  "createdAt": "ISO 8601",
  "paymentSummary": {
    "totalPaid": 0,
    "lastPaymentDate": "ISO 8601 | null",
    "overdueCount": 0
  },
  "openMaintenanceCount": 0
}
```

---

### PATCH `/tenants/:id`

Updates a tenant's contact details or lease terms. Used by agents/admins to correct information or adjust rent.

**Request body (all fields optional — send only what is changing):**
```json
{
  "fullName": "string",
  "email": "string",
  "phone": "string",
  "unit": "string",
  "buildingId": "string",
  "monthlyRent": 0,
  "deposit": 0,
  "leaseStart": "YYYY-MM-DD",
  "leaseEnd": "YYYY-MM-DD"
}
```

| Field | Type | Notes |
|---|---|---|
| `fullName` | string | Correct a name spelling |
| `email` | string | Must remain unique within the agency |
| `phone` | string | Contact number update |
| `unit` | string | Unit reassignment within the same building |
| `buildingId` | string | Transfer to a different building |
| `monthlyRent` | number | Rent adjustment — takes effect from next billing cycle |
| `deposit` | number | Deposit correction |
| `leaseStart` | string | Correction to lease start date |
| `leaseEnd` | string | Direct lease end date update (distinct from an extension request) |

**Response `200`:** Returns the full updated tenant object.

---

### DELETE `/tenants/:id`

Removes a tenant record. Only permitted when the tenant has no active lease and no outstanding payments.

**Response `200`:**
```json
{ "id": "string", "deleted": true }
```

> The backend should return `409 Conflict` if the tenant has an active lease or unresolved overdue payments, with a message indicating the blocking constraint. For move-outs, consider a soft-delete (`archivedAt`) to preserve payment and maintenance history.

---

## 5. Payments

**Pages:** `src/pages/dashboard/Payments.tsx`, `src/pages/tenant/PayRent.tsx`

### GET `/payments`

Returns the full payment ledger for the authenticated agency. Supports filtering for the ledger table.

**Query params:** `?status=Paid|Pending|Overdue` `&tenantId=string` `&method=mpesa|paypal|bank_transfer` `&from=YYYY-MM-DD` `&to=YYYY-MM-DD`

**Response `200`:**
```json
[
  {
    "id": "string",
    "tenantId": "string",
    "tenantName": "string",
    "amount": 0,
    "method": "mpesa | paypal | bank_transfer",
    "mpesaAction": "initiate | reference | details | null",
    "bank": "string | null",
    "reference": "string",
    "dueDate": "YYYY-MM-DD",
    "paidAt": "ISO 8601 | null",
    "status": "Paid | Pending | Overdue",
    "loggedBy": "string",
    "createdAt": "ISO 8601"
  }
]
```

> `loggedBy` is the `user_id` of the agent who recorded the payment. `paidAt` is set server-side when `status` is `"Paid"`.

---

### POST `/payments`

Logs a new payment against a tenant. The `reference` stored on the record is derived from the payment method action:
- **M-Pesa Initiate** → tenant phone number (STK push sent)
- **M-Pesa Reference** → confirmation code entered by the agent
- **M-Pesa Details** → no reference captured (informational view only)
- **PayPal** → tenant's PayPal email
- **Bank Transfer** → bank name

**Request body:**
```json
{
  "tenantId": "string",
  "amount": 0,
  "dueDate": "YYYY-MM-DD",
  "status": "Paid | Pending | Overdue",
  "method": "mpesa | paypal | bank_transfer",
  "mpesaAction": "initiate | reference | details | null",
  "phone": "string | null",
  "confirmationCode": "string | null",
  "paypalEmail": "string | null",
  "bankId": "string | null"
}
```

**Response `201`:** Returns the created payment object (same shape as GET list item).

---

### GET `/payments/:id`

Returns a single payment record by ID.

**Response `200`:** Same shape as a single item in the list response above.

---

### PATCH `/payments/:id`

Updates a payment record. Used by agents/admins to correct status, amount, or reference after the fact.

**Request body (all fields optional — send only what is changing):**
```json
{
  "status": "Paid | Pending | Overdue",
  "amount": 0,
  "dueDate": "YYYY-MM-DD",
  "reference": "string",
  "agentNotes": "string | null"
}
```

| Field | Type | Notes |
|---|---|---|
| `status` | string | Changing to `"Paid"` sets `paidAt` server-side |
| `amount` | number | Correction to a mis-logged amount |
| `dueDate` | string | Correction to the due date |
| `reference` | string | Correction to the reference/confirmation code |
| `agentNotes` | string \| null | Internal note attached to the change for audit trail |

**Response `200`:** Returns the full updated payment object.

> **Immutability note:** Per the data model, all financial mutations must append a row to `audit_events` with `actor_id`, `action`, `entity_type: "payment"`, `entity_id`, and `diff_json`. PATCH should never silently overwrite — the original values must be preserved in the audit log.

---

### DELETE `/payments/:id`

Voids (soft-deletes) a payment record. Used to reverse an incorrectly logged entry.

**Response `200`:**
```json
{ "id": "string", "voided": true, "voidedAt": "ISO 8601", "voidedBy": "string" }
```

> Voided records should remain in the ledger with a `voided` flag rather than being hard-deleted, to preserve the audit trail.

---

## 6. Payment Methods Config (Admin)

**Pages:** `src/pages/dashboard/admin/PaymentMethods.tsx`, consumed via `src/context/PaymentMethodsContext.tsx`

The frontend currently manages payment method config in React context (in-memory only). These endpoints persist config across sessions and make it available to the tenant portal via `GET /tenant/payment-methods`.

The page has three distinct sections:
1. **Channels** — enable/disable M-Pesa, PayPal, Bank Transfer
2. **M-Pesa config** — paybill number, account number, instructions (shown when M-Pesa is enabled)
3. **PayPal config** — receiving email, instructions (shown when PayPal is enabled)
4. **Kenyan Banks list** — 18 banks each with an individual toggle (shown when Bank Transfer is enabled)

---

### GET `/admin/payment-methods`

Returns the full payment method configuration for the agency. This is the initialisation call for `PaymentMethodsContext` on app load, and is also called by `GET /tenant/payment-methods`.

**Response `200`:**
```json
{
  "methods": [
    { "id": "mpesa", "label": "M-Pesa", "enabled": true },
    { "id": "paypal", "label": "PayPal", "enabled": false },
    { "id": "bank_transfer", "label": "Bank Transfer", "enabled": true }
  ],
  "mpesaConfig": {
    "businessNo": "string",
    "accountNo": "string",
    "instructions": "string"
  },
  "paypalConfig": {
    "email": "string",
    "instructions": "string"
  },
  "banks": [
    { "id": "string", "name": "string", "enabled": true }
  ]
}
```

> The `banks` array is always returned in full (all 18 Kenyan banks), each with its current `enabled` state. The UI shows a count of enabled banks alongside the Bank Transfer toggle.

---

### PUT `/admin/payment-methods/:methodId/toggle`

Enable or disable a top-level payment method. `:methodId` is one of `mpesa`, `paypal`, `bank_transfer`.

Disabling a method hides it immediately from the tenant Pay Rent page and the agent Payments modal. Any in-flight payments already logged under that method are unaffected.

**Request body:**
```json
{ "enabled": true }
```

**Response `200`:**
```json
{ "id": "string", "label": "string", "enabled": true }
```

---

### PUT `/admin/payment-methods/mpesa/config`

Saves the M-Pesa paybill details displayed to tenants on the Pay Rent page and in the agent Payments modal (View Details action).

**Request body:**
```json
{
  "businessNo": "string",
  "accountNo": "string",
  "instructions": "string"
}
```

| Field | Type | Notes |
|---|---|---|
| `businessNo` | string | Safaricom paybill / till number |
| `accountNo` | string | Account reference shown to the tenant (e.g. unit number) |
| `instructions` | string | Optional free-text instructions rendered below the payment details card |

**Response `200`:** Returns the updated M-Pesa config object (same shape as request body).

---

### PUT `/admin/payment-methods/paypal/config`

Saves the PayPal receiving details displayed to tenants on the Pay Rent page.

**Request body:**
```json
{
  "email": "string",
  "instructions": "string"
}
```

| Field | Type | Notes |
|---|---|---|
| `email` | string | PayPal email address the agency receives payments at |
| `instructions` | string | Optional free-text instructions rendered below the payment details card |

**Response `200`:** Returns the updated PayPal config object (same shape as request body).

---

### PUT `/admin/payment-methods/banks/:bankId/toggle`

Enable or disable a single bank in the Bank Transfer list. `:bankId` corresponds to the `id` values returned in the `banks` array from `GET /admin/payment-methods`.

Only enabled banks appear in the bank dropdown on the tenant Pay Rent page and in the agent Payments modal.

**Request body:**
```json
{ "enabled": true }
```

**Response `200`:**
```json
{ "id": "string", "name": "string", "enabled": true }
```

---

### PUT `/admin/payment-methods/banks`

Bulk-updates the enabled state for multiple banks in a single request. Useful for enabling/disabling all banks at once without 18 individual calls.

**Request body:**
```json
{
  "banks": [
    { "id": "string", "enabled": true }
  ]
}
```

**Response `200`:** Returns the full updated `banks` array (same shape as in `GET /admin/payment-methods`).

---

## 7. Maintenance Requests

**Pages:** `src/pages/dashboard/Maintenance.tsx`, `src/pages/tenant/Maintenance.tsx`

### GET `/maintenance-requests`

Returns all maintenance requests for the authenticated agency. Supports optional filtering.

**Query params:** `?status=Open|In Progress|Resolved` `&priority=Low|Medium|High` `&buildingId=string`

**Response `200`:**
```json
[
  {
    "id": "string",
    "title": "string",
    "description": "string",
    "priority": "Low | Medium | High",
    "buildingId": "string",
    "buildingName": "string",
    "tenantId": "string | null",
    "tenantName": "string | null",
    "source": "agent | tenant",
    "status": "Open | In Progress | Resolved",
    "assignedTo": "string | null",
    "resolvedAt": "ISO 8601 | null",
    "createdAt": "ISO 8601"
  }
]
```

> `source` distinguishes agent-created requests from tenant-submitted ones. `tenantId`/`tenantName` are populated when `source` is `"tenant"`.

---

### POST `/maintenance-requests`

Creates a new maintenance request (agent-initiated). For tenant-submitted requests see `POST /tenant/maintenance`.

**Request body:**
```json
{
  "title": "string",
  "description": "string",
  "priority": "Low | Medium | High",
  "buildingId": "string"
}
```

**Response `201`:** Returns the created request object (same shape as GET list item, with `source: "agent"`).

---

### GET `/maintenance-requests/:id`

Returns a single maintenance request by ID.

**Response `200`:** Same shape as a single item in the list response above.

---

### PATCH `/maintenance-requests/:id`

Updates the status, priority, or assignee of a maintenance request. Used by agents/admins to progress a request through its lifecycle.

**Request body (all fields optional — send only what is changing):**
```json
{
  "status": "Open | In Progress | Resolved",
  "priority": "Low | Medium | High",
  "assignedTo": "string | null",
  "agentNotes": "string | null"
}
```

| Field | Type | Notes |
|---|---|---|
| `status` | string | Moving to `"Resolved"` sets `resolvedAt` server-side |
| `priority` | string | Agent can re-classify priority after review |
| `assignedTo` | string \| null | User ID of the staff member handling the request |
| `agentNotes` | string \| null | Internal notes or update message visible to the tenant |

**Response `200`:** Returns the full updated request object.

> **Side effect on resolve:** When `status` is set to `"Resolved"`, the backend should set `resolvedAt` to the current timestamp and decrement the `openMaintenance` count returned by `GET /dashboard/stats`.

---

## 8. Documents — Admin Upload

**Pages:** `src/pages/dashboard/admin/Documents.tsx`, `src/pages/tenant/Documents.tsx` (read)

### GET `/admin/documents`

Returns all documents uploaded by the admin. The frontend tabs by `targetType` so filtering by it is the primary use case.

**Query params:** `?targetType=tenant|building` `&targetId=string` `&docType=string`

**Response `200`:**
```json
[
  {
    "id": "string",
    "targetType": "tenant | building",
    "targetId": "string",
    "targetName": "string",
    "docType": "string",
    "fileName": "string",
    "fileSize": 0,
    "mimeType": "string",
    "notes": "string | null",
    "uploadedBy": "string",
    "uploadedAt": "ISO 8601",
    "fileUrl": "string"
  }
]
```

**Accepted file types:** PDF, DOC, DOCX, JPG, PNG — max 10 MB per file.

**Tenant `docType` values:** `National ID`, `Passport`, `Driving License`, `KRA PIN Certificate`, `Lease Agreement`, `Employment Letter`, `Bank Statement`, `Move-in Inspection Report`, `Reference Letter`, `Utility Bill`, `Other`

**Building `docType` values:** `Title Deed`, `Building Plan / Blueprint`, `Occupation Certificate`, `Insurance Certificate`, `Compliance Certificate`, `Valuation Report`, `Land Rates Receipt`, `Utility Agreement`, `Other`

---

### POST `/admin/documents`

Uploads a document for a tenant or building. Send as `multipart/form-data`.

**Form fields:**
| Field | Type | Required | Notes |
|---|---|---|---|
| `file` | File | Yes | PDF, DOC, DOCX, JPG, PNG — max 10 MB |
| `targetType` | string | Yes | `"tenant"` or `"building"` |
| `targetId` | string | Yes | ID of the tenant or building |
| `docType` | string | Yes | One of the valid doc type values above |
| `notes` | string | No | Optional free-text note (e.g. "ID expires Dec 2028") |

**Response `201`:** Returns the created document object (same shape as GET list item).

> The backend should store the file in S3 and return a signed `fileUrl` valid for a configured TTL. The `uploadedBy` field should be set from the JWT `user_id`.

---

### GET `/admin/documents/:id`

Returns a single document record by ID, including a fresh signed download URL.

**Response `200`:** Same shape as a single GET list item. The `fileUrl` should be a freshly signed URL, since stored URLs may have expired.

---

### PATCH `/admin/documents/:id`

Updates the metadata of an existing document. Used to correct a mislabelled doc type or update notes. The file itself cannot be replaced — upload a new document instead.

**Request body (all fields optional):**
```json
{
  "docType": "string",
  "notes": "string | null"
}
```

| Field | Type | Notes |
|---|---|---|
| `docType` | string | Must be a valid value from the appropriate type list |
| `notes` | string \| null | Pass `null` to clear existing notes |

**Response `200`:** Returns the full updated document object.

---

### DELETE `/admin/documents/:id`

Permanently deletes a document record and its corresponding file from storage.

**Response `200`:**
```json
{ "id": "string", "deleted": true }
```

> This is a hard delete — the file is removed from S3 and the record is gone. Warn the user in the UI before calling this endpoint if the document is a Lease Agreement or legally significant type.

---

## 9. Alerts (Admin)

**Page:** `src/pages/dashboard/admin/Alerts.tsx`

### GET `/admin/alerts`

Returns the history of sent alerts for the agency. The frontend history table displays all columns in the response.

**Query params:** `?status=Sent|Failed` `&targetType=all|building|tenant|rent_due` `&channel=in_app|email|sms` `&from=YYYY-MM-DD` `&to=YYYY-MM-DD`

**Response `200`:**
```json
[
  {
    "id": "string",
    "sentAt": "ISO 8601",
    "sentBy": "string",
    "targetType": "all | building | tenant | rent_due",
    "targetLabel": "string",
    "buildingId": "string | null",
    "tenantIds": ["string"],
    "rentDueFilter": "upcoming | overdue | both | null",
    "channels": ["in_app", "email", "sms"],
    "subject": "string",
    "message": "string",
    "recipientCount": 0,
    "status": "Sent | Failed",
    "failureReason": "string | null"
  }
]
```

> `sentBy` is the `user_id` of the admin who composed the alert. `failureReason` is populated when `status` is `"Failed"` — e.g. a downstream SMS/email provider error.

---

### POST `/admin/alerts`

Sends an alert notification. The backend resolves recipients from the target criteria, dispatches via the chosen channels, and records the result in alert history.

**Request body:**
```json
{
  "targetType": "all | building | tenant | rent_due",
  "buildingId": "string | null",
  "tenantIds": ["string"],
  "rentDueFilter": "upcoming | overdue | both | null",
  "channels": ["in_app", "email", "sms"],
  "subject": "string",
  "message": "string"
}
```

| Field | Type | Notes |
|---|---|---|
| `targetType` | string | Determines which of the conditional fields below are used |
| `buildingId` | string \| null | Required when `targetType` is `"building"` |
| `tenantIds` | string[] | Required (non-empty) when `targetType` is `"tenant"` |
| `rentDueFilter` | string \| null | Required when `targetType` is `"rent_due"`: `"upcoming"` (7 days), `"overdue"`, or `"both"` |
| `channels` | string[] | At least one of `"in_app"`, `"email"`, `"sms"` required |
| `subject` | string | Alert subject line |
| `message` | string | Alert body |

**Response `201`:** Returns the created alert history record (same shape as GET list item).

> **Dispatch behaviour:** `in_app` creates a notification record readable by the tenant portal. `email` triggers SendGrid. `sms` triggers Twilio. Partial failures (e.g. SMS fails but email succeeds) should still record `status: "Sent"` with per-channel failure detail in `failureReason`.

---

### GET `/admin/alerts/:id`

Returns a single alert record by ID, including the full recipient list resolved at send time.

**Response `200`:**
```json
{
  "id": "string",
  "sentAt": "ISO 8601",
  "sentBy": "string",
  "targetType": "all | building | tenant | rent_due",
  "targetLabel": "string",
  "buildingId": "string | null",
  "tenantIds": ["string"],
  "rentDueFilter": "upcoming | overdue | both | null",
  "channels": ["in_app", "email", "sms"],
  "subject": "string",
  "message": "string",
  "recipientCount": 0,
  "status": "Sent | Failed",
  "failureReason": "string | null",
  "recipients": [
    {
      "tenantId": "string",
      "tenantName": "string",
      "email": "string",
      "phone": "string | null",
      "channelStatuses": {
        "in_app": "Delivered | Skipped",
        "email": "Delivered | Failed | Skipped",
        "sms": "Delivered | Failed | Skipped"
      }
    }
  ]
}
```

---

### DELETE `/admin/alerts/:id`

Removes an alert record from history. Does not retract already-delivered notifications.

**Response `200`:**
```json
{ "id": "string", "deleted": true }
```

---

## 10. Tenant — Overview

**Page:** `src/pages/tenant/Overview.tsx`

### GET `/tenant/overview`

Returns the summary data for the tenant's home dashboard.

**Response `200`:**
```json
{
  "rentDue": {
    "amount": 0,
    "dueDate": "YYYY-MM-DD",
    "status": "Due | Paid | Overdue"
  },
  "lease": {
    "endDate": "YYYY-MM-DD",
    "daysRemaining": 0
  },
  "recentPayments": [
    {
      "id": "string",
      "period": "string",
      "amount": 0,
      "method": "string",
      "status": "Paid | Pending | Overdue"
    }
  ]
}
```

---

## 11. Tenant — Lease

**Page:** `src/pages/tenant/Lease.tsx`

### GET `/tenant/lease`

Returns the tenant's current active lease.

**Response `200`:**
```json
{
  "id": "string",
  "unit": "string",
  "building": "string",
  "floor": "string",
  "startDate": "YYYY-MM-DD",
  "endDate": "YYYY-MM-DD",
  "monthlyRent": 0,
  "deposit": 0,
  "status": "Active | Expired | Pending Renewal"
}
```

---

### POST `/tenant/lease/extension-request`

Submits a lease extension request from the tenant.

The form offers preset durations (3, 6, 12, 24 months) or a custom end date. Exactly one of `durationMonths` or `customEndDate` will be non-null per request.

**Request body:**
```json
{
  "leaseId": "string",
  "durationMonths": 3,
  "customEndDate": "YYYY-MM-DD | null",
  "notes": "string | null"
}
```

| Field | Type | Notes |
|---|---|---|
| `leaseId` | string | ID of the active lease being extended |
| `durationMonths` | number \| null | One of `3`, `6`, `12`, `24` — null when custom date is used |
| `customEndDate` | string \| null | ISO date — only set when the tenant picks "Custom date"; null otherwise |
| `notes` | string \| null | Optional free-text from the tenant |

**Response `201`:**
```json
{
  "id": "string",
  "leaseId": "string",
  "status": "Pending",
  "proposedEndDate": "YYYY-MM-DD",
  "submittedAt": "ISO 8601"
}
```

> **Note:** The backend should compute `proposedEndDate` from the current `leaseEnd` + `durationMonths`, or use `customEndDate` directly. The request sits in a `Pending` state until an agent approves or rejects it.

---

## 12. Tenant — Pay Rent

**Page:** `src/pages/tenant/PayRent.tsx`

### GET `/tenant/payment-methods`

Returns the payment methods enabled by the admin, including the configured display details shown to tenants (M-Pesa paybill, PayPal email, enabled banks). This is the tenant-facing read-only version of the admin payment methods config.

**Response `200`:** Same shape as `GET /admin/payment-methods` but read-only — no config editing fields needed on the frontend for this role.

---

### GET `/tenant/payments`

Returns the tenant's own payment history.

**Response `200`:**
```json
[
  {
    "id": "string",
    "period": "string",
    "amount": 0,
    "method": "mpesa | paypal | bank_transfer",
    "reference": "string",
    "date": "ISO 8601",
    "status": "Paid | Pending | Overdue"
  }
]
```

---

### POST `/tenant/payments`

Submits a rent payment record.

**Request body:**
```json
{
  "period": "string",
  "amount": 0,
  "method": "mpesa | paypal | bank_transfer",
  "mpesaAction": "initiate | reference | details | null",
  "phone": "string | null",
  "confirmationCode": "string | null",
  "paypalEmail": "string | null",
  "bankId": "string | null"
}
```

**Response `201`:** Returns created payment object (same shape as GET list item).

---

## 13. Tenant — Maintenance Requests

**Page:** `src/pages/tenant/Maintenance.tsx`

### GET `/tenant/maintenance`

Returns the maintenance requests submitted by the authenticated tenant.

**Response `200`:**
```json
[
  {
    "id": "string",
    "title": "string",
    "description": "string",
    "priority": "Low | Medium | High",
    "status": "Open | In Progress | Resolved",
    "createdAt": "ISO 8601"
  }
]
```

---

### POST `/tenant/maintenance`

Submits a new maintenance request from the tenant.

**Request body:**
```json
{
  "title": "string",
  "description": "string",
  "priority": "Low | Medium | High"
}
```

**Response `201`:** Returns the created request object (same shape as GET list item).

---

## 14. Tenant — Documents

**Page:** `src/pages/tenant/Documents.tsx`

### GET `/tenant/documents`

Returns all documents associated with the authenticated tenant (both admin-uploaded and self-uploaded).

**Response `200`:**
```json
[
  {
    "id": "string",
    "name": "string",
    "docType": "string",
    "fileSize": 0,
    "uploadedAt": "ISO 8601",
    "uploadedBy": "admin | tenant",
    "fileUrl": "string"
  }
]
```

---

### POST `/tenant/documents`

Allows the tenant to upload their own document. Send as `multipart/form-data`.

**Form fields:**
| Field | Type | Notes |
|---|---|---|
| `file` | File | The document file (PDF, DOC, DOCX, JPG, PNG) |

**Response `201`:** Returns the uploaded document object (same shape as GET list item, with `uploadedBy: "tenant"`).

---

## 15. Contact / Demo Request

**Page:** `src/pages/Contact.tsx`

### POST `/contact/inquiries`

Saves a demo request or contact inquiry from the marketing site. This is a public endpoint — no JWT required.

**Request body:**
```json
{
  "fullName": "string",
  "workEmail": "string",
  "company": "string",
  "units": 0,
  "message": "string"
}
```

**Response `201`:**
```json
{ "success": true }
```

---

## 16. Lease Extension Requests (Admin)

**Page:** `src/pages/dashboard/Tenants.tsx` (pending UI — no admin review screen exists yet)

When a tenant submits a lease extension request via `POST /tenant/lease/extension-request`, it lands in a `Pending` state. These endpoints allow an agent/admin to list and act on those requests.

### GET `/admin/lease-extension-requests`

Returns all pending (and recently resolved) lease extension requests across the agency.

**Query params:** `?status=pending|approved|rejected` (optional, defaults to all)

**Response `200`:**
```json
[
  {
    "id": "string",
    "leaseId": "string",
    "tenantId": "string",
    "tenantName": "string",
    "unit": "string",
    "building": "string",
    "currentEndDate": "YYYY-MM-DD",
    "proposedEndDate": "YYYY-MM-DD",
    "durationMonths": 12,
    "customEndDate": "YYYY-MM-DD | null",
    "notes": "string | null",
    "status": "Pending | Approved | Rejected",
    "submittedAt": "ISO 8601",
    "resolvedAt": "ISO 8601 | null",
    "resolvedBy": "string | null"
  }
]
```

---

### PATCH `/admin/lease-extension-requests/:id`

Approves or rejects a lease extension request. On approval, the backend should update the linked lease's `endDate` to `proposedEndDate`.

**Request body:**
```json
{
  "status": "Approved | Rejected",
  "agentNotes": "string | null"
}
```

| Field | Type | Notes |
|---|---|---|
| `status` | string | Must be `"Approved"` or `"Rejected"` |
| `agentNotes` | string \| null | Optional message back to the tenant |

**Response `200`:**
```json
{
  "id": "string",
  "status": "Approved | Rejected",
  "proposedEndDate": "YYYY-MM-DD",
  "resolvedAt": "ISO 8601",
  "resolvedBy": "string"
}
```

> **Side effect on approval:** The backend must update the tenant's lease record — set `leaseEnd = proposedEndDate` and `leaseStatus = "Active"`. This ensures `GET /tenant/lease` and `GET /tenants` immediately reflect the new end date.

---

## Appendix — Shared Resource Dependencies

Several pages fetch the same resources (buildings, tenants) to populate dropdowns. The backend should return consistent shapes for these so a single React Query hook can serve multiple consumers.

| Resource | Consumed by |
|---|---|
| `GET /buildings` | Buildings page, Tenants form, Maintenance form, Alerts compose, Admin Documents form |
| `GET /tenants` | Tenants page, Payments modal, Alerts compose, Admin Documents form |
| `GET /admin/payment-methods` | PaymentMethodsContext (admin), tenant Pay Rent page |

---

## Appendix — Role-Based Access

| Endpoint group | admin | agent | tenant |
|---|---|---|---|
| `/auth/*` | Yes | Yes | Yes |
| `/dashboard/stats` | Yes | Yes | No |
| `GET /buildings` | Yes | Yes | No |
| `POST /buildings` | Yes | Yes | No |
| `GET /buildings/:id` | Yes | Yes | No |
| `PATCH /buildings/:id` | Yes | Yes | No |
| `DELETE /buildings/:id` | Yes | No | No |
| `GET /tenants` | Yes | Yes | No |
| `POST /tenants` | Yes | Yes | No |
| `GET /tenants/:id` | Yes | Yes | No |
| `PATCH /tenants/:id` | Yes | Yes | No |
| `DELETE /tenants/:id` | Yes | No | No |
| `GET /payments` | Yes | Yes | No |
| `POST /payments` | Yes | Yes | No |
| `PATCH /payments/:id` | Yes | Yes | No |
| `DELETE /payments/:id` | Yes | No | No |
| `GET /maintenance-requests` | Yes | Yes | No |
| `POST /maintenance-requests` | Yes | Yes | No |
| `PATCH /maintenance-requests/:id` | Yes | Yes | No |
| `GET /tenant/maintenance` | No | No | Yes |
| `POST /tenant/maintenance` | No | No | Yes |
| `GET /admin/documents` | Yes | Yes | No |
| `POST /admin/documents` | Yes | Yes | No |
| `GET /admin/documents/:id` | Yes | Yes | No |
| `PATCH /admin/documents/:id` | Yes | Yes | No |
| `DELETE /admin/documents/:id` | Yes | No | No |
| `GET /admin/alerts` | Yes | Yes | No |
| `POST /admin/alerts` | Yes | No | No |
| `GET /admin/alerts/:id` | Yes | Yes | No |
| `DELETE /admin/alerts/:id` | Yes | No | No |
| `GET /admin/payment-methods` | Yes | Yes | No |
| `PUT /admin/payment-methods/:methodId/toggle` | Yes | No | No |
| `PUT /admin/payment-methods/mpesa/config` | Yes | No | No |
| `PUT /admin/payment-methods/paypal/config` | Yes | No | No |
| `PUT /admin/payment-methods/banks/:bankId/toggle` | Yes | No | No |
| `PUT /admin/payment-methods/banks` (bulk) | Yes | No | No |
| `/admin/lease-extension-requests/*` | Yes | Yes | No |
| `/tenant/*` | No | No | Yes |
| `/contact/inquiries` | Public | Public | Public |

All non-public endpoints must validate the `agency_id` from the JWT and apply PostgreSQL RLS to scope results to the correct agency.
