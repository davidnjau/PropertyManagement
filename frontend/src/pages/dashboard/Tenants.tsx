import { useState } from 'react'
import { X } from 'lucide-react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { fetchTenants, createTenant } from '../../services/tenants'

const EMPTY_FORM = {
  fullName: '', email: '', phone: '',
  emergencyContactName: '', emergencyContactPhone: '',
  notes: '',
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

  function handleChange(e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  function handleSave(e: React.FormEvent) {
    e.preventDefault()
    mutation.mutate(
      {
        fullName: form.fullName,
        email: form.email,
        phone: form.phone || undefined,
        emergencyContactName: form.emergencyContactName || undefined,
        emergencyContactPhone: form.emergencyContactPhone || undefined,
        notes: form.notes || undefined,
      },
      {
        onSuccess: () => {
          setForm(EMPTY_FORM)
          setOpen(false)
        },
      },
    )
  }

  const canSave = form.fullName && form.email

  return (
    <div className="w-full">
      <div className="flex items-start justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 mb-1">Tenants</h1>
          <p className="text-sm text-gray-500">Tenant contact records.</p>
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
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Phone</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Emergency contact</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Status</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr><td colSpan={5} className="px-5 py-8 text-center text-sm text-gray-400">Loading...</td></tr>
            ) : isError ? (
              <tr><td colSpan={5} className="px-5 py-8 text-center text-sm text-red-500">Failed to load data.</td></tr>
            ) : tenants.length === 0 ? (
              <tr><td colSpan={5} className="px-5 py-8 text-center text-sm text-gray-400">No tenants yet.</td></tr>
            ) : (
              tenants.map((t) => (
                <tr key={t.id} className="border-b border-gray-50 hover:bg-gray-50">
                  <td className="px-5 py-3 font-medium text-gray-900">{t.fullName}</td>
                  <td className="px-5 py-3 text-gray-500">{t.email}</td>
                  <td className="px-5 py-3 text-gray-500">{t.phone ?? '—'}</td>
                  <td className="px-5 py-3 text-gray-500">
                    {t.emergencyContactName
                      ? <span>{t.emergencyContactName}{t.emergencyContactPhone ? ` · ${t.emergencyContactPhone}` : ''}</span>
                      : '—'}
                  </td>
                  <td className="px-5 py-3">
                    <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${t.isActive ? 'bg-green-50 text-green-700' : 'bg-red-50 text-red-500'}`}>
                      {t.isActive ? 'Active' : 'Inactive'}
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
              <h2 className="text-lg font-bold text-gray-900">New tenant</h2>
              <button onClick={() => setOpen(false)} className="text-gray-400 hover:text-gray-600"><X size={18} /></button>
            </div>

            <form onSubmit={handleSave} className="overflow-y-auto">
              <div className="px-8 py-6 space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">Full name <span className="text-red-400">*</span></label>
                  <input name="fullName" type="text" value={form.fullName} onChange={handleChange} autoFocus
                    className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400" />
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1.5">Email <span className="text-red-400">*</span></label>
                    <input name="email" type="email" value={form.email} onChange={handleChange}
                      className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1.5">Phone</label>
                    <input name="phone" type="tel" value={form.phone} onChange={handleChange}
                      className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400" />
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1.5">Emergency contact name</label>
                    <input name="emergencyContactName" type="text" value={form.emergencyContactName} onChange={handleChange}
                      className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1.5">Emergency contact phone</label>
                    <input name="emergencyContactPhone" type="tel" value={form.emergencyContactPhone} onChange={handleChange}
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
