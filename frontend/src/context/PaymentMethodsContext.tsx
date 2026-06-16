import { createContext, useContext, useState } from 'react'

export type Bank = { id: string; name: string; enabled: boolean }

export type PaymentMethod = {
  id: string
  label: string
  enabled: boolean
  banks?: Bank[]
}

export type MpesaConfig = {
  businessNo: string
  accountNo: string
  instructions: string
}

export type PaypalConfig = {
  email: string
  instructions: string
}

const KENYAN_BANKS: string[] = [
  'KCB Bank',
  'Equity Bank',
  'Co-operative Bank',
  'Absa Bank Kenya',
  'Standard Chartered Kenya',
  'NCBA Bank',
  'Stanbic Bank Kenya',
  'I&M Bank',
  'Diamond Trust Bank (DTB)',
  'Family Bank',
  'HF Group',
  'Prime Bank',
  'Bank of Africa Kenya',
  'Sidian Bank',
  'Gulf African Bank',
  'National Bank of Kenya',
  'Development Bank of Kenya',
  'Consolidated Bank Kenya',
]

const DEFAULT_METHODS: PaymentMethod[] = [
  { id: 'mpesa', label: 'M-Pesa', enabled: true },
  { id: 'paypal', label: 'PayPal', enabled: true },
  {
    id: 'bank_transfer',
    label: 'Bank Transfer',
    enabled: true,
    banks: KENYAN_BANKS.map((name) => ({
      id: name.toLowerCase().replace(/\s+/g, '_').replace(/[^a-z0-9_]/g, ''),
      name,
      enabled: true,
    })),
  },
]

const DEFAULT_MPESA: MpesaConfig = {
  businessNo: '123456',
  accountNo: 'Your unit number',
  instructions: 'Go to M-Pesa → Lipa na M-Pesa → Pay Bill. Enter the business number and use your unit number as the account number.',
}

const DEFAULT_PAYPAL: PaypalConfig = {
  email: 'payments@buildagent.example',
  instructions: 'Send payment to the email above. Include your unit number and rent period as the payment note.',
}

type ContextValue = {
  methods: PaymentMethod[]
  mpesaConfig: MpesaConfig
  paypalConfig: PaypalConfig
  toggleMethod: (id: string) => void
  toggleBank: (bankId: string) => void
  updateMpesaConfig: (config: MpesaConfig) => void
  updatePaypalConfig: (config: PaypalConfig) => void
}

const PaymentMethodsContext = createContext<ContextValue | null>(null)

export function PaymentMethodsProvider({ children }: { children: React.ReactNode }) {
  const [methods, setMethods] = useState<PaymentMethod[]>(DEFAULT_METHODS)
  const [mpesaConfig, setMpesaConfig] = useState<MpesaConfig>(DEFAULT_MPESA)
  const [paypalConfig, setPaypalConfig] = useState<PaypalConfig>(DEFAULT_PAYPAL)

  function toggleMethod(id: string) {
    setMethods((prev) =>
      prev.map((m) => (m.id === id ? { ...m, enabled: !m.enabled } : m))
    )
  }

  function toggleBank(bankId: string) {
    setMethods((prev) =>
      prev.map((m) =>
        m.id === 'bank_transfer'
          ? {
              ...m,
              banks: m.banks?.map((b) =>
                b.id === bankId ? { ...b, enabled: !b.enabled } : b
              ),
            }
          : m
      )
    )
  }

  return (
    <PaymentMethodsContext.Provider value={{
      methods,
      mpesaConfig,
      paypalConfig,
      toggleMethod,
      toggleBank,
      updateMpesaConfig: setMpesaConfig,
      updatePaypalConfig: setPaypalConfig,
    }}>
      {children}
    </PaymentMethodsContext.Provider>
  )
}

export function usePaymentMethods() {
  const ctx = useContext(PaymentMethodsContext)
  if (!ctx) throw new Error('usePaymentMethods must be used inside PaymentMethodsProvider')
  return ctx
}
