import api from './api'
import type { PaymentMethodsResponse } from './paymentMethods'

export type TenantOverview = {
  rentDue?: number
  rentDueDate?: string
  leaseEndDate?: string
  leaseStatus?: string
  recentPayments: {
    id: string
    amount: number
    status: string
    periodFrom: string
    periodTo: string
    paymentDate?: string
  }[]
}

export type TenantLease = {
  id: string
  unitId: string
  tenantId: string
  startDate: string
  endDate?: string
  rentAmount: number
  bondAmount: number
  status: string
  paymentDay: number
}

export type TenantPayment = {
  id: string
  leaseId: string
  amount: number
  paymentType: string
  status: string
  periodFrom: string
  periodTo: string
  referenceNo?: string
  paymentDate?: string
  createdAt: string
}

export type TenantMaintenanceRequest = {
  id: string
  unitId: string
  category: string
  priority: string
  title: string
  description: string
  status: string
  createdAt: string
}

export type TenantDocument = {
  id: string
  targetType: string
  targetId: string
  docType: string
  fileName: string
  fileSize: number
  fileUrl: string
  notes?: string
  uploadedAt: string
}

export type SubmitTenantMaintenanceInput = {
  unitId: string
  category: string
  priority: string
  title: string
  description: string
}

export type SubmitLeaseExtensionInput = {
  leaseId: string
  durationMonths?: number
  customEndDate?: string
  notes?: string
}

export const fetchTenantOverview = (): Promise<TenantOverview> =>
  api.get<{ data: TenantOverview }>('/tenant/overview').then((r) => r.data.data)

export const fetchTenantLease = (): Promise<TenantLease> =>
  api.get<{ data: TenantLease }>('/tenant/lease').then((r) => r.data.data)

export const fetchTenantPayments = (): Promise<TenantPayment[]> =>
  api.get<{ data: TenantPayment[] }>('/tenant/payments').then((r) => r.data.data)

export const fetchTenantMaintenance = (): Promise<TenantMaintenanceRequest[]> =>
  api.get<{ data: TenantMaintenanceRequest[] }>('/tenant/maintenance').then((r) => r.data.data)

export const createTenantMaintenanceRequest = (
  data: SubmitTenantMaintenanceInput,
): Promise<TenantMaintenanceRequest> =>
  api.post<{ data: TenantMaintenanceRequest }>('/tenant/maintenance', data).then((r) => r.data.data)

export const fetchTenantDocuments = (): Promise<TenantDocument[]> =>
  api.get<{ data: TenantDocument[] }>('/tenant/documents').then((r) => r.data.data)

export const uploadTenantDocument = (formData: FormData): Promise<TenantDocument> =>
  api
    .post<{ data: TenantDocument }>('/tenant/documents', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    .then((r) => r.data.data)

export const fetchTenantPaymentMethods = (): Promise<PaymentMethodsResponse> =>
  api.get<{ data: PaymentMethodsResponse }>('/tenant/payment-methods').then((r) => r.data.data)

export const submitLeaseExtensionRequest = (data: SubmitLeaseExtensionInput): Promise<void> =>
  api.post('/tenant/lease/extension-request', data).then(() => undefined)
