import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import {
  Building2,
  LayoutGrid,
  Building,
  Users,
  CreditCard,
  Wrench,
  FileText,
  LogOut,
  SquareUser,
} from 'lucide-react'

const nav = [
  { to: '/dashboard', label: 'Overview', icon: <LayoutGrid size={16} />, end: true },
  { to: '/dashboard/buildings', label: 'Buildings', icon: <Building size={16} /> },
  { to: '/dashboard/tenants', label: 'Tenants', icon: <Users size={16} /> },
  { to: '/dashboard/payments', label: 'Payments', icon: <CreditCard size={16} /> },
  { to: '/dashboard/maintenance', label: 'Maintenance', icon: <Wrench size={16} /> },
  { to: '/dashboard/documents', label: 'Documents', icon: <FileText size={16} /> },
]

export default function DashboardLayout() {
  const navigate = useNavigate()

  return (
    <div className="flex min-h-screen bg-white">
      {/* Sidebar */}
      <aside className="w-52 border-r border-gray-100 flex flex-col shrink-0">
        <div className="h-14 flex items-center px-4 border-b border-gray-100">
          <span className="flex items-center gap-2 text-sm font-bold text-gray-900">
            <Building2 size={16} />
            BuildAgent
          </span>
        </div>

        <div className="flex-1 p-3">
          <p className="text-xs font-semibold uppercase tracking-widest text-gray-400 px-2 mt-2 mb-2">
            Workspace
          </p>
          <nav className="space-y-0.5">
            {nav.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                className={({ isActive }) =>
                  `flex items-center gap-2.5 px-2 py-2 rounded-md text-sm transition-colors ${
                    isActive
                      ? 'bg-gray-100 text-gray-900 font-medium'
                      : 'text-gray-500 hover:text-gray-900 hover:bg-gray-50'
                  }`
                }
              >
                {item.icon}
                {item.label}
              </NavLink>
            ))}
          </nav>
        </div>
      </aside>

      {/* Main */}
      <div className="flex-1 flex flex-col">
        {/* Top bar */}
        <div className="h-14 border-b border-gray-100 flex items-center justify-between px-6">
          <div className="flex items-center gap-2 text-sm text-gray-500">
            <SquareUser size={14} />
            davidnjau21@gmail.com
          </div>
          <button
            onClick={() => navigate('/auth')}
            className="flex items-center gap-1.5 text-sm text-gray-500 hover:text-gray-900 transition-colors"
          >
            <LogOut size={14} />
            Sign out
          </button>
        </div>

        {/* Page content */}
        <main className="flex-1 p-8">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
