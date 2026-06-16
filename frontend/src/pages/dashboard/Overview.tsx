import { Building, Users, CreditCard, Wrench } from 'lucide-react'
import { Link } from 'react-router-dom'

const stats = [
  { label: 'Buildings', value: 0, icon: <Building size={16} className="text-gray-400" /> },
  { label: 'Tenants', value: 0, icon: <Users size={16} className="text-gray-400" /> },
  { label: 'Payments', value: 0, icon: <CreditCard size={16} className="text-gray-400" /> },
  { label: 'Open Maintenance', value: 0, icon: <Wrench size={16} className="text-gray-400" /> },
]

export default function Overview() {
  return (
    <div className="max-w-5xl">
      <h1 className="text-2xl font-bold text-gray-900 mb-1">Portfolio overview</h1>
      <p className="text-sm text-gray-500 mb-8">A snapshot of your workspace today.</p>

      {/* Stat cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        {stats.map((s) => (
          <div key={s.label} className="border border-gray-100 rounded-xl p-5">
            <div className="flex items-center justify-between mb-3">
              <p className="text-sm text-gray-500">{s.label}</p>
              {s.icon}
            </div>
            <p className="text-3xl font-bold text-gray-900">{s.value}</p>
          </div>
        ))}
      </div>

      {/* Get started */}
      <div className="border border-gray-100 rounded-xl p-6">
        <h2 className="text-base font-semibold text-gray-900 mb-4">Get started</h2>
        <ol className="space-y-2 text-sm text-gray-500">
          <li>
            1. Add your first building in{' '}
            <Link to="/dashboard/buildings" className="font-semibold text-gray-900 hover:underline">
              Buildings
            </Link>
            .
          </li>
          <li>2. Register tenants and leases.</li>
          <li>3. Log payments and maintenance requests as they happen.</li>
        </ol>
      </div>
    </div>
  )
}
