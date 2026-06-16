import { useState } from 'react'
import { X } from 'lucide-react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { fetchTenants, createTenant } from '../../services/tenants'

const EMPTY_FORM = {
  fullName: '', email: '', phone: '', unit: '', building: '',
  monthlyRent: '', deposit: '',
  leaseStart: '', leaseEnd: '',
}

function leaseStatus(endDate: string): { label: string; color: string } {
  if (!endDate) return { label: 'No lease', color: 'bg-gray-100 text-gray-500' }
  const diff = Math.ceil((new Date(endDate).getTime() - Date.now()) / (1000 * 60 * 60 * 24))
  if (diff < 0) return { label: 'Expired', color: 'bg-red-50 text-red-600' }
  if (diff <= 30) return { label: 'Expiring soon', color: 'bg-amber-50 text-amber-600' }
  return { label: 'Active', color: 'bg-green-50 text-green-700' }
}

export default function Tenants() {
  const qc = useQueryClient()
  const { data: tenants = [], isLoading, isError } = useQuery({
    queryKey: ['tenants'],
    queryFn: fetchTenants,
  })

  const mutation = useMutation({
    mutationFn: createTenant,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['tenants'] }),
  })

  const [open, setOpen] = useState(false)
  const [form, setForm] = useState(EMPTY_FORM)

  function handleChange(e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  function handleSave(e: React.FormEvent) {
    e.preventDefault()
    mutation.mutate(
      {
        ...form,
        monthlyRent: Number(form.monthlyRent),
        deposit: Number(form.deposit),
      },
      {
        onSuccess: () => {
          setForm(EMPTY_FORM)
          setOpen(false)
        },
      },
    )
  }

  const canSave = form.fullName && form.email && form.unit && form.monthlyRent && form.leaseStart && form.leaseEnd

  return (
    <div className="w-full">
      <div className="flex items-start justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 mb-1">Tenants</h1>
          <p className="text-sm text-gray-500">Leases & contact info.</p>
        </div>
        <button
          onClick={() => setOpen(true)}
          className="flex items-center gap-1.5 bg-gray-900 text-white text-sm font-medium px-4 py-2 rounded-md hover:bg-gray-700 transition-colors"
        >
          + Add tenant
        </button>
      </div>

      <div className="border border-gray-100 rounded-xl overflow-hidden">
        <div className="border-b border-gray-100 px-5 py-3">
          <p className="text-sm font-semibold text-gray-900">All tenants</p>
        </div>
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-100 bg-gray-50">
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Name</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Email</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Unit</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Lease period</th>
              <th className="text-right px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Rent</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Status</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr>
                <td colSpan={6} className="px-5 py-8 text-center">
                  <p className="text-sm text-gray-400">Loading...</p>
                </td>
              </tr>
            ) : isError ? (
              <tr>
                <td colSpan={6} className="px-5 py-8 text-center">
                  <p className="text-sm text-red-500">Failed to load data.</p>
                </td>
              </tr>
            ) : tenants.length === 0 ? (
              <tr>
                <td colSpan={6} className="px-5 py-8 text-center text-sm text-gray-400">No tenants yet.</td>
              </tr>
            ) : (
              tenants.map((t) => {
                const status = leaseStatus(t.leaseEnd)
                return (
                  <tr key={t.id} className="border-b border-gray-50 hover:bg-gray-50">
                    <td className="px-5 py-3">
                      <p className="font-medium text-gray-900">{t.fullName}</p>
                      {t.building && <p className="text-xs text-gray-400 mt-0.5">{t.building}</p>}
                    </td>
                    <td className="px-5 py-3 text-gray-500">{t.email}</td>
                    <td className="px-5 py-3 text-gray-500">{t.unit}</td>
                    <td className="px-5 py-3 text-gray-500 text-xs whitespace-nowrap">
                      {t.leaseStart} → {t.leaseEnd}
                    </td>
                    <td className="px-5 py-3 text-right text-gray-900">
                      <p>KES {Number(t.monthlyRent).toLocaleString()}</p>
                      {t.deposit > 0 && (
                        <p className="text-xs text-gray-400 font-normal mt-0.5">
                          Dep: KES {Number(t.deposit).toLocaleString()}
                        </p>
                      )}
                    </td>
                    <td className="px-5 py-3">
                      <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${status.color}`}>
                        {status.label}
                      </span>
                    </td>
                  </tr>
                )
              })
            )}
          </tbody>
        </table>
      </div>

      {open && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl w-full max-w-lg shadow-xl flex flex-col max-h-[90vh]">
            {/* Header */}
            <div className="flex items-center justify-between px-8 py-6 border-b border-gray-100 shrink-0">
              <h2 className="text-lg font-bold text-gray-900">New tenant</h2>
              <button onClick={() => setOpen(false)} className="text-gray-400 hover:text-gray-600">
                <X size={18} />
              </button>
            </div>

            <form onSubmit={handleSave} className="overflow-y-auto">
              <div className="px-8 py-6 space-y-6">

                {/* Tenant details */}
                <div>
                  <p className="text-xs font-semibold uppercase tracking-widest text-gray-400 mb-4">Tenant details</p>
                  <div className="space-y-4">
                    <div className="grid grid-cols-2 gap-4">
                      <div className="col-span-2">
                        <label className="block text-sm font-medium text-gray-700 mb-1.5">Full name <span className="text-red-400">*</span></label>
                        <input
                          name="fullName" type="text" value={form.fullName} onChange={handleChange} autoFocus
                          className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400"
                        />
                      </div>
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1.5">Email <span className="text-red-400">*</span></label>
                        <input
                          name="email" type="email" value={form.email} onChange={handleChange}
                          className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400"
                        />
                      </div>
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1.5">Phone</label>
                        <input
                          name="phone" type="tel" value={form.phone} onChange={handleChange}
                          className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400"
                        />
                      </div>
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1.5">Building</label>
                        <select
                          name="building" value={form.building} onChange={handleChange}
                          className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 bg-white"
                        >
                          <option value="">Choose building</option>
                        </select>
                      </div>
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1.5">Unit <span className="text-red-400">*</span></label>
                        <input
                          name="unit" type="text" value={form.unit} onChange={handleChange}
                          className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400"
                        />
                      </div>
                    </div>
                  </div>
                </div>

                <hr className="border-gray-100" />

                {/* Lease details */}
                <div>
                  <p className="text-xs font-semibold uppercase tracking-widest text-gray-400 mb-4">Lease details</p>
                  <div className="space-y-4">
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1.5">Lease start <span className="text-red-400">*</span></label>
                        <input
                          name="leaseStart" type="date" value={form.leaseStart} onChange={handleChange}
                          className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400"
                        />
                      </div>
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1.5">Lease end <span className="text-red-400">*</span></label>
                        <input
                          name="leaseEnd" type="date" value={form.leaseEnd} onChange={handleChange}
                          min={form.leaseStart || undefined}
                          className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400"
                        />
                      </div>
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1.5">Monthly rent (KES) <span className="text-red-400">*</span></label>
                        <input
                          name="monthlyRent" type="number" min={0} value={form.monthlyRent} onChange={handleChange}
                          className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400"
                        />
                      </div>
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1.5">Deposit (KES)</label>
                        <input
                          name="deposit" type="number" min={0} value={form.deposit} onChange={handleChange}
                          className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400"
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              {/* Footer */}
              <div className="flex justify-end gap-3 px-8 py-5 border-t border-gray-100 shrink-0">
                <button
                  type="button"
                  onClick={() => setOpen(false)}
                  className="text-sm text-gray-500 hover:text-gray-800 px-4 py-2 rounded-md transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={!canSave || mutation.isPending}
                  className="bg-gray-900 text-white text-sm font-medium px-5 py-2 rounded-md hover:bg-gray-700 transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
                >
                  {mutation.isPending ? 'Saving…' : 'Save tenant'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
