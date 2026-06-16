import { useState } from 'react'
import { CheckCircle } from 'lucide-react'
import { usePaymentMethods } from '../../context/PaymentMethodsContext'

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
  reference: '',
}

export default function PayRent() {
  const { methods } = usePaymentMethods()
  const enabledMethods = methods.filter((m) => m.enabled)
  const bankTransfer = methods.find((m) => m.id === 'bank_transfer')
  const enabledBanks = bankTransfer?.banks?.filter((b) => b.enabled) ?? []

  const [form, setForm] = useState(emptyForm)
  const [payments, setPayments] = useState<Payment[]>([])
  const [submitted, setSubmitted] = useState(false)

  function handleChange(e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) {
    const { name, value } = e.target
    if (name === 'method' && value !== 'bank_transfer') {
      setForm({ ...form, method: value, bank: '' })
    } else {
      setForm({ ...form, [name]: name === 'amount' ? Number(value) : value })
    }
  }

  function selectMethod(id: string) {
    setForm({ ...form, method: id, bank: '' })
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    const methodLabel = methods.find((m) => m.id === form.method)?.label ?? form.method
    setPayments([
      {
        id: Date.now(),
        period: form.period,
        amount: form.amount,
        method: methodLabel,
        bank: form.bank,
        reference: form.reference || `REF-${Date.now()}`,
        date: new Date().toLocaleDateString('en-KE'),
      },
      ...payments,
    ])
    setForm(emptyForm)
    setSubmitted(true)
    setTimeout(() => setSubmitted(false), 4000)
  }

  return (
    <div className="w-full">
      <h1 className="text-2xl font-bold text-gray-900 mb-1">Pay Rent</h1>
      <p className="text-sm text-gray-500 mb-8">Submit your rent payment for the current period.</p>

      {/* Success toast */}
      {submitted && (
        <div className="flex items-center gap-3 bg-green-50 border border-green-100 rounded-xl px-5 py-4 mb-6">
          <CheckCircle size={16} className="text-green-600 shrink-0" />
          <p className="text-sm text-green-800 font-medium">Payment submitted successfully.</p>
        </div>
      )}

      {/* Payment form */}
      <div className="border border-gray-100 rounded-2xl p-7 mb-8">
        <form onSubmit={handleSubmit} className="space-y-5">

          {/* Rent period */}
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
            <input
              name="amount"
              type="number"
              value={form.amount}
              onChange={handleChange}
              className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400"
            />
          </div>

          {/* Payment method */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Payment method</label>
            {enabledMethods.length === 0 ? (
              <p className="text-xs text-red-500 bg-red-50 border border-red-100 rounded-md px-3 py-2.5">
                No payment methods are currently available. Please contact your agent.
              </p>
            ) : (
              <div className="grid grid-cols-3 gap-2">
                {enabledMethods.map((m) => (
                  <button
                    key={m.id}
                    type="button"
                    onClick={() => selectMethod(m.id)}
                    className={`py-3 px-2 rounded-md border text-sm font-medium transition-colors ${
                      form.method === m.id
                        ? 'border-gray-900 bg-gray-900 text-white'
                        : 'border-gray-200 text-gray-700 hover:border-gray-400'
                    }`}
                  >
                    {m.label}
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* M-Pesa instructions */}
          {form.method === 'mpesa' && (
            <div className="bg-green-50 border border-green-100 rounded-md px-4 py-3 text-sm text-green-800 space-y-1">
              <p className="font-semibold">M-Pesa instructions</p>
              <p>Go to <span className="font-medium">M-Pesa → Lipa na M-Pesa → Pay Bill</span></p>
              <p>Business No: <span className="font-medium">123456</span> · Account No: <span className="font-medium">your unit number</span></p>
            </div>
          )}

          {/* PayPal instructions */}
          {form.method === 'paypal' && (
            <div className="bg-blue-50 border border-blue-100 rounded-md px-4 py-3 text-sm text-blue-800 space-y-1">
              <p className="font-semibold">PayPal instructions</p>
              <p>Send payment to <span className="font-medium">payments@buildagent.example</span></p>
              <p>Include your unit number as the payment note.</p>
            </div>
          )}

          {/* Bank selection — only for bank_transfer */}
          {form.method === 'bank_transfer' && (
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
              {form.bank && (
                <div className="mt-3 bg-gray-50 border border-gray-100 rounded-md px-4 py-3 text-sm text-gray-700 space-y-1">
                  <p className="font-semibold">Bank transfer details</p>
                  <p>Bank: <span className="font-medium">{form.bank}</span></p>
                  <p>Account Name: <span className="font-medium">BuildAgent Properties Ltd</span></p>
                  <p>Account No: <span className="font-medium">1234567890</span></p>
                  <p>Branch: <span className="font-medium">Main Branch</span></p>
                </div>
              )}
            </div>
          )}

          {/* Reference */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">
              Payment reference <span className="text-gray-400 font-normal">(optional)</span>
            </label>
            <input
              name="reference"
              value={form.reference}
              onChange={handleChange}
              placeholder="e.g. M-Pesa confirmation code"
              className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 placeholder:text-gray-300"
            />
          </div>

          <button
            type="submit"
            disabled={!form.method || !form.period}
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
