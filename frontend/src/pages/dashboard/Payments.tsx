import { useState } from 'react'
import { X } from 'lucide-react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { fetchPayments, recordPayment } from '../../services/payments'
import api from '../../services/api'

type Lease = { id: string; tenantId: string; unitId: string; rentAmount: number; status: string; startDate: string; endDate?: string }

const PAYMENT_TYPES = ['RENT', 'BOND', 'WATER', 'FEE', 'OTHER']
const PAYMENT_STATUSES = ['RECEIVED', 'PENDING', 'PARTIAL', 'WAIVED']

const EMPTY_FORM = {
  leaseId: '',
  amount: '',
  paymentType: 'RENT',
  status: 'RECEIVED',
  periodFrom: '',
  periodTo: '',
  referenceNo: '',
  notes: '',
  paymentDate: '',
}

const statusColors: Record<string, string> = {
  RECEIVED: 'bg-green-50 text-green-700',
  PENDING:  'bg-amber-50 text-amber-700',
  OVERDUE:  'bg-red-50 text-red-600',
  PARTIAL:  'bg-blue-50 text-blue-700',
  WAIVED:   'bg-gray-100 text-gray-500',
}

export default function Payments() {
  const qc = useQueryClient()

  const { data: payments = [], isLoading, isError } = useQuery({
    queryKey: ['payments'],
    queryFn: fetchPayments,
  })

  const { data: leasesResp } = useQuery({
    queryKey: ['leases'],
    queryFn: () => api.get<{ data: Lease[] }>('/leases').then((r) => r.data.data),
  })
  const leases = leasesResp ?? []

  const mutation = useMutation({
    mutationFn: recordPayment,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['payments'] }),
  })

  const [open, setOpen] = useState(false)
  const [form, setForm] = useState(EMPTY_FORM)

  function handleChange(e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  function handleSave(e: React.FormEvent) {
    e.preventDefault()
    mutation.mutate(
      {
        leaseId: form.leaseId,
        amount: Number(form.amount),
        paymentType: form.paymentType,
        status: form.status,
        periodFrom: form.periodFrom,
        periodTo: form.periodTo,
        referenceNo: form.referenceNo || undefined,
        notes: form.notes || undefined,
        paymentDate: form.paymentDate || undefined,
      },
      {
        onSuccess: () => {
          setForm(EMPTY_FORM)
          setOpen(false)
        },
      },
    )
  }

  const canSave = form.leaseId && form.amount && Number(form.amount) > 0 && form.periodFrom && form.periodTo

  return (
    <div className="w-full">
      <div className="flex items-start justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 mb-1">Payments</h1>
          <p className="text-sm text-gray-500">Rent &amp; fee ledger.</p>
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
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Type</th>
              <th className="text-right px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Amount (KES)</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Period</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Reference</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Payment date</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Status</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr><td colSpan={6} className="px-5 py-8 text-center text-sm text-gray-400">Loading...</td></tr>
            ) : isError ? (
              <tr><td colSpan={6} className="px-5 py-8 text-center text-sm text-red-500">Failed to load data.</td></tr>
            ) : payments.length === 0 ? (
              <tr><td colSpan={6} className="px-5 py-8 text-center text-sm text-gray-400">No payments logged yet.</td></tr>
            ) : (
              payments.filter((p) => !p.voided).map((p) => (
                <tr key={p.id} className="border-b border-gray-50 hover:bg-gray-50">
                  <td className="px-5 py-3 text-gray-700 font-medium">{p.paymentType}</td>
                  <td className="px-5 py-3 text-right text-gray-900">{Number(p.amount).toLocaleString()}</td>
                  <td className="px-5 py-3 text-gray-500 text-xs whitespace-nowrap">{p.periodFrom} → {p.periodTo}</td>
                  <td className="px-5 py-3 text-gray-400 font-mono text-xs">{p.referenceNo || '—'}</td>
                  <td className="px-5 py-3 text-gray-500">{p.paymentDate ?? '—'}</td>
                  <td className="px-5 py-3">
                    <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${statusColors[p.status] ?? 'bg-gray-100 text-gray-500'}`}>
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
          <div className="bg-white rounded-2xl w-full max-w-lg shadow-xl flex flex-col max-h-[90vh]">
            <div className="flex items-center justify-between px-8 py-6 border-b border-gray-100 shrink-0">
              <h2 className="text-lg font-bold text-gray-900">Log payment</h2>
              <button onClick={() => setOpen(false)} className="text-gray-400 hover:text-gray-600"><X size={18} /></button>
            </div>

            <form onSubmit={handleSave} className="overflow-y-auto">
              <div className="px-8 py-6 space-y-4">

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">Lease <span className="text-red-400">*</span></label>
                  <select name="leaseId" value={form.leaseId} onChange={handleChange}
                    className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 bg-white">
                    <option value="">Select lease</option>
                    {leases.map((l) => (
                      <option key={l.id} value={l.id}>
                        Lease {l.id.slice(0, 8)}… · KES {Number(l.rentAmount).toLocaleString()}/mo
                      </option>
                    ))}
                  </select>
                  {leases.length === 0 && (
                    <p className="text-xs text-amber-600 mt-1">No active leases found. Add a building, unit and lease first.</p>
                  )}
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1.5">Payment type <span className="text-red-400">*</span></label>
                    <select name="paymentType" value={form.paymentType} onChange={handleChange}
                      className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 bg-white">
                      {PAYMENT_TYPES.map((t) => <option key={t}>{t}</option>)}
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1.5">Status</label>
                    <select name="status" value={form.status} onChange={handleChange}
                      className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 bg-white">
                      {PAYMENT_STATUSES.map((s) => <option key={s}>{s}</option>)}
                    </select>
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">Amount (KES) <span className="text-red-400">*</span></label>
                  <input name="amount" type="number" min={0} value={form.amount} onChange={handleChange}
                    className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400" />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1.5">Period from <span className="text-red-400">*</span></label>
                    <input name="periodFrom" type="date" value={form.periodFrom} onChange={handleChange}
                      className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1.5">Period to <span className="text-red-400">*</span></label>
                    <input name="periodTo" type="date" value={form.periodTo} onChange={handleChange}
                      min={form.periodFrom || undefined}
                      className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400" />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1.5">Payment date</label>
                    <input name="paymentDate" type="date" value={form.paymentDate} onChange={handleChange}
                      className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1.5">Reference no.</label>
                    <input name="referenceNo" type="text" value={form.referenceNo} onChange={handleChange}
                      placeholder="e.g. M-Pesa ref"
                      className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400" />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">Notes</label>
                  <textarea name="notes" value={form.notes} onChange={handleChange} rows={2}
                    className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 resize-none" />
                </div>
              </div>

              <div className="flex justify-end gap-3 px-8 py-5 border-t border-gray-100 shrink-0">
                <button type="button" onClick={() => setOpen(false)}
                  className="text-sm text-gray-500 hover:text-gray-800 px-4 py-2 rounded-md transition-colors">
                  Cancel
                </button>
                <button type="submit" disabled={!canSave || mutation.isPending}
                  className="bg-gray-900 text-white text-sm font-medium px-5 py-2 rounded-md hover:bg-gray-700 transition-colors disabled:opacity-40 disabled:cursor-not-allowed">
                  {mutation.isPending ? 'Saving…' : 'Save payment'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
