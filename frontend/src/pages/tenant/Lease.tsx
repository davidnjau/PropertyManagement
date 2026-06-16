import { useState } from 'react'
import { CheckCircle, X } from 'lucide-react'

const lease = {
  unit: 'Unit 4B',
  building: 'Westlands Heights',
  floor: '4th Floor',
  startDate: '1 Jan 2026',
  endDate: '31 Dec 2026',
  monthlyRent: 'KES 45,000',
  deposit: 'KES 90,000',
  status: 'Active',
}

const DURATION_OPTIONS = [
  { value: '3', label: '3 months' },
  { value: '6', label: '6 months' },
  { value: '12', label: '12 months' },
  { value: '24', label: '24 months' },
  { value: 'custom', label: 'Custom date' },
]

export default function TenantLease() {
  const [showModal, setShowModal] = useState(false)
  const [submitted, setSubmitted] = useState(false)
  const [form, setForm] = useState({ duration: '12', customDate: '', notes: '' })

  function handleChange(e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setShowModal(false)
    setSubmitted(true)
    setTimeout(() => setSubmitted(false), 5000)
  }

  // Calculate proposed end date for display
  function proposedEndDate() {
    if (form.duration === 'custom') return form.customDate || '—'
    const base = new Date('2026-12-31')
    base.setMonth(base.getMonth() + Number(form.duration))
    return base.toLocaleDateString('en-KE', { day: 'numeric', month: 'short', year: 'numeric' })
  }

  return (
    <div className="w-full">
      <div className="flex items-start justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 mb-1">My Lease</h1>
          <p className="text-sm text-gray-500">Your current tenancy agreement details.</p>
        </div>
        <button
          onClick={() => setShowModal(true)}
          className="flex items-center gap-1.5 bg-gray-900 text-white text-sm font-medium px-4 py-2 rounded-md hover:bg-gray-700 transition-colors"
        >
          Extend Lease
        </button>
      </div>

      {/* Success notice */}
      {submitted && (
        <div className="flex items-center gap-3 bg-green-50 border border-green-100 rounded-xl px-5 py-4 mb-6">
          <CheckCircle size={16} className="text-green-600 shrink-0" />
          <p className="text-sm text-green-800 font-medium">
            Extension request submitted. Your agent will review and get back to you shortly.
          </p>
        </div>
      )}

      {/* Lease details card */}
      <div className="border border-gray-100 rounded-2xl overflow-hidden mb-4">
        <div className="border-b border-gray-100 px-6 py-4 flex items-center justify-between bg-gray-50">
          <p className="text-sm font-semibold text-gray-900">{lease.unit} — {lease.building}</p>
          <span className="text-xs font-semibold bg-green-50 text-green-700 px-2.5 py-1 rounded-full">
            {lease.status}
          </span>
        </div>

        <dl className="divide-y divide-gray-50">
          {[
            { label: 'Building', value: lease.building },
            { label: 'Unit', value: lease.unit },
            { label: 'Floor', value: lease.floor },
            { label: 'Lease start', value: lease.startDate },
            { label: 'Lease end', value: lease.endDate },
            { label: 'Monthly rent', value: lease.monthlyRent },
            { label: 'Security deposit', value: lease.deposit },
          ].map(({ label, value }) => (
            <div key={label} className="flex items-center justify-between px-6 py-3.5">
              <dt className="text-sm text-gray-500">{label}</dt>
              <dd className="text-sm font-medium text-gray-900">{value}</dd>
            </div>
          ))}
        </dl>
      </div>

      <p className="text-xs text-gray-400">
        To request a lease amendment, use the <span className="font-medium">Extend Lease</span> button above.
      </p>

      {/* Extend Lease Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl w-full max-w-md shadow-xl">
            <div className="flex items-center justify-between px-7 pt-7 pb-5 border-b border-gray-100">
              <div>
                <h2 className="text-lg font-bold text-gray-900">Extend Lease</h2>
                <p className="text-xs text-gray-500 mt-0.5">Current end date: <span className="font-medium text-gray-700">{lease.endDate}</span></p>
              </div>
              <button onClick={() => setShowModal(false)} className="text-gray-400 hover:text-gray-600">
                <X size={18} />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="px-7 py-6 space-y-5">
              {/* Duration */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Extension duration</label>
                <div className="grid grid-cols-2 gap-2">
                  {DURATION_OPTIONS.map((opt) => (
                    <button
                      key={opt.value}
                      type="button"
                      onClick={() => setForm({ ...form, duration: opt.value, customDate: '' })}
                      className={`py-2.5 px-3 rounded-md border text-sm font-medium transition-colors text-left ${
                        form.duration === opt.value
                          ? 'border-gray-900 bg-gray-900 text-white'
                          : 'border-gray-200 text-gray-700 hover:border-gray-400'
                      }`}
                    >
                      {opt.label}
                    </button>
                  ))}
                </div>
              </div>

              {/* Custom date picker */}
              {form.duration === 'custom' && (
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">New end date</label>
                  <input
                    name="customDate"
                    type="date"
                    value={form.customDate}
                    onChange={handleChange}
                    min="2027-01-01"
                    className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400"
                  />
                </div>
              )}

              {/* Proposed end date preview */}
              {(form.duration !== 'custom' || form.customDate) && (
                <div className="flex items-center justify-between bg-gray-50 border border-gray-100 rounded-md px-4 py-3">
                  <span className="text-xs text-gray-500 font-medium">Proposed new end date</span>
                  <span className="text-sm font-bold text-gray-900">{proposedEndDate()}</span>
                </div>
              )}

              {/* Notes */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">
                  Notes <span className="text-gray-400 font-normal">(optional)</span>
                </label>
                <textarea
                  name="notes"
                  value={form.notes}
                  onChange={handleChange}
                  rows={3}
                  placeholder="Any specific requests or conditions for the extension…"
                  className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 resize-none placeholder:text-gray-300"
                />
              </div>

              <div className="flex gap-3 pt-1">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="flex-1 border border-gray-200 text-gray-700 text-sm font-medium py-2.5 rounded-md hover:border-gray-400 transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={form.duration === 'custom' && !form.customDate}
                  className="flex-1 bg-gray-900 text-white text-sm font-semibold py-2.5 rounded-md hover:bg-gray-700 transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
                >
                  Submit request
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
