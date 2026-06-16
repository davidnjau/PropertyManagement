import { createContext, useContext, useState } from 'react'

export type Bank = { id: string; name: string; enabled: boolean }

export type PaymentMethod = {
  id: string
  label: string
  enabled: boolean
  banks?: Bank[]
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

type ContextValue = {
  methods: PaymentMethod[]
  toggleMethod: (id: string) => void
  toggleBank: (bankId: string) => void
}

const PaymentMethodsContext = createContext<ContextValue | null>(null)

export function PaymentMethodsProvider({ children }: { children: React.ReactNode }) {
  const [methods, setMethods] = useState<PaymentMethod[]>(DEFAULT_METHODS)

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
    <PaymentMethodsContext.Provider value={{ methods, toggleMethod, toggleBank }}>
      {children}
    </PaymentMethodsContext.Provider>
  )
}

export function usePaymentMethods() {
  const ctx = useContext(PaymentMethodsContext)
  if (!ctx) throw new Error('usePaymentMethods must be used inside PaymentMethodsProvider')
  return ctx
}
