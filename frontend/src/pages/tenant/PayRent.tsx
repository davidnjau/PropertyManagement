import { useState } from 'react'
import { CheckCircle, Copy } from 'lucide-react'
import { usePaymentMethods } from '../../context/PaymentMethodsContext'

type MpesaAction = 'initiate' | 'reference' | 'details'

type Payment = {
  id: number
  period: string
  amount: number
  method: string
  bank: string
  reference: string
  date: string
}

const emptyForm = {
  period: '',
  amount: 45000,
  method: '',
  bank: '',
  // mpesa fields
  phone: '',
  mpesaRef: '',
  mpesaAction: '' as MpesaAction | '',
  // paypal fields
  paypalEmail: '',
}

function CopyButton({ text }: { text: string }) {
  const [copied, setCopied] = useState(false)
  function copy() {
    navigator.clipboard.writeText(text)
    setCopied(true)
    setTimeout(() => setCopied(false), 1500)
  }
  return (
    <button type="button" onClick={copy}
      className="flex items-center gap-1 text-xs text-gray-400 hover:text-gray-700 transition-colors">
      <Copy size={12} />
      {copied ? 'Copied' : 'Copy'}
    </button>
  )
}

export default function PayRent() {
  const { methods, mpesaConfig, paypalConfig } = usePaymentMethods()
  const enabledMethods = methods.filter((m) => m.enabled)
  const bankTransfer = methods.find((m) => m.id === 'bank_transfer')
  const enabledBanks = bankTransfer?.banks?.filter((b) => b.enabled) ?? []

  const [form, setForm] = useState(emptyForm)
  const [payments, setPayments] = useState<Payment[]>([])
  const [submitted, setSubmitted] = useState(false)

  function handleChange(e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) {
    const { name, value } = e.target
    if (name === 'method') {
      setForm({ ...emptyForm, period: form.period, amount: form.amount, method: value })
    } else {
      setForm({ ...form, [name]: name === 'amount' ? Number(value) : value })
    }
  }

  function selectMethod(id: string) {
    setForm({ ...emptyForm, period: form.period, amount: form.amount, method: id })
  }

  function selectMpesaAction(action: MpesaAction) {
    setForm({ ...form, mpesaAction: action, phone: '', mpesaRef: '' })
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    const methodLabel = methods.find((m) => m.id === form.method)?.label ?? form.method
    const ref = form.mpesaRef || form.paypalEmail || form.phone || `REF-${Date.now()}`
    setPayments([
      {
        id: Date.now(),
        period: form.period,
        amount: form.amount,
        method: methodLabel,
        bank: form.bank,
        reference: ref,
        date: new Date().toLocaleDateString('en-KE'),
      },
      ...payments,
    ])
    setForm(emptyForm)
    setSubmitted(true)
    setTimeout(() => setSubmitted(false), 4000)
  }

  const canSubmit =
    form.period &&
    form.method &&
    (form.method === 'mpesa'
      ? form.mpesaAction === 'initiate'
        ? form.phone.length >= 9
        : form.mpesaAction === 'reference'
        ? form.mpesaRef.length > 0
        : form.mpesaAction === 'details' // view details — no extra input needed, just allow logging
      : form.method === 'paypal'
      ? form.paypalEmail.length > 0
      : form.method === 'bank_transfer'
      ? true
      : false)

  return (
    <div className="w-full">
      <h1 className="text-2xl font-bold text-gray-900 mb-1">Pay Rent</h1>
      <p className="text-sm text-gray-500 mb-8">Submit your rent payment for the current period.</p>

      {submitted && (
        <div className="flex items-center gap-3 bg-green-50 border border-green-100 rounded-xl px-5 py-4 mb-6">
          <CheckCircle size={16} className="text-green-600 shrink-0" />
          <p className="text-sm text-green-800 font-medium">Payment submitted successfully.</p>
        </div>
      )}

      <div className="border border-gray-100 rounded-2xl p-7 mb-8">
        <form onSubmit={handleSubmit} className="space-y-6">

          {/* Period */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Rent period</label>
            <select name="period" value={form.period} onChange={handleChange}
              className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 bg-white">
              <option value="">Select month</option>
              {['July 2026', 'August 2026', 'September 2026'].map((m) => (
                <option key={m}>{m}</option>
              ))}
            </select>
          </div>

          {/* Amount */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Amount (KES)</label>
            <input name="amount" type="number" value={form.amount} onChange={handleChange}
              className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400" />
          </div>

          {/* Payment method picker */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Payment method</label>
            {enabledMethods.length === 0 ? (
              <p className="text-xs text-red-500 bg-red-50 border border-red-100 rounded-md px-3 py-2.5">
                No payment methods are currently available. Please contact your agent.
              </p>
            ) : (
              <div className="grid grid-cols-3 gap-2">
                {enabledMethods.map((m) => (
                  <button key={m.id} type="button" onClick={() => selectMethod(m.id)}
                    className={`py-3 px-2 rounded-md border text-sm font-medium transition-colors ${
                      form.method === m.id
                        ? 'border-gray-900 bg-gray-900 text-white'
                        : 'border-gray-200 text-gray-700 hover:border-gray-400'
                    }`}>
                    {m.label}
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* ── M-Pesa flow ── */}
          {form.method === 'mpesa' && (
            <div className="space-y-4">
              {/* Sub-option tabs */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">How would you like to pay?</label>
                <div className="grid grid-cols-3 gap-2">
                  {([
                    { id: 'initiate', label: 'Initiate Payment' },
                    { id: 'reference', label: 'Provide Reference' },
                    { id: 'details', label: 'View Details' },
                  ] as { id: MpesaAction; label: string }[]).map((opt) => (
                    <button key={opt.id} type="button" onClick={() => selectMpesaAction(opt.id)}
                      className={`py-2.5 px-2 rounded-md border text-sm font-medium transition-colors text-center ${
                        form.mpesaAction === opt.id
                          ? 'border-green-600 bg-green-50 text-green-700'
                          : 'border-gray-200 text-gray-600 hover:border-gray-400'
                      }`}>
                      {opt.label}
                    </button>
                  ))}
                </div>
              </div>

              {/* Initiate Payment — phone number */}
              {form.mpesaAction === 'initiate' && (
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">Phone number</label>
                  <div className="flex gap-2">
                    <span className="flex items-center px-3 border border-r-0 border-gray-200 rounded-l-md bg-gray-50 text-sm text-gray-500">
                      +254
                    </span>
                    <input
                      name="phone"
                      type="tel"
                      value={form.phone}
                      onChange={handleChange}
                      placeholder="7XX XXX XXX"
                      maxLength={9}
                      className="flex-1 border border-gray-200 rounded-r-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 placeholder:text-gray-300"
                    />
                  </div>
                  <p className="text-xs text-gray-400 mt-1">
                    An STK push will be sent to this number to complete payment of KES {form.amount.toLocaleString()}.
                  </p>
                </div>
              )}

              {/* Provide Reference — confirmation code */}
              {form.mpesaAction === 'reference' && (
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">M-Pesa confirmation code</label>
                  <input
                    name="mpesaRef"
                    value={form.mpesaRef}
                    onChange={handleChange}
                    placeholder="e.g. QA5X3Y2Z1"
                    className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm font-mono focus:outline-none focus:border-gray-400 placeholder:text-gray-300 uppercase"
                  />
                  <p className="text-xs text-gray-400 mt-1">
                    Enter the confirmation code from your M-Pesa transaction message.
                  </p>
                </div>
              )}

              {/* View Details — admin-configured paybill info */}
              {form.mpesaAction === 'details' && (
                <div className="bg-green-50 border border-green-100 rounded-xl p-5 space-y-3">
                  <p className="text-sm font-semibold text-green-800">M-Pesa Payment Details</p>
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-xs text-green-700 font-medium">Business / Paybill No</span>
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-bold text-green-900">{mpesaConfig.businessNo}</span>
                        <CopyButton text={mpesaConfig.businessNo} />
                      </div>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-xs text-green-700 font-medium">Account No</span>
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-bold text-green-900">{mpesaConfig.accountNo}</span>
                        <CopyButton text={mpesaConfig.accountNo} />
                      </div>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-xs text-green-700 font-medium">Amount</span>
                      <span className="text-sm font-bold text-green-900">KES {form.amount.toLocaleString()}</span>
                    </div>
                  </div>
                  {mpesaConfig.instructions && (
                    <p className="text-xs text-green-700 border-t border-green-200 pt-3">
                      {mpesaConfig.instructions}
                    </p>
                  )}
                </div>
              )}
            </div>
          )}

          {/* ── PayPal flow ── */}
          {form.method === 'paypal' && (
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Your PayPal email address</label>
                <input
                  name="paypalEmail"
                  type="email"
                  value={form.paypalEmail}
                  onChange={handleChange}
                  placeholder="you@example.com"
                  className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 placeholder:text-gray-300"
                />
              </div>
              <div className="bg-blue-50 border border-blue-100 rounded-xl p-5 space-y-2">
                <p className="text-sm font-semibold text-blue-800">PayPal Payment Details</p>
                <div className="flex items-center justify-between">
                  <span className="text-xs text-blue-700 font-medium">Send payment to</span>
                  <div className="flex items-center gap-2">
                    <span className="text-sm font-bold text-blue-900">{paypalConfig.email}</span>
                    <CopyButton text={paypalConfig.email} />
                  </div>
                </div>
                {paypalConfig.instructions && (
                  <p className="text-xs text-blue-700 border-t border-blue-200 pt-3">
                    {paypalConfig.instructions}
                  </p>
                )}
              </div>
            </div>
          )}

          {/* ── Bank Transfer flow ── */}
          {form.method === 'bank_transfer' && (
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Select bank</label>
                {enabledBanks.length === 0 ? (
                  <p className="text-xs text-red-500">No banks are currently enabled. Contact your agent.</p>
                ) : (
                  <select name="bank" value={form.bank} onChange={handleChange}
                    className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 bg-white">
                    <option value="">Select bank</option>
                    {enabledBanks.map((b) => (
                      <option key={b.id} value={b.name}>{b.name}</option>
                    ))}
                  </select>
                )}
              </div>
              {form.bank && (
                <div className="bg-gray-50 border border-gray-100 rounded-xl p-5 space-y-2">
                  <p className="text-sm font-semibold text-gray-900">Bank Transfer Details</p>
                  {[
                    { label: 'Bank', value: form.bank },
                    { label: 'Account Name', value: 'BuildAgent Properties Ltd' },
                    { label: 'Account No', value: '1234567890' },
                    { label: 'Branch', value: 'Main Branch' },
                  ].map(({ label, value }) => (
                    <div key={label} className="flex items-center justify-between">
                      <span className="text-xs text-gray-500 font-medium">{label}</span>
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-bold text-gray-900">{value}</span>
                        <CopyButton text={value} />
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          <button
            type="submit"
            disabled={!canSubmit}
            className="w-full bg-gray-900 text-white text-sm font-semibold py-3 rounded-md hover:bg-gray-700 transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
          >
            Submit payment
          </button>
        </form>
      </div>

      {/* Payment history */}
      {payments.length > 0 && (
        <div className="border border-gray-100 rounded-xl overflow-hidden">
          <div className="border-b border-gray-100 px-5 py-3">
            <p className="text-sm font-semibold text-gray-900">Payment history</p>
          </div>
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-gray-100 bg-gray-50">
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Period</th>
                <th className="text-right px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Amount</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Method</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Reference</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Date</th>
              </tr>
            </thead>
            <tbody>
              {payments.map((p) => (
                <tr key={p.id} className="border-b border-gray-50 hover:bg-gray-50">
                  <td className="px-5 py-3 font-medium text-gray-900">{p.period}</td>
                  <td className="px-5 py-3 text-right text-gray-900">KES {p.amount.toLocaleString()}</td>
                  <td className="px-5 py-3 text-gray-500">
                    {p.method}{p.bank && <span className="text-gray-400"> · {p.bank}</span>}
                  </td>
                  <td className="px-5 py-3 text-gray-400 font-mono text-xs">{p.reference}</td>
                  <td className="px-5 py-3 text-gray-500">{p.date}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
