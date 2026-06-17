import { useState } from 'react'
import { X } from 'lucide-react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { fetchMaintenance, createMaintenanceRequest } from '../../services/maintenance'

export default function Maintenance() {
  const qc = useQueryClient()
  const { data: requests = [], isLoading, isError } = useQuery({
    queryKey: ['maintenance'],
    queryFn: fetchMaintenance,
  })

  const mutation = useMutation({
    mutationFn: createMaintenanceRequest,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['maintenance'] }),
  })

  const [open, setOpen] = useState(false)
  const [form, setForm] = useState({ title: '', description: '', priority: 'MEDIUM', category: 'GENERAL', unitId: '' })

  function handleChange(e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  function handleSave(e: React.FormEvent) {
    e.preventDefault()
    mutation.mutate(form, {
      onSuccess: () => {
        setForm({ title: '', description: '', priority: 'MEDIUM', category: 'GENERAL', unitId: '' })
        setOpen(false)
      },
    })
  }

  const priorityColors: Record<string, string> = {
    LOW: 'bg-gray-100 text-gray-600',
    MEDIUM: 'bg-amber-50 text-amber-700',
    HIGH: 'bg-red-50 text-red-700',
  }

  return (
    <div className="w-full">
      <div className="flex items-start justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 mb-1">Maintenance</h1>
          <p className="text-sm text-gray-500">Work orders & repairs.</p>
        </div>
        <button
          onClick={() => setOpen(true)}
          className="flex items-center gap-1.5 bg-gray-900 text-white text-sm font-medium px-4 py-2 rounded-md hover:bg-gray-700 transition-colors"
        >
          + New request
        </button>
      </div>

      <div className="border border-gray-100 rounded-xl overflow-hidden">
        <div className="border-b border-gray-100 px-5 py-3">
          <p className="text-sm font-semibold text-gray-900">All requests</p>
        </div>
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-100 bg-gray-50">
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Title</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Building</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Priority</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr>
                <td colSpan={3} className="px-5 py-8 text-center">
                  <p className="text-sm text-gray-400">Loading...</p>
                </td>
              </tr>
            ) : isError ? (
              <tr>
                <td colSpan={3} className="px-5 py-8 text-center">
                  <p className="text-sm text-red-500">Failed to load data.</p>
                </td>
              </tr>
            ) : requests.length === 0 ? (
              <tr>
                <td colSpan={3} className="px-5 py-8 text-center text-sm text-gray-400">No requests yet.</td>
              </tr>
            ) : (
              requests.map((r) => (
                <tr key={r.id} className="border-b border-gray-50 hover:bg-gray-50">
                  <td className="px-5 py-3 font-medium text-gray-900">{r.title}</td>
                  <td className="px-5 py-3 text-gray-500">{r.unitId || '—'}</td>
                  <td className="px-5 py-3">
                    <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${priorityColors[r.priority] ?? ''}`}>
                      {r.priority}
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
          <div className="bg-white rounded-2xl w-full max-w-md p-8 shadow-xl">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-lg font-bold text-gray-900">New maintenance request</h2>
              <button onClick={() => setOpen(false)} className="text-gray-400 hover:text-gray-600">
                <X size={18} />
              </button>
            </div>
            <form onSubmit={handleSave} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Title</label>
                <input name="title" value={form.title} onChange={handleChange} autoFocus
                  className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Description</label>
                <textarea name="description" rows={3} value={form.description} onChange={handleChange}
                  className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 resize-none" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Category</label>
                <select name="category" value={form.category} onChange={handleChange}
                  className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 bg-white">
                  <option value="GENERAL">General</option>
                  <option value="PLUMBING">Plumbing</option>
                  <option value="ELECTRICAL">Electrical</option>
                  <option value="STRUCTURAL">Structural</option>
                  <option value="APPLIANCE">Appliance</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Priority</label>
                <select name="priority" value={form.priority} onChange={handleChange}
                  className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 bg-white">
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HIGH">High</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Unit ID</label>
                <input name="unitId" value={form.unitId} onChange={handleChange}
                  placeholder="e.g. unit-uuid"
                  className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400" />
              </div>
              <div className="flex justify-end pt-2">
                <button type="submit" disabled={mutation.isPending}
                  className="bg-gray-200 text-gray-700 text-sm font-medium px-5 py-2 rounded-md hover:bg-gray-300 transition-colors disabled:opacity-50">
                  {mutation.isPending ? 'Saving…' : 'Save'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
