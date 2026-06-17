import { useState } from 'react'
import { X } from 'lucide-react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { fetchBuildings, createBuilding } from '../../services/buildings'

const EMPTY_FORM = { name: '', address: '', suburb: '', state: '', postcode: '', country: 'Kenya', notes: '' }

export default function Buildings() {
  const qc = useQueryClient()
  const { data: buildings = [], isLoading, isError } = useQuery({
    queryKey: ['buildings'],
    queryFn: fetchBuildings,
  })

  const mutation = useMutation({
    mutationFn: createBuilding,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['buildings'] }),
  })

  const [open, setOpen] = useState(false)
  const [form, setForm] = useState(EMPTY_FORM)

  function handleChange(e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  function handleSave(e: React.FormEvent) {
    e.preventDefault()
    mutation.mutate(form, {
      onSuccess: () => {
        setForm(EMPTY_FORM)
        setOpen(false)
      },
    })
  }

  const canSave = form.address && form.suburb && form.state && form.postcode

  return (
    <div className="w-full">
      <div className="flex items-start justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 mb-1">Buildings</h1>
          <p className="text-sm text-gray-500">Your managed properties.</p>
        </div>
        <button
          onClick={() => setOpen(true)}
          className="flex items-center gap-1.5 bg-gray-900 text-white text-sm font-medium px-4 py-2 rounded-md hover:bg-gray-700 transition-colors"
        >
          + Add building
        </button>
      </div>

      <div className="border border-gray-100 rounded-xl overflow-hidden">
        <div className="border-b border-gray-100 px-5 py-3">
          <p className="text-sm font-semibold text-gray-900">All buildings</p>
        </div>
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-100 bg-gray-50">
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Name</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Address</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Suburb / State</th>
              <th className="text-right px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Units</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr><td colSpan={4} className="px-5 py-8 text-center text-sm text-gray-400">Loading...</td></tr>
            ) : isError ? (
              <tr><td colSpan={4} className="px-5 py-8 text-center text-sm text-red-500">Failed to load data.</td></tr>
            ) : buildings.length === 0 ? (
              <tr><td colSpan={4} className="px-5 py-8 text-center text-sm text-gray-400">No buildings yet.</td></tr>
            ) : (
              buildings.map((b) => (
                <tr key={b.id} className="border-b border-gray-50 hover:bg-gray-50">
                  <td className="px-5 py-3 font-medium text-gray-900">{b.name ?? '—'}</td>
                  <td className="px-5 py-3 text-gray-500">{b.address}</td>
                  <td className="px-5 py-3 text-gray-500">{b.suburb}, {b.state} {b.postcode}</td>
                  <td className="px-5 py-3 text-right text-gray-900">{b.unitCount}</td>
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
              <h2 className="text-lg font-bold text-gray-900">New building</h2>
              <button onClick={() => setOpen(false)} className="text-gray-400 hover:text-gray-600"><X size={18} /></button>
            </div>

            <form onSubmit={handleSave} className="overflow-y-auto">
              <div className="px-8 py-6 space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">Building name</label>
                  <input name="name" value={form.name} onChange={handleChange} autoFocus
                    className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">Address <span className="text-red-400">*</span></label>
                  <input name="address" value={form.address} onChange={handleChange}
                    className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400" />
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1.5">Suburb / Area <span className="text-red-400">*</span></label>
                    <input name="suburb" value={form.suburb} onChange={handleChange}
                      className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1.5">County / State <span className="text-red-400">*</span></label>
                    <input name="state" value={form.state} onChange={handleChange}
                      className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1.5">Postcode <span className="text-red-400">*</span></label>
                    <input name="postcode" value={form.postcode} onChange={handleChange}
                      className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1.5">Country</label>
                    <input name="country" value={form.country} onChange={handleChange}
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
                  {mutation.isPending ? 'Saving…' : 'Save building'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
