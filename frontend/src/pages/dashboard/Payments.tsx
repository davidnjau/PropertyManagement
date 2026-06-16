import { useState } from 'react'
import { X } from 'lucide-react'
import { usePaymentMethods } from '../../context/PaymentMethodsContext'

type Payment = {
  id: number
  tenant: string
  amount: number
  dueDate: string
  status: string
  paymentMethod: string
  bank: string
}

const emptyForm = {
  tenant: '',
  amount: 0,
  dueDate: '',
  status: 'Pending',
  paymentMethod: '',
  bank: '',
}

export default function Payments() {
  const { methods } = usePaymentMethods()
  const enabledMethods = methods.filter((m) => m.enabled)
  const bankTransfer = methods.find((m) => m.id === 'bank_transfer')
  const enabledBanks = bankTransfer?.banks?.filter((b) => b.enabled) ?? []

  const [payments, setPayments] = useState<Payment[]>([])
  const [open, setOpen] = useState(false)
  const [form, setForm] = useState(emptyForm)

  function handleChange(e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) {
    const { name, value } = e.target
    // Reset bank when payment method changes away from bank_transfer
    if (name === 'paymentMethod' && value !== 'bank_transfer') {
      setForm({ ...form, paymentMethod: value, bank: '' })
    } else {
      setForm({ ...form, [name]: name === 'amount' ? Number(value) : value })
    }
  }

  function handleSave(e: React.FormEvent) {
    e.preventDefault()
    setPayments([...payments, { ...form, id: Date.now() }])
    setForm(emptyForm)
    setOpen(false)
  }

  const statusColors: Record<string, string> = {
    Pending: 'bg-amber-50 text-amber-700',
    Paid: 'bg-green-50 text-green-700',
    Overdue: 'bg-red-50 text-red-700',
  }

  const methodLabel = (id: string) =>
    methods.find((m) => m.id === id)?.label ?? id

  return (
    <div className="max-w-5xl">
      <div className="flex items-start justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 mb-1">Payments</h1>
          <p className="text-sm text-gray-500">Rent & fee ledger.</p>
        </div>
        <button
          onClick={() => setOpen(true)}
          className="flex items-center gap-1.5 bg-gray-900 text-white text-sm font-medium px-4 py-2 rounded-md hover:bg-gray-700 transition-colors"
        >
          + Log payment
        </button>
      </div>

      <div className="border border-gray-100 rounded-xl overflow-hidden">
        <div className="border-b border-gray-100 px-5 py-3">
          <p className="text-sm font-semibold text-gray-900">Ledger</p>
        </div>
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-100 bg-gray-50">
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Tenant</th>
              <th className="text-right px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Amount</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Method</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Due date</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Status</th>
            </tr>
          </thead>
          <tbody>
            {payments.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-5 py-8 text-center text-sm text-gray-400">
                  No payments logged yet.
                </td>
              </tr>
            ) : (
              payments.map((p) => (
                <tr key={p.id} className="border-b border-gray-50 hover:bg-gray-50">
                  <td className="px-5 py-3 font-medium text-gray-900">{p.tenant}</td>
                  <td className="px-5 py-3 text-right text-gray-900">{p.amount.toLocaleString()}</td>
                  <td className="px-5 py-3 text-gray-500">
                    {methodLabel(p.paymentMethod)}
                    {p.bank && <span className="text-gray-400"> · {p.bank}</span>}
                  </td>
                  <td className="px-5 py-3 text-gray-500">{p.dueDate}</td>
                  <td className="px-5 py-3">
                    <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${statusColors[p.status] ?? ''}`}>
                      {p.status}
                    </span>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {open && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl w-full max-w-md shadow-xl max-h-[90vh] overflow-y-auto">
            <div className="p-8">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-lg font-bold text-gray-900">New payment</h2>
                <button onClick={() => setOpen(false)} className="text-gray-400 hover:text-gray-600">
                  <X size={18} />
                </button>
              </div>

              <form onSubmit={handleSave} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">Tenant</label>
                  <select name="tenant" value={form.tenant} onChange={handleChange}
                    className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 bg-white">
                    <option value="">Choose tenant</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">Amount</label>
                  <input name="amount" type="number" value={form.amount} onChange={handleChange}
                    className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400" />
                </div>

                {/* Payment method */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">Payment method</label>
                  {enabledMethods.length === 0 ? (
                    <p className="text-xs text-red-500">No payment methods enabled. Configure them in Admin → Payment Methods.</p>
                  ) : (
                    <div className="grid grid-cols-3 gap-2">
                      {enabledMethods.map((m) => (
                        <button
                          key={m.id}
                          type="button"
                          onClick={() => setForm({ ...form, paymentMethod: m.id, bank: '' })}
                          className={`py-2.5 px-2 rounded-md border text-sm font-medium transition-colors ${
                            form.paymentMethod === m.id
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

                {/* Bank — only when Bank Transfer is selected */}
                {form.paymentMethod === 'bank_transfer' && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1.5">Bank</label>
                    <select name="bank" value={form.bank} onChange={handleChange}
                      className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 bg-white">
                      <option value="">Select bank</option>
                      {enabledBanks.map((b) => (
                        <option key={b.id} value={b.name}>{b.name}</option>
                      ))}
                    </select>
                  </div>
                )}

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">Due date</label>
                  <input name="dueDate" type="date" value={form.dueDate} onChange={handleChange}
                    className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400" />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">Status</label>
                  <select name="status" value={form.status} onChange={handleChange}
                    className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 bg-white">
                    <option>Pending</option>
                    <option>Paid</option>
                    <option>Overdue</option>
                  </select>
                </div>

                <div className="flex justify-end pt-2">
                  <button type="submit"
                    className="bg-gray-200 text-gray-700 text-sm font-medium px-5 py-2 rounded-md hover:bg-gray-300 transition-colors">
                    Save
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
