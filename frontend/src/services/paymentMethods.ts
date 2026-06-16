import api from './api'
import type { PaymentMethod, MpesaConfig, PaypalConfig } from '../context/PaymentMethodsContext'

export type PaymentMethodsResponse = {
  methods: PaymentMethod[]
  mpesaConfig: MpesaConfig
  paypalConfig: PaypalConfig
}

export const fetchPaymentMethods = (): Promise<PaymentMethodsResponse> =>
  api.get<PaymentMethodsResponse>('/admin/payment-methods').then((r) => r.data)

export const togglePaymentMethod = (id: string, enabled: boolean): Promise<void> =>
  api.put(`/admin/payment-methods/${id}/toggle`, { enabled }).then(() => undefined)

export const toggleBankMethod = (bankId: string, enabled: boolean): Promise<void> =>
  api.put(`/admin/payment-methods/bank/${bankId}/toggle`, { enabled }).then(() => undefined)

export const saveMpesaConfig = (config: MpesaConfig): Promise<void> =>
  api.put('/admin/payment-methods/mpesa', config).then(() => undefined)

export const savePaypalConfig = (config: PaypalConfig): Promise<void> =>
  api.put('/admin/payment-methods/paypal', config).then(() => undefined)
