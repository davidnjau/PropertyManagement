import { Building, Users, CreditCard, Wrench } from 'lucide-react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { fetchDashboardStats } from '../../services/dashboard'

export default function Overview() {
  const { data: stats, isLoading, isError } = useQuery({
    queryKey: ['dashboard-stats'],
    queryFn: fetchDashboardStats,
  })

  const statCards = [
    { label: 'Buildings', value: stats?.buildings ?? 0, icon: <Building size={16} className="text-gray-400" /> },
    { label: 'Tenants', value: stats?.tenants ?? 0, icon: <Users size={16} className="text-gray-400" /> },
    { label: 'Payments', value: stats?.payments ?? 0, icon: <CreditCard size={16} className="text-gray-400" /> },
    { label: 'Open Maintenance', value: stats?.openMaintenance ?? 0, icon: <Wrench size={16} className="text-gray-400" /> },
  ]

  return (
    <div className="w-full">
      <h1 className="text-2xl font-bold text-gray-900 mb-1">Portfolio overview</h1>
      <p className="text-sm text-gray-500 mb-8">A snapshot of your workspace today.</p>

      {/* Stat cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        {isLoading ? (
          <div className="col-span-4">
            <p className="text-sm text-gray-400">Loading...</p>
          </div>
        ) : isError ? (
          <div className="col-span-4">
            <p className="text-sm text-red-500">Failed to load data.</p>
          </div>
        ) : (
          statCards.map((s) => (
            <div key={s.label} className="border border-gray-100 rounded-xl p-5">
              <div className="flex items-center justify-between mb-3">
                <p className="text-sm text-gray-500">{s.label}</p>
                {s.icon}
              </div>
              <p className="text-3xl font-bold text-gray-900">{s.value}</p>
            </div>
          ))
        )}
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
