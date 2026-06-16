import { useState } from 'react'
import { X } from 'lucide-react'

type Building = { id: number; name: string; address: string; city: string; units: number }

export default function Buildings() {
  const [buildings, setBuildings] = useState<Building[]>([])
  const [open, setOpen] = useState(false)
  const [form, setForm] = useState({ name: '', address: '', city: '', units: 0 })

  function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    const value = e.target.name === 'units' ? Number(e.target.value) : e.target.value
    setForm({ ...form, [e.target.name]: value })
  }

  function handleSave(e: React.FormEvent) {
    e.preventDefault()
    setBuildings([...buildings, { ...form, id: Date.now() }])
    setForm({ name: '', address: '', city: '', units: 0 })
    setOpen(false)
  }

  return (
    <div className="max-w-5xl">
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

      {/* Table */}
      <div className="border border-gray-100 rounded-xl overflow-hidden">
        <div className="border-b border-gray-100 px-5 py-3">
          <p className="text-sm font-semibold text-gray-900">All buildings</p>
        </div>
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-100 bg-gray-50">
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Name</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">City</th>
              <th className="text-right px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Units</th>
            </tr>
          </thead>
          <tbody>
            {buildings.length === 0 ? (
              <tr>
                <td colSpan={3} className="px-5 py-8 text-center text-sm text-gray-400">No buildings yet.</td>
              </tr>
            ) : (
              buildings.map((b) => (
                <tr key={b.id} className="border-b border-gray-50 hover:bg-gray-50">
                  <td className="px-5 py-3 font-medium text-gray-900">{b.name}</td>
                  <td className="px-5 py-3 text-gray-500">{b.city}</td>
                  <td className="px-5 py-3 text-right text-gray-900">{b.units}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Modal */}
      {open && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl w-full max-w-md p-8 shadow-xl">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-lg font-bold text-gray-900">New building</h2>
              <button onClick={() => setOpen(false)} className="text-gray-400 hover:text-gray-600">
                <X size={18} />
              </button>
            </div>
            <form onSubmit={handleSave} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Name</label>
                <input name="name" value={form.name} onChange={handleChange} autoFocus
                  className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Address</label>
                <input name="address" value={form.address} onChange={handleChange}
                  className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">City</label>
                <input name="city" value={form.city} onChange={handleChange}
                  className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Units</label>
                <input name="units" type="number" value={form.units} onChange={handleChange}
                  className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400" />
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
      )}
    </div>
  )
}
