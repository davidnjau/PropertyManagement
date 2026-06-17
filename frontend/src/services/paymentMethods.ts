import api from './api'
import type { PaymentMethod, MpesaConfig, PaypalConfig } from '../context/PaymentMethodsContext'

type BackendPaymentMethodsResponse = {
  methods: { methodId: string; enabled: boolean }[]
  mpesaConfig: { businessNo: string; accountNo: string; instructions: string } | null
  paypalConfig: { email: string; instructions: string } | null
  banks: { bankId: string; bankName: string; enabled: boolean }[]
}

const METHOD_LABELS: Record<string, string> = {
  mpesa: 'M-Pesa',
  paypal: 'PayPal',
  bank_transfer: 'Bank Transfer',
}

const DEFAULT_MPESA: MpesaConfig = { businessNo: '', accountNo: '', instructions: '' }
const DEFAULT_PAYPAL: PaypalConfig = { email: '', instructions: '' }

export type PaymentMethodsResponse = {
  methods: PaymentMethod[]
  mpesaConfig: MpesaConfig
  paypalConfig: PaypalConfig
}

export const fetchPaymentMethods = (): Promise<PaymentMethodsResponse> =>
  api.get<{ data: BackendPaymentMethodsResponse }>('/admin/payment-methods').then((r) => {
    const d = r.data.data
    const banks = (d.banks ?? []).map((b) => ({ id: b.bankId, name: b.bankName, enabled: b.enabled }))
    const methods: PaymentMethod[] = (d.methods ?? []).map((m) => ({
      id: m.methodId,
      label: METHOD_LABELS[m.methodId] ?? m.methodId,
      enabled: m.enabled,
      banks: m.methodId === 'bank_transfer' ? banks : undefined,
    }))
    return {
      methods,
      mpesaConfig: d.mpesaConfig ?? DEFAULT_MPESA,
      paypalConfig: d.paypalConfig ?? DEFAULT_PAYPAL,
    }
  })

export const togglePaymentMethod = (id: string, enabled: boolean): Promise<void> =>
  api.put(`/admin/payment-methods/${id}/toggle`, { enabled }).then(() => undefined)

export const toggleBankMethod = (bankId: string, enabled: boolean): Promise<void> =>
  api.put(`/admin/payment-methods/bank/${bankId}/toggle`, { enabled }).then(() => undefined)

export const saveMpesaConfig = (config: MpesaConfig): Promise<void> =>
  api.put('/admin/payment-methods/mpesa', config).then(() => undefined)

export const savePaypalConfig = (config: PaypalConfig): Promise<void> =>
  api.put('/admin/payment-methods/paypal', config).then(() => undefined)
