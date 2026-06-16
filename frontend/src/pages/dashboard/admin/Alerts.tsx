import { useState } from 'react'
import { Send, Bell, CheckCircle } from 'lucide-react'

type TargetType = 'all' | 'building' | 'tenant' | 'rent_due'
type Channel = 'in_app' | 'email' | 'sms'
type RentDueFilter = 'upcoming' | 'overdue' | 'both'

type SentAlert = {
  id: number
  sentAt: string
  target: string
  channels: string
  subject: string
  message: string
  recipients: number
  status: 'Sent' | 'Failed'
}

// Mock data — will come from API
const MOCK_BUILDINGS = ['Westlands Heights', 'Kilimani Court', 'Lavington Gardens', 'Karen View Estate']
const MOCK_TENANTS = [
  'Alice Mwangi', 'Brian Otieno', 'Carol Wanjiku', 'David Kamau',
  'Esther Njeri', 'Francis Oloo', 'Grace Akinyi', 'Hassan Omar',
]

const TARGET_OPTIONS: { id: TargetType; label: string; desc: string }[] = [
  { id: 'all', label: 'All', desc: 'Every tenant and user on the platform' },
  { id: 'building', label: 'By Building', desc: 'All tenants in a specific building' },
  { id: 'tenant', label: 'Specific Tenants', desc: 'Select one or more tenants by name' },
  { id: 'rent_due', label: 'Rent Due', desc: 'Tenants with upcoming or overdue rent' },
]

const CHANNEL_OPTIONS: { id: Channel; label: string }[] = [
  { id: 'in_app', label: 'In-app' },
  { id: 'email', label: 'Email' },
  { id: 'sms', label: 'SMS' },
]

const statusColors: Record<string, string> = {
  Sent: 'bg-green-50 text-green-700',
  Failed: 'bg-red-50 text-red-700',
}

