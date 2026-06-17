import { useState } from 'react'
import { CheckCircle, XCircle, X } from 'lucide-react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { fetchLeaseExtensions, resolveLeaseExtension } from '../../../services/leaseExtensions'
import type { LeaseExtensionRequest } from '../../../services/leaseExtensions'

type Filter = 'PENDING' | 'APPROVED' | 'REJECTED'

const statusColors: Record<string, string> = {
  PENDING: 'bg-amber-50 text-amber-700',
  APPROVED: 'bg-green-50 text-green-700',
  REJECTED: 'bg-red-50 text-red-700',
}

export default function LeaseExtensions() {
  const qc = useQueryClient()

  const { data: requests = [], isLoading, isError } = useQuery({
    queryKey: ['lease-extension-requests'],
    queryFn: fetchLeaseExtensions,
  })

  const mutation = useMutation({
    mutationFn: ({ id, status, agentNotes }: { id: string; status: string; agentNotes?: string }) =>
      resolveLeaseExtension(id, status, agentNotes),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['lease-extension-requests'] }),
  })

  const [filter, setFilter] = useState<Filter>('PENDING')
  const [selected, setSelected] = useState<LeaseExtensionRequest | null>(null)
  const [agentNotes, setAgentNotes] = useState('')
  const [resolving, setResolving] = useState<'APPROVED' | 'REJECTED' | null>(null)

  const filtered = requests.filter((r) => r.status === filter)

  function openModal(req: LeaseExtensionRequest) {
    setSelected(req)
    setAgentNotes('')
    setResolving(null)
  }

  function closeModal() {
    setSelected(null)
    setAgentNotes('')
    setResolving(null)
  }

  function handleResolve(status: 'APPROVED' | 'REJECTED') {
    if (!selected) return
    setResolving(status)
    mutation.mutate(
      { id: selected.id, status, agentNotes: agentNotes.trim() || undefined },
      { onSuccess: closeModal },
    )
  }

  const tabs: Filter[] = ['PENDING', 'APPROVED', 'REJECTED']
  const counts = tabs.reduce<Record<string, number>>((acc, s) => {
    acc[s] = requests.filter((r) => r.status === s).length
    return acc
  }, {})

  return (
    <div className="w-full">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-1">Lease Extension Requests</h1>
        <p className="text-sm text-gray-500">Review and approve or reject tenant lease extension requests.</p>
      </div>

      {/* Filter tabs */}
      <div className="flex bg-gray-100 rounded-lg p-1 mb-6 w-fit">
        {tabs.map((tab) => (
          <button
            key={tab}
            onClick={() => setFilter(tab)}
            className={`flex items-center gap-2 px-4 py-2 rounded-md text-sm font-medium transition-colors ${
              filter === tab ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-500 hover:text-gray-700'
            }`}
          >
            {tab.charAt(0) + tab.slice(1).toLowerCase()}
            {counts[tab] > 0 && (
              <span className={`text-xs font-bold px-1.5 py-0.5 rounded-full ${
                tab === 'PENDING' ? 'bg-amber-100 text-amber-700' : 'bg-gray-200 text-gray-600'
              }`}>
                {counts[tab]}
              </span>
            )}
          </button>
        ))}
      </div>

      {/* Table */}
      <div className="border border-gray-100 rounded-xl overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-100 bg-gray-50">
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Lease ID</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Current End</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Proposed End</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Duration</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Submitted</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Status</th>
              {filter === 'PENDING' && (
                <th className="px-5 py-3" />
              )}
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr>
                <td colSpan={7} className="px-5 py-10 text-center text-sm text-gray-400">Loading…</td>
              </tr>
            ) : isError ? (
              <tr>
                <td colSpan={7} className="px-5 py-10 text-center text-sm text-red-500">Failed to load data.</td>
              </tr>
            ) : filtered.length === 0 ? (
              <tr>
                <td colSpan={7} className="px-5 py-10 text-center text-sm text-gray-400">
                  No {filter.toLowerCase()} requests.
                </td>
              </tr>
            ) : (
              filtered.map((req) => (
                <tr key={req.id} className="border-b border-gray-50 hover:bg-gray-50">
                  <td className="px-5 py-3 font-mono text-xs text-gray-500">{req.leaseId.slice(0, 8)}…</td>
                  <td className="px-5 py-3 text-gray-700">{req.currentEndDate.slice(0, 10)}</td>
                  <td className="px-5 py-3 font-medium text-gray-900">{req.proposedEndDate.slice(0, 10)}</td>
                  <td className="px-5 py-3 text-gray-500">
                    {req.durationMonths ? `${req.durationMonths} months` : 'Custom'}
                  </td>
                  <td className="px-5 py-3 text-gray-400 text-xs">{req.submittedAt.slice(0, 10)}</td>
                  <td className="px-5 py-3">
                    <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${statusColors[req.status] ?? ''}`}>
                      {req.status}
                    </span>
                  </td>
                  {filter === 'PENDING' && (
                    <td className="px-5 py-3 text-right">
                      <button
                        onClick={() => openModal(req)}
                        className="text-xs font-medium text-gray-500 hover:text-gray-900 border border-gray-200 hover:border-gray-400 px-3 py-1.5 rounded-md transition-colors"
                      >
                        Review
                      </button>
                    </td>
                  )}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Review modal */}
      {selected && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl w-full max-w-md shadow-xl">
            <div className="flex items-center justify-between px-7 pt-7 pb-5 border-b border-gray-100">
              <div>
                <h2 className="text-lg font-bold text-gray-900">Review request</h2>
                <p className="text-xs text-gray-400 mt-0.5 font-mono">{selected.leaseId}</p>
              </div>
              <button onClick={closeModal} className="text-gray-400 hover:text-gray-600">
                <X size={18} />
              </button>
            </div>

            <div className="px-7 py-5 space-y-4">
              <dl className="space-y-2">
                {[
                  { label: 'Current end date', value: selected.currentEndDate.slice(0, 10) },
                  { label: 'Proposed end date', value: selected.proposedEndDate.slice(0, 10) },
                  { label: 'Duration', value: selected.durationMonths ? `${selected.durationMonths} months` : 'Custom' },
                  { label: 'Submitted', value: selected.submittedAt.slice(0, 10) },
                ].map(({ label, value }) => (
                  <div key={label} className="flex items-center justify-between text-sm">
                    <dt className="text-gray-500">{label}</dt>
                    <dd className="font-medium text-gray-900">{value}</dd>
                  </div>
                ))}
              </dl>

              {selected.notes && (
                <div className="bg-gray-50 border border-gray-100 rounded-md px-4 py-3">
                  <p className="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-1">Tenant notes</p>
                  <p className="text-sm text-gray-700">{selected.notes}</p>
                </div>
              )}

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">
                  Agent notes <span className="text-gray-400 font-normal">(optional)</span>
                </label>
                <textarea
                  value={agentNotes}
                  onChange={(e) => setAgentNotes(e.target.value)}
                  rows={3}
                  placeholder="Add a note for the tenant…"
                  className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 resize-none placeholder:text-gray-300"
                />
              </div>

              <div className="flex gap-3 pt-1">
                <button
                  onClick={() => handleResolve('REJECTED')}
                  disabled={mutation.isPending}
                  className="flex-1 flex items-center justify-center gap-2 border border-red-200 text-red-600 text-sm font-medium py-2.5 rounded-md hover:bg-red-50 transition-colors disabled:opacity-40"
                >
                  <XCircle size={14} />
                  {resolving === 'REJECTED' && mutation.isPending ? 'Rejecting…' : 'Reject'}
                </button>
                <button
                  onClick={() => handleResolve('APPROVED')}
                  disabled={mutation.isPending}
                  className="flex-1 flex items-center justify-center gap-2 bg-gray-900 text-white text-sm font-semibold py-2.5 rounded-md hover:bg-gray-700 transition-colors disabled:opacity-40"
                >
                  <CheckCircle size={14} />
                  {resolving === 'APPROVED' && mutation.isPending ? 'Approving…' : 'Approve'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
