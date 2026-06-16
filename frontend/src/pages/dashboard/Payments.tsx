import { useState } from 'react'
import { X, Copy } from 'lucide-react'
import { usePaymentMethods } from '../../context/PaymentMethodsContext'

type MpesaAction = 'initiate' | 'reference' | 'details'

type Payment = {
  id: number
  tenant: string
  amount: number
  dueDate: string
  status: string
  paymentMethod: string
  bank: string
  reference: string
}

const emptyForm = {
  tenant: '',
  amount: 0,
  dueDate: '',
  status: 'Pending',
  paymentMethod: '',
  bank: '',
  // mpesa
  phone: '',
  mpesaRef: '',
  mpesaAction: '' as MpesaAction | '',
  // paypal
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

export default function Payments() {
  const { methods, mpesaConfig, paypalConfig } = usePaymentMethods()
  const enabledMethods = methods.filter((m) => m.enabled)
  const bankTransfer = methods.find((m) => m.id === 'bank_transfer')
  const enabledBanks = bankTransfer?.banks?.filter((b) => b.enabled) ?? []

  const [payments, setPayments] = useState<Payment[]>([])
  const [open, setOpen] = useState(false)
  const [form, setForm] = useState(emptyForm)

  function handleChange(e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) {
    const { name, value } = e.target
    if (name === 'paymentMethod') {
      setForm({ ...emptyForm, tenant: form.tenant, amount: form.amount, dueDate: form.dueDate, status: form.status, paymentMethod: value })
    } else {
      setForm({ ...form, [name]: name === 'amount' ? Number(value) : value })
    }
  }

  function selectMethod(id: string) {
    setForm({ ...emptyForm, tenant: form.tenant, amount: form.amount, dueDate: form.dueDate, status: form.status, paymentMethod: id })
  }

  function selectMpesaAction(action: MpesaAction) {
    setForm({ ...form, mpesaAction: action, phone: '', mpesaRef: '' })
  }

  function handleSave(e: React.FormEvent) {
    e.preventDefault()
    const ref = form.mpesaRef || form.paypalEmail || form.phone || ''
    setPayments([...payments, { ...form, id: Date.now(), reference: ref }])
    setForm(emptyForm)
    setOpen(false)
  }

  const methodLabel = (id: string) => methods.find((m) => m.id === id)?.label ?? id

  const statusColors: Record<string, string> = {
    Pending: 'bg-amber-50 text-amber-700',
    Paid: 'bg-green-50 text-green-700',
    Overdue: 'bg-red-50 text-red-700',
  }

  const canSave =
    form.tenant &&
    form.amount > 0 &&
    form.paymentMethod &&
    (form.paymentMethod === 'mpesa'
      ? form.mpesaAction === 'initiate'
        ? form.phone.length >= 9
        : form.mpesaAction === 'reference'
        ? form.mpesaRef.length > 0
        : !!form.mpesaAction
      : form.paymentMethod === 'paypal'
      ? form.paypalEmail.length > 0
      : true)

  return (
    <div className="w-full">
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

      {/* Ledger table */}
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
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Reference</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Due date</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Status</th>
            </tr>
          </thead>
          <tbody>
            {payments.length === 0 ? (
              <tr>
                <td colSpan={6} className="px-5 py-8 text-center text-sm text-gray-400">No payments logged yet.</td>
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
                  <td className="px-5 py-3 text-gray-400 font-mono text-xs">{p.reference || '—'}</td>
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

      {/* Log Payment Modal */}
      {open && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl w-full max-w-md shadow-xl max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between px-7 pt-7 pb-5 border-b border-gray-100">
              <h2 className="text-lg font-bold text-gray-900">Log payment</h2>
              <button onClick={() => setOpen(false)} className="text-gray-400 hover:text-gray-600">
                <X size={18} />
              </button>
            </div>

            <form onSubmit={handleSave} className="px-7 py-6 space-y-5">

              {/* Tenant */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Tenant</label>
                <select name="tenant" value={form.tenant} onChange={handleChange}
                  className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 bg-white">
                  <option value="">Choose tenant</option>
                </select>
              </div>

              {/* Amount */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Amount</label>
                <input name="amount" type="number" value={form.amount} onChange={handleChange}
                  className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400" />
              </div>

              {/* Due date */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Due date</label>
                <input name="dueDate" type="date" value={form.dueDate} onChange={handleChange}
                  className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400" />
              </div>

              {/* Status */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Status</label>
                <select name="status" value={form.status} onChange={handleChange}
                  className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 bg-white">
                  <option>Pending</option>
                  <option>Paid</option>
                  <option>Overdue</option>
                </select>
              </div>

              {/* Payment method */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Payment method</label>
                {enabledMethods.length === 0 ? (
                  <p className="text-xs text-red-500 bg-red-50 border border-red-100 rounded-md px-3 py-2.5">
                    No payment methods enabled. Configure in Admin → Payment Methods.
                  </p>
                ) : (
                  <div className="grid grid-cols-3 gap-2">
                    {enabledMethods.map((m) => (
                      <button key={m.id} type="button" onClick={() => selectMethod(m.id)}
                        className={`py-2.5 px-2 rounded-md border text-sm font-medium transition-colors ${
                          form.paymentMethod === m.id
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
              {form.paymentMethod === 'mpesa' && (
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Payment action</label>
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

                  {form.mpesaAction === 'initiate' && (
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1.5">Tenant phone number</label>
                      <div className="flex gap-2">
                        <span className="flex items-center px-3 border border-r-0 border-gray-200 rounded-l-md bg-gray-50 text-sm text-gray-500">
                          +254
                        </span>
                        <input name="phone" type="tel" value={form.phone} onChange={handleChange}
                          placeholder="7XX XXX XXX" maxLength={9}
                          className="flex-1 border border-gray-200 rounded-r-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 placeholder:text-gray-300" />
                      </div>
                      <p className="text-xs text-gray-400 mt-1">An STK push will be sent to this number.</p>
                    </div>
                  )}

                  {form.mpesaAction === 'reference' && (
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1.5">M-Pesa confirmation code</label>
                      <input name="mpesaRef" value={form.mpesaRef} onChange={handleChange}
                        placeholder="e.g. QA5X3Y2Z1"
                        className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm font-mono focus:outline-none focus:border-gray-400 placeholder:text-gray-300 uppercase" />
                      <p className="text-xs text-gray-400 mt-1">Enter the confirmation code from the tenant's M-Pesa message.</p>
                    </div>
                  )}

                  {form.mpesaAction === 'details' && (
                    <div className="bg-green-50 border border-green-100 rounded-xl p-5 space-y-3">
                      <p className="text-sm font-semibold text-green-800">M-Pesa Payment Details</p>
                      <div className="space-y-2">
                        {[
                          { label: 'Business / Paybill No', value: mpesaConfig.businessNo },
                          { label: 'Account No', value: mpesaConfig.accountNo },
                        ].map(({ label, value }) => (
                          <div key={label} className="flex items-center justify-between">
                            <span className="text-xs text-green-700 font-medium">{label}</span>
                            <div className="flex items-center gap-2">
                              <span className="text-sm font-bold text-green-900">{value}</span>
                              <CopyButton text={value} />
                            </div>
                          </div>
                        ))}
                      </div>
                      {mpesaConfig.instructions && (
                        <p className="text-xs text-green-700 border-t border-green-200 pt-3">{mpesaConfig.instructions}</p>
                      )}
                    </div>
                  )}
                </div>
              )}

              {/* ── PayPal flow ── */}
              {form.paymentMethod === 'paypal' && (
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1.5">Tenant PayPal email</label>
                    <input name="paypalEmail" type="email" value={form.paypalEmail} onChange={handleChange}
                      placeholder="tenant@example.com"
                      className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 placeholder:text-gray-300" />
                  </div>
                  <div className="bg-blue-50 border border-blue-100 rounded-xl p-5 space-y-2">
                    <p className="text-sm font-semibold text-blue-800">PayPal Payment Details</p>
                    <div className="flex items-center justify-between">
                      <span className="text-xs text-blue-700 font-medium">Receive payment at</span>
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-bold text-blue-900">{paypalConfig.email}</span>
                        <CopyButton text={paypalConfig.email} />
                      </div>
                    </div>
                    {paypalConfig.instructions && (
                      <p className="text-xs text-blue-700 border-t border-blue-200 pt-3">{paypalConfig.instructions}</p>
                    )}
                  </div>
                </div>
              )}

              {/* ── Bank Transfer flow ── */}
              {form.paymentMethod === 'bank_transfer' && (
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1.5">Bank</label>
                    {enabledBanks.length === 0 ? (
                      <p className="text-xs text-red-500">No banks enabled. Configure in Admin → Payment Methods.</p>
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

              <div className="flex gap-3 pt-1">
                <button type="button" onClick={() => setOpen(false)}
                  className="flex-1 border border-gray-200 text-gray-700 text-sm font-medium py-2.5 rounded-md hover:border-gray-400 transition-colors">
                  Cancel
                </button>
                <button type="submit" disabled={!canSave}
                  className="flex-1 bg-gray-900 text-white text-sm font-semibold py-2.5 rounded-md hover:bg-gray-700 transition-colors disabled:opacity-40 disabled:cursor-not-allowed">
                  Save
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
