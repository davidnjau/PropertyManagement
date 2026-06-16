import api from './api'
import type { PaymentMethodsResponse } from './paymentMethods'

export type TenantOverview = {
  rentDue: number
  rentDueDate: string
  rentStatus: string
  leaseEndDate: string
  recentPayments: {
    month: string
    amount: number
    method: string
    status: string
  }[]
}

export type TenantLease = {
  unit: string
  building: string
  floor: string
  startDate: string
  endDate: string
  monthlyRent: string
  deposit: string
  status: string
}

export type TenantPayment = {
  id: string
  period: string
  amount: number
  method: string
  bank: string
  reference: string
  date: string
}

export type TenantMaintenanceRequest = {
  id: string
  title: string
  description: string
  priority: string
  date: string
  status: string
}

export type TenantDocument = {
  id: string
  name: string
  type: string
  size: string
  date: string
  uploaded: boolean
  url?: string
}

export type SubmitTenantPaymentInput = {
  period: string
  amount: number
  method: string
  bank: string
  reference: string
}

export type SubmitTenantMaintenanceInput = {
  title: string
  description: string
  priority: string
}

export type SubmitLeaseExtensionInput = {
  duration: string
  customDate?: string
  notes: string
}

export const fetchTenantOverview = (): Promise<TenantOverview> =>
  api.get<TenantOverview>('/tenant/overview').then((r) => r.data)

export const fetchTenantLease = (): Promise<TenantLease> =>
  api.get<TenantLease>('/tenant/lease').then((r) => r.data)

export const fetchTenantPayments = (): Promise<TenantPayment[]> =>
  api.get<TenantPayment[]>('/tenant/payments').then((r) => r.data)

export const submitTenantPayment = (data: SubmitTenantPaymentInput): Promise<TenantPayment> =>
  api.post<TenantPayment>('/tenant/payments', data).then((r) => r.data)

export const fetchTenantMaintenance = (): Promise<TenantMaintenanceRequest[]> =>
  api.get<TenantMaintenanceRequest[]>('/tenant/maintenance').then((r) => r.data)

export const createTenantMaintenanceRequest = (
  data: SubmitTenantMaintenanceInput,
): Promise<TenantMaintenanceRequest> =>
  api.post<TenantMaintenanceRequest>('/tenant/maintenance', data).then((r) => r.data)

export const fetchTenantDocuments = (): Promise<TenantDocument[]> =>
  api.get<TenantDocument[]>('/tenant/documents').then((r) => r.data)

export const uploadTenantDocument = (formData: FormData): Promise<TenantDocument> =>
  api
    .post<TenantDocument>('/tenant/documents', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    .then((r) => r.data)

export const fetchTenantPaymentMethods = (): Promise<PaymentMethodsResponse> =>
  api.get<PaymentMethodsResponse>('/tenant/payment-methods').then((r) => r.data)

export const submitLeaseExtensionRequest = (data: SubmitLeaseExtensionInput): Promise<void> =>
  api.post('/tenant/lease/extension-request', data).then(() => undefined)
