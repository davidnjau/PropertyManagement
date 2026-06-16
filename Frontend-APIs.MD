# Frontend API Requirements

This document describes every API endpoint the BuildAgent frontend requires, derived directly from the current frontend implementation. All forms and data tables are currently using local state only — this document defines the contracts needed to wire them to a real backend.

Base path: `/api/v1`
All requests require: `Authorization: Bearer <JWT>`
JWT claims must include: `agency_id`, `user_id`, `role` (`admin | agent | tenant`)

---

## Table of Contents

1. [Authentication](#1-authentication)
2. [Dashboard — Stats](#2-dashboard--stats)
3. [Buildings](#3-buildings)
4. [Tenants](#4-tenants)
5. [Payments](#5-payments)
6. [Payment Methods Config (Admin)](#6-payment-methods-config-admin)
7. [Maintenance Requests](#7-maintenance-requests)
8. [Documents — Admin Upload](#8-documents--admin-upload)
9. [Alerts (Admin)](#9-alerts-admin)
10. [Tenant — Overview](#10-tenant--overview)
11. [Tenant — Lease](#11-tenant--lease)
12. [Tenant — Pay Rent](#12-tenant--pay-rent)
13. [Tenant — Maintenance Requests](#13-tenant--maintenance-requests)
14. [Tenant — Documents](#14-tenant--documents)
15. [Contact / Demo Request](#15-contact--demo-request)

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

**Response `200`:**
```json
[
  {
    "id": "string",
    "name": "string",
    "address": "string",
    "city": "string",
    "units": 0
  }
]
```

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

**Response `201`:**
```json
{
  "id": "string",
  "name": "string",
  "address": "string",
  "city": "string",
  "units": 0
}
```

---

## 4. Tenants

**Page:** `src/pages/dashboard/Tenants.tsx`

### GET `/tenants`

Returns all tenants for the authenticated agency. Also consumed by the Payments modal, Alerts compose panel, and admin Documents page.

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
    "leaseStatus": "Active | Expiring soon | Expired"
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

**Response `201`:** Returns the created tenant object (same shape as list item above).

> **Note:** The backend should derive `leaseStatus` on read from the current date vs `leaseEnd`: `Active` (>30 days remaining), `Expiring soon` (≤30 days), `Expired` (past end date).

---

## 5. Payments

**Page:** `src/pages/dashboard/Payments.tsx`

### GET `/payments`

Returns the payment ledger for the authenticated agency.

**Response `200`:**
```json
[
  {
    "id": "string",
    "tenantId": "string",
    "tenantName": "string",
    "amount": 0,
    "method": "mpesa | paypal | bank_transfer",
    "bank": "string | null",
    "reference": "string",
    "dueDate": "YYYY-MM-DD",
    "status": "Paid | Pending | Overdue",
    "createdAt": "ISO 8601"
  }
]
```

---

### POST `/payments`

Logs a new payment. The `reference` field is derived from the payment method:
- M-Pesa Initiate: phone number
- M-Pesa Reference: confirmation code
- PayPal: tenant PayPal email
- Bank Transfer: bank name

**Request body:**
```json
{
  "tenantId": "string",
  "amount": 0,
  "method": "mpesa | paypal | bank_transfer",
  "mpesaAction": "initiate | reference | details | null",
  "phone": "string | null",
  "confirmationCode": "string | null",
  "paypalEmail": "string | null",
  "bankId": "string | null",
  "dueDate": "YYYY-MM-DD",
  "status": "Paid | Pending | Overdue"
}
```

**Response `201`:** Returns the created payment object (same shape as GET list item).

---

## 6. Payment Methods Config (Admin)

**Pages:** `src/pages/dashboard/admin/PaymentMethods.tsx`, consumed via `src/context/PaymentMethodsContext.tsx`

The frontend currently manages payment method config in React context (in-memory only). These endpoints are needed to persist config across sessions and share it with the tenant portal.

### GET `/admin/payment-methods`

Returns the current payment method configuration for the agency.

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

---

### PUT `/admin/payment-methods/:methodId/toggle`

Enable or disable a payment method (`mpesa`, `paypal`, `bank_transfer`).

**Request body:**
```json
{ "enabled": true }
```

**Response `200`:** Returns updated method object `{ "id": "string", "enabled": true }`.

---

### PUT `/admin/payment-methods/mpesa/config`

Update M-Pesa payment details shown to tenants.

**Request body:**
```json
{
  "businessNo": "string",
  "accountNo": "string",
  "instructions": "string"
}
```

**Response `200`:** Returns the updated config object.

---

### PUT `/admin/payment-methods/paypal/config`

Update PayPal payment details shown to tenants.

**Request body:**
```json
{
  "email": "string",
  "instructions": "string"
}
```

**Response `200`:** Returns the updated config object.

---

### PUT `/admin/payment-methods/banks/:bankId/toggle`

Enable or disable an individual bank under Bank Transfer.

**Request body:**
```json
{ "enabled": true }
```

**Response `200`:** Returns updated bank object `{ "id": "string", "name": "string", "enabled": true }`.

---

## 7. Maintenance Requests

**Page:** `src/pages/dashboard/Maintenance.tsx`

### GET `/maintenance-requests`

Returns all maintenance requests for the authenticated agency.

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
    "status": "Open | In Progress | Resolved",
    "createdAt": "ISO 8601"
  }
]
```

---

### POST `/maintenance-requests`

Creates a new maintenance request (agent-initiated).

**Request body:**
```json
{
  "title": "string",
  "description": "string",
  "priority": "Low | Medium | High",
  "buildingId": "string"
}
```

**Response `201`:** Returns created request object (same shape as GET list item).

---

## 8. Documents — Admin Upload

**Page:** `src/pages/dashboard/admin/Documents.tsx`

### GET `/admin/documents`

Returns all documents uploaded by the admin, optionally filtered by tab.

**Query params:** `?type=tenant|building`

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
    "notes": "string | null",
    "uploadedAt": "ISO 8601",
    "fileUrl": "string"
  }
]
```

**Tenant `docType` values:** `National ID`, `Passport`, `Driving License`, `KRA PIN Certificate`, `Lease Agreement`, `Employment Letter`, `Bank Statement`, `Move-in Inspection Report`, `Reference Letter`, `Utility Bill`, `Other`

**Building `docType` values:** `Title Deed`, `Building Plan / Blueprint`, `Occupation Certificate`, `Insurance Certificate`, `Compliance Certificate`, `Valuation Report`, `Land Rates Receipt`, `Utility Agreement`, `Other`

---

### POST `/admin/documents`

Uploads a document for a tenant or building. Send as `multipart/form-data`.

**Form fields:**
| Field | Type | Notes |
|---|---|---|
| `file` | File | The document file |
| `targetType` | string | `"tenant"` or `"building"` |
| `targetId` | string | ID of the tenant or building |
| `docType` | string | One of the valid doc type values above |
| `notes` | string | Optional |

**Response `201`:** Returns the created document object (same shape as GET list item).

---

## 9. Alerts (Admin)

**Page:** `src/pages/dashboard/admin/Alerts.tsx`

### GET `/admin/alerts`

Returns the history of sent alerts for the agency.

**Response `200`:**
```json
[
  {
    "id": "string",
    "sentAt": "ISO 8601",
    "targetType": "all | building | tenant | rent_due",
    "targetLabel": "string",
    "channels": ["in_app", "email", "sms"],
    "subject": "string",
    "message": "string",
    "recipientCount": 0,
    "status": "Sent | Failed"
  }
]
```

---

### POST `/admin/alerts`

Sends an alert notification. The backend is responsible for resolving recipients from the target criteria and dispatching via the chosen channels.

**Request body:**
```json
{
  "targetType": "all | building | tenant | rent_due",
  "buildingId": "string | null",
  "tenantIds": ["string"] ,
  "rentDueFilter": "upcoming | overdue | both | null",
  "channels": ["in_app", "email", "sms"],
  "subject": "string",
  "message": "string"
}
```

**Response `201`:** Returns the created alert history record (same shape as GET list item).

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

**Request body:**
```json
{
  "leaseId": "string",
  "durationMonths": 3,
  "customEndDate": "YYYY-MM-DD | null",
  "notes": "string | null"
}
```

**Response `201`:**
```json
{
  "id": "string",
  "status": "Pending",
  "proposedEndDate": "YYYY-MM-DD",
  "submittedAt": "ISO 8601"
}
```

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
| `/buildings` | Yes | Yes | No |
| `/tenants` | Yes | Yes | No |
| `/payments` | Yes | Yes | No |
| `/maintenance-requests` | Yes | Yes | No |
| `/admin/*` | Yes | No | No |
| `/tenant/*` | No | No | Yes |
| `/contact/inquiries` | Public | Public | Public |

All non-public endpoints must validate the `agency_id` from the JWT and apply PostgreSQL RLS to scope results to the correct agency.
