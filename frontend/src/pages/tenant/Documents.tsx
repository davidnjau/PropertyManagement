import { FileText, Download } from 'lucide-react'

const docs = [
  { name: 'Lease Agreement — Jan 2026', type: 'PDF', size: '248 KB', date: '1 Jan 2026' },
  { name: 'Move-in Inspection Report', type: 'PDF', size: '1.2 MB', date: '3 Jan 2026' },
  { name: 'House Rules & Regulations', type: 'PDF', size: '84 KB', date: '1 Jan 2026' },
  { name: 'Deposit Receipt', type: 'PDF', size: '32 KB', date: '1 Jan 2026' },
]

export default function TenantDocuments() {
  return (
    <div className="w-full">
      <h1 className="text-2xl font-bold text-gray-900 mb-1">Documents</h1>
      <p className="text-sm text-gray-500 mb-8">Your lease documents and notices.</p>

      <div className="border border-gray-100 rounded-xl overflow-hidden">
        <div className="border-b border-gray-100 px-5 py-3">
          <p className="text-sm font-semibold text-gray-900">All documents</p>
        </div>
        <ul className="divide-y divide-gray-50">
          {docs.map((d) => (
            <li key={d.name} className="flex items-center justify-between px-5 py-4 hover:bg-gray-50 transition-colors">
              <div className="flex items-center gap-3">
                <FileText size={16} className="text-gray-400 shrink-0" />
                <div>
                  <p className="text-sm font-medium text-gray-900">{d.name}</p>
                  <p className="text-xs text-gray-400">{d.type} · {d.size} · {d.date}</p>
                </div>
              </div>
              <button className="text-gray-400 hover:text-gray-700 transition-colors p-1">
                <Download size={15} />
              </button>
            </li>
          ))}
        </ul>
      </div>
    </div>
  )
}