export default function Alerts() {
  const [target, setTarget] = useState<TargetType>('all')
  const [building, setBuilding] = useState('')
  const [selectedTenants, setSelectedTenants] = useState<string[]>([])
  const [rentDueFilter, setRentDueFilter] = useState<RentDueFilter>('both')
  const [channels, setChannels] = useState<Channel[]>(['in_app'])
  const [subject, setSubject] = useState('')
  const [message, setMessage] = useState('')
  const [sent, setSent] = useState(false)
  const [history, setHistory] = useState<SentAlert[]>([])

  function toggleChannel(id: Channel) {
    setChannels((prev) =>
      prev.includes(id) ? prev.filter((c) => c !== id) : [...prev, id]
    )
  }

  function toggleTenant(name: string) {
    setSelectedTenants((prev) =>
      prev.includes(name) ? prev.filter((t) => t !== name) : [...prev, name]
    )
  }

  function targetLabel() {
    if (target === 'all') return 'All users'
    if (target === 'building') return building || 'Building'
    if (target === 'tenant') return selectedTenants.length ? selectedTenants.join(', ') : 'Specific tenants'
    if (target === 'rent_due') return `Rent due (${rentDueFilter})`
    return ''
  }

  function estimatedRecipients() {
    if (target === 'all') return MOCK_TENANTS.length
    if (target === 'building') return building ? 4 : 0
    if (target === 'tenant') return selectedTenants.length
    if (target === 'rent_due') return rentDueFilter === 'both' ? 5 : 3
    return 0
  }

  function handleSend(e: React.FormEvent) {
    e.preventDefault()
    const now = new Date().toLocaleString('en-KE', {
      day: 'numeric', month: 'short', year: 'numeric',
      hour: '2-digit', minute: '2-digit',
    })
    setHistory((prev) => [
      {
        id: Date.now(),
        sentAt: now,
        target: targetLabel(),
        channels: channels.map((c) => CHANNEL_OPTIONS.find((o) => o.id === c)?.label ?? c).join(', '),
        subject,
        message,
        recipients: estimatedRecipients(),
        status: 'Sent',
      },
      ...prev,
    ])
    setSubject('')
    setMessage('')
    setSelectedTenants([])
    setBuilding('')
    setSent(true)
    setTimeout(() => setSent(false), 4000)
  }

  const canSend =
    channels.length > 0 &&
    subject.trim() &&
    message.trim() &&
    (target === 'all' ||
      (target === 'building' && building) ||
      (target === 'tenant' && selectedTenants.length > 0) ||
      target === 'rent_due')

  return (
    <div className="w-full">
      <div className="flex items-start justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 mb-1">Alerts</h1>
          <p className="text-sm text-gray-500">Send notifications to tenants by building, individual, or payment status.</p>
        </div>
        <div className="flex items-center gap-2 bg-gray-50 border border-gray-100 rounded-lg px-3 py-2">
          <Bell size={14} className="text-gray-400" />
          <span className="text-xs text-gray-500">{history.length} sent</span>
        </div>
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-5 gap-6">

        {/* ── Compose panel ── */}
        <div className="xl:col-span-2 border border-gray-100 rounded-2xl overflow-hidden">
          <div className="border-b border-gray-100 px-6 py-4 bg-gray-50">
            <p className="text-sm font-semibold text-gray-900">Compose alert</p>
          </div>

          <form onSubmit={handleSend} className="p-6 space-y-6">

            {/* Target */}
            <div>
              <label className="block text-xs font-semibold uppercase tracking-widest text-gray-400 mb-3">
                Send to
              </label>
              <div className="grid grid-cols-2 gap-2">
                {TARGET_OPTIONS.map((opt) => (
                  <button
                    key={opt.id}
                    type="button"
                    onClick={() => { setTarget(opt.id); setBuilding(''); setSelectedTenants([]) }}
                    className={`text-left px-3 py-2.5 rounded-lg border text-sm transition-colors ${
                      target === opt.id
                        ? 'border-gray-900 bg-gray-900 text-white'
                        : 'border-gray-200 text-gray-700 hover:border-gray-400'
                    }`}
                  >
                    <p className="font-medium">{opt.label}</p>
                    <p className={`text-xs mt-0.5 leading-tight ${target === opt.id ? 'text-gray-400' : 'text-gray-400'}`}>
                      {opt.desc}
                    </p>
                  </button>
                ))}
              </div>
            </div>

            {/* Building selector */}
            {target === 'building' && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Building</label>
                <select
                  value={building}
                  onChange={(e) => setBuilding(e.target.value)}
                  className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 bg-white"
                >
                  <option value="">Select building</option>
                  {MOCK_BUILDINGS.map((b) => <option key={b}>{b}</option>)}
                </select>
              </div>
            )}

            {/* Tenant multi-select */}
            {target === 'tenant' && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Tenants
                  {selectedTenants.length > 0 && (
                    <span className="ml-2 text-xs text-gray-400 font-normal">{selectedTenants.length} selected</span>
                  )}
                </label>
                <div className="border border-gray-200 rounded-md overflow-hidden max-h-44 overflow-y-auto">
                  {MOCK_TENANTS.map((t) => (
                    <label
                      key={t}
                      className="flex items-center gap-3 px-3 py-2.5 hover:bg-gray-50 cursor-pointer border-b border-gray-50 last:border-0"
                    >
                      <input
                        type="checkbox"
                        checked={selectedTenants.includes(t)}
                        onChange={() => toggleTenant(t)}
                        className="rounded border-gray-300 text-gray-900 focus:ring-0"
                      />
                      <span className="text-sm text-gray-700">{t}</span>
                    </label>
                  ))}
                </div>
              </div>
            )}

            {/* Rent due filter */}
            {target === 'rent_due' && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Rent status</label>
                <div className="flex gap-2">
                  {([
                    { id: 'upcoming', label: 'Upcoming (7 days)' },
                    { id: 'overdue', label: 'Overdue' },
                    { id: 'both', label: 'Both' },
                  ] as { id: RentDueFilter; label: string }[]).map((opt) => (
                    <button
                      key={opt.id}
                      type="button"
                      onClick={() => setRentDueFilter(opt.id)}
                      className={`flex-1 py-2 px-2 rounded-md border text-xs font-medium transition-colors ${
                        rentDueFilter === opt.id
                          ? 'border-amber-500 bg-amber-50 text-amber-700'
                          : 'border-gray-200 text-gray-600 hover:border-gray-400'
                      }`}
                    >
                      {opt.label}
                    </button>
                  ))}
                </div>
              </div>
            )}

            {/* Recipient estimate */}
            {estimatedRecipients() > 0 && (
              <div className="flex items-center gap-2 bg-gray-50 border border-gray-100 rounded-md px-3 py-2">
                <Bell size={13} className="text-gray-400" />
                <p className="text-xs text-gray-500">
                  Estimated recipients: <span className="font-semibold text-gray-900">{estimatedRecipients()}</span>
                </p>
              </div>
            )}

            {/* Channel */}
            <div>
              <label className="block text-xs font-semibold uppercase tracking-widest text-gray-400 mb-2">
                Channel
              </label>
              <div className="flex gap-2">
                {CHANNEL_OPTIONS.map((c) => (
                  <button
                    key={c.id}
                    type="button"
                    onClick={() => toggleChannel(c.id)}
                    className={`flex-1 py-2 rounded-md border text-sm font-medium transition-colors ${
                      channels.includes(c.id)
                        ? 'border-gray-900 bg-gray-900 text-white'
                        : 'border-gray-200 text-gray-600 hover:border-gray-400'
                    }`}
                  >
                    {c.label}
                  </button>
                ))}
              </div>
            </div>

            {/* Subject */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Subject</label>
              <input
                value={subject}
                onChange={(e) => setSubject(e.target.value)}
                placeholder="e.g. Rent reminder — July 2026"
                className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 placeholder:text-gray-300"
              />
            </div>

            {/* Message */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Message</label>
              <textarea
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                rows={4}
                placeholder="Write your alert message here…"
                className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 resize-none placeholder:text-gray-300"
              />
            </div>

            {sent && (
              <div className="flex items-center gap-2 bg-green-50 border border-green-100 rounded-md px-3 py-2.5">
                <CheckCircle size={14} className="text-green-600 shrink-0" />
                <p className="text-xs text-green-800 font-medium">Alert sent successfully.</p>
              </div>
            )}

            <button
              type="submit"
              disabled={!canSend}
              className="w-full flex items-center justify-center gap-2 bg-gray-900 text-white text-sm font-semibold py-3 rounded-md hover:bg-gray-700 transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
            >
              <Send size={14} />
              Send alert
            </button>
          </form>
        </div>

        {/* ── History panel ── */}
        <div className="xl:col-span-3 border border-gray-100 rounded-2xl overflow-hidden">
          <div className="border-b border-gray-100 px-6 py-4 bg-gray-50 flex items-center justify-between">
            <p className="text-sm font-semibold text-gray-900">Sent alerts</p>
            <p className="text-xs text-gray-400">{history.length} total</p>
          </div>

          {history.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-20 text-center px-6">
              <Bell size={32} className="text-gray-200 mb-4" />
              <p className="text-sm text-gray-400">No alerts sent yet.</p>
              <p className="text-xs text-gray-300 mt-1">Compose an alert on the left to get started.</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-gray-100 bg-gray-50">
                    <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Date</th>
                    <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Subject</th>
                    <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Target</th>
                    <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Channel</th>
                    <th className="text-right px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Recipients</th>
                    <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Status</th>
                  </tr>
                </thead>
                <tbody>
                  {history.map((a) => (
                    <tr key={a.id} className="border-b border-gray-50 hover:bg-gray-50">
                      <td className="px-5 py-3 text-gray-400 text-xs whitespace-nowrap">{a.sentAt}</td>
                      <td className="px-5 py-3 font-medium text-gray-900 max-w-[180px]">
                        <p className="truncate">{a.subject}</p>
                        <p className="text-xs text-gray-400 font-normal truncate mt-0.5">{a.message}</p>
                      </td>
                      <td className="px-5 py-3 text-gray-500 max-w-[140px]">
                        <p className="truncate text-xs">{a.target}</p>
                      </td>
                      <td className="px-5 py-3 text-gray-500 text-xs whitespace-nowrap">{a.channels}</td>
                      <td className="px-5 py-3 text-right text-gray-900 font-medium">{a.recipients}</td>
                      <td className="px-5 py-3">
                        <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${statusColors[a.status]}`}>
                          {a.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

      </div>
    </div>
  )
}
