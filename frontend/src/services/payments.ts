import api from './api'

export type Payment = {
  id: string
  tenant: string
  amount: number
  dueDate: string
  status: string
  paymentMethod: string
  bank: string
  reference: string
}

export type CreatePaymentInput = Omit<Payment, 'id'>

export const fetchPayments = (): Promise<Payment[]> =>
  api.get<Payment[]>('/payments').then((r) => r.data)

export const createPayment = (data: CreatePaymentInput): Promise<Payment> =>
  api.post<Payment>('/payments', data).then((r) => r.data)
