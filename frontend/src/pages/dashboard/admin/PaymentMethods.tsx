import { useState } from 'react'
import { usePaymentMethods, type MpesaConfig, type PaypalConfig } from '../../../context/PaymentMethodsContext'

function Toggle({ enabled, onToggle }: { enabled: boolean; onToggle: () => void }) {
  return (
    <button
      type="button"
      onClick={onToggle}
      className={`relative inline-flex h-5 w-9 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors focus:outline-none ${
        enabled ? 'bg-gray-900' : 'bg-gray-200'
      }`}
    >
      <span
        className={`pointer-events-none inline-block h-4 w-4 rounded-full bg-white shadow transition-transform ${
          enabled ? 'translate-x-4' : 'translate-x-0'
        }`}
      />
    </button>
  )
}

function SectionLabel({ children }: { children: React.ReactNode }) {
  return (
    <div className="border-b border-gray-100 px-5 py-3 bg-gray-50">
      <p className="text-xs font-semibold uppercase tracking-widest text-gray-500">{children}</p>
    </div>
  )
}

function Field({ label, name, value, onChange, textarea }: {
  label: string
  name: string
  value: string
  onChange: (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void
  textarea?: boolean
}) {
  const cls = 'w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 bg-white'
  return (
    <div>
      <label className="block text-xs font-semibold uppercase tracking-widest text-gray-400 mb-1.5">{label}</label>
      {textarea
        ? <textarea name={name} value={value} onChange={onChange} rows={3} className={`${cls} resize-none`} />
        : <input name={name} value={value} onChange={onChange} className={cls} />
      }
    </div>
  )
}

export default function PaymentMethodsAdmin() {
  const { methods, mpesaConfig, paypalConfig, toggleMethod, toggleBank, updateMpesaConfig, updatePaypalConfig } = usePaymentMethods()
  const bankTransfer = methods.find((m) => m.id === 'bank_transfer')

  const [mpesa, setMpesa] = useState<MpesaConfig>(mpesaConfig)
  const [paypal, setPaypal] = useState<PaypalConfig>(paypalConfig)
  const [mpesaSaved, setMpesaSaved] = useState(false)
  const [paypalSaved, setPaypalSaved] = useState(false)

  function handleMpesaChange(e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    setMpesa({ ...mpesa, [e.target.name]: e.target.value })
  }

  function handlePaypalChange(e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    setPaypal({ ...paypal, [e.target.name]: e.target.value })
  }

  function saveMpesa(e: React.FormEvent) {
    e.preventDefault()
    updateMpesaConfig(mpesa)
    setMpesaSaved(true)
    setTimeout(() => setMpesaSaved(false), 2500)
  }

  function savePaypal(e: React.FormEvent) {
    e.preventDefault()
    updatePaypalConfig(paypal)
    setPaypalSaved(true)
    setTimeout(() => setPaypalSaved(false), 2500)
  }

  return (
    <div className="w-full max-w-2xl">
      <h1 className="text-2xl font-bold text-gray-900 mb-1">Payment Methods</h1>
      <p className="text-sm text-gray-500 mb-8">
        Configure which payment channels are available and edit their details.
      </p>

      {/* ── Channels toggle ── */}
      <div className="border border-gray-100 rounded-xl overflow-hidden mb-8">
        <SectionLabel>Channels</SectionLabel>
        <div className="divide-y divide-gray-50">
          {methods.map((m) => (
            <div key={m.id} className="flex items-center justify-between px-5 py-4">
              <div>
                <p className="text-sm font-medium text-gray-900">{m.label}</p>
                {m.id === 'bank_transfer' && (
                  <p className="text-xs text-gray-400 mt-0.5">
                    {m.banks?.filter((b) => b.enabled).length ?? 0} of {m.banks?.length ?? 0} banks enabled
                  </p>
                )}
              </div>
              <Toggle enabled={m.enabled} onToggle={() => toggleMethod(m.id)} />
            </div>
          ))}
        </div>
      </div>

      {/* ── M-Pesa config ── */}
      {methods.find((m) => m.id === 'mpesa')?.enabled && (
        <div className="border border-gray-100 rounded-xl overflow-hidden mb-8">
          <SectionLabel>M-Pesa Payment Details</SectionLabel>
          <form onSubmit={saveMpesa} className="p-5 space-y-4">
            <Field label="Business / Paybill Number" name="businessNo" value={mpesa.businessNo} onChange={handleMpesaChange} />
            <Field label="Account Number / Reference" name="accountNo" value={mpesa.accountNo} onChange={handleMpesaChange} />
            <Field label="Additional Instructions" name="instructions" value={mpesa.instructions} onChange={handleMpesaChange} textarea />
            <div className="flex items-center gap-3 pt-1">
              <button type="submit"
                className="bg-gray-900 text-white text-sm font-medium px-5 py-2 rounded-md hover:bg-gray-700 transition-colors">
                Save
              </button>
              {mpesaSaved && <span className="text-xs text-green-600 font-medium">Saved</span>}
            </div>
          </form>
        </div>
      )}

      {/* ── PayPal config ── */}
      {methods.find((m) => m.id === 'paypal')?.enabled && (
        <div className="border border-gray-100 rounded-xl overflow-hidden mb-8">
          <SectionLabel>PayPal Payment Details</SectionLabel>
          <form onSubmit={savePaypal} className="p-5 space-y-4">
            <Field label="PayPal Email Address" name="email" value={paypal.email} onChange={handlePaypalChange} />
            <Field label="Instructions for Tenants" name="instructions" value={paypal.instructions} onChange={handlePaypalChange} textarea />
            <div className="flex items-center gap-3 pt-1">
              <button type="submit"
                className="bg-gray-900 text-white text-sm font-medium px-5 py-2 rounded-md hover:bg-gray-700 transition-colors">
                Save
              </button>
              {paypalSaved && <span className="text-xs text-green-600 font-medium">Saved</span>}
            </div>
          </form>
        </div>
      )}

      {/* ── Kenyan Banks list ── */}
      {bankTransfer?.enabled && (
        <div className="border border-gray-100 rounded-xl overflow-hidden">
          <div className="border-b border-gray-100 px-5 py-3 bg-gray-50 flex items-center justify-between">
            <p className="text-xs font-semibold uppercase tracking-widest text-gray-500">Kenyan Banks</p>
            <p className="text-xs text-gray-400">
              {bankTransfer.banks?.filter((b) => b.enabled).length} enabled
            </p>
          </div>
          <div className="divide-y divide-gray-50 max-h-[420px] overflow-y-auto">
            {bankTransfer.banks?.map((bank) => (
              <div key={bank.id} className="flex items-center justify-between px-5 py-3">
                <p className={`text-sm ${bank.enabled ? 'text-gray-900' : 'text-gray-400'}`}>
                  {bank.name}
                </p>
                <Toggle enabled={bank.enabled} onToggle={() => toggleBank(bank.id)} />
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
