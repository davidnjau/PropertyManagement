import api from './api'

export type Payment = {
  id: string
  leaseId: string
  agencyId: string
  amount: number
  paymentType: string
  status: string
  periodFrom: string
  periodTo: string
  referenceNo?: string
  notes?: string
  recordedBy?: string
  paymentDate?: string
  isAdjustment: boolean
  voided: boolean
  createdAt: string
  updatedAt: string
}

export type RecordPaymentInput = {
  leaseId: string
  amount: number
  paymentType: string
  status: string
  periodFrom: string
  periodTo: string
  referenceNo?: string
  notes?: string
  paymentDate?: string
}

type PagedResponse<T> = { data: T[]; meta: { total: number; page: number; limit: number; pages: number } }

export const fetchPayments = (): Promise<Payment[]> =>
  api.get<PagedResponse<Payment>>('/payments').then((r) => r.data.data)

export const recordPayment = (data: RecordPaymentInput): Promise<Payment> =>
  api.post<{ data: Payment }>('/payments', data).then((r) => r.data.data)
