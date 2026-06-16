import { useState } from 'react'
import { CheckCircle } from 'lucide-react'

type Request = { id: number; title: string; description: string; priority: string; date: string; status: string }

const emptyForm = { title: '', description: '', priority: 'Medium' }

const statusColors: Record<string, string> = {
  Open: 'bg-amber-50 text-amber-700',
  'In Progress': 'bg-blue-50 text-blue-700',
  Resolved: 'bg-green-50 text-green-700',
}

export default function TenantMaintenance() {
  const [requests, setRequests] = useState<Request[]>([])
  const [form, setForm] = useState(emptyForm)
  const [submitted, setSubmitted] = useState(false)

  function handleChange(e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setRequests([
      {
        ...form,
        id: Date.now(),
        date: new Date().toLocaleDateString('en-KE'),
        status: 'Open',
      },
      ...requests,
    ])
    setForm(emptyForm)
    setSubmitted(true)
    setTimeout(() => setSubmitted(false), 4000)
  }

  return (
    <div className="w-full">
      <h1 className="text-2xl font-bold text-gray-900 mb-1">Maintenance</h1>
      <p className="text-sm text-gray-500 mb-8">Report an issue and track its progress.</p>

      {submitted && (
        <div className="flex items-center gap-3 bg-green-50 border border-green-100 rounded-xl px-5 py-4 mb-6">
          <CheckCircle size={16} className="text-green-600 shrink-0" />
          <p className="text-sm text-green-800 font-medium">Request submitted. Your agent will be in touch shortly.</p>
        </div>
      )}

      {/* Submit form */}
      <div className="border border-gray-100 rounded-2xl p-7 mb-8">
        <p className="text-sm font-semibold text-gray-900 mb-4">New request</p>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Title</label>
            <input name="title" value={form.title} onChange={handleChange} required
              placeholder="e.g. Leaking tap in kitchen"
              className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 placeholder:text-gray-300" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Description</label>
            <textarea name="description" rows={3} value={form.description} onChange={handleChange}
              placeholder="Describe the issue in detail…"
              className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 resize-none placeholder:text-gray-300" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Priority</label>
            <select name="priority" value={form.priority} onChange={handleChange}
              className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 bg-white">
              <option>Low</option>
              <option>Medium</option>
              <option>High</option>
            </select>
          </div>
          <button type="submit"
            className="bg-gray-900 text-white text-sm font-semibold px-5 py-2.5 rounded-md hover:bg-gray-700 transition-colors">
            Submit request
          </button>
        </form>
      </div>

      {/* Request history */}
      {requests.length > 0 && (
        <div className="border border-gray-100 rounded-xl overflow-hidden">
          <div className="border-b border-gray-100 px-5 py-3">
            <p className="text-sm font-semibold text-gray-900">My requests</p>
          </div>
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-gray-100 bg-gray-50">
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Title</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Priority</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Date</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Status</th>
              </tr>
            </thead>
            <tbody>
              {requests.map((r) => (
                <tr key={r.id} className="border-b border-gray-50 hover:bg-gray-50">
                  <td className="px-5 py-3 font-medium text-gray-900">{r.title}</td>
                  <td className="px-5 py-3 text-gray-500">{r.priority}</td>
                  <td className="px-5 py-3 text-gray-500">{r.date}</td>
                  <td className="px-5 py-3">
                    <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${statusColors[r.status] ?? ''}`}>
                      {r.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
