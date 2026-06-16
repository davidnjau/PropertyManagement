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
  Settings,
  Bell,
} from 'lucide-react'

const workspaceNav = [
  { to: '/dashboard', label: 'Overview', icon: <LayoutGrid size={16} />, end: true },
  { to: '/dashboard/buildings', label: 'Buildings', icon: <Building size={16} /> },
  { to: '/dashboard/tenants', label: 'Tenants', icon: <Users size={16} /> },
  { to: '/dashboard/payments', label: 'Payments', icon: <CreditCard size={16} /> },
  { to: '/dashboard/maintenance', label: 'Maintenance', icon: <Wrench size={16} /> },
  { to: '/dashboard/documents', label: 'Documents', icon: <FileText size={16} /> },
]

const adminNav = [
  { to: '/dashboard/admin/alerts', label: 'Alerts', icon: <Bell size={16} /> },
  { to: '/dashboard/admin/documents', label: 'Documents', icon: <FileText size={16} /> },
  { to: '/dashboard/admin/payment-methods', label: 'Payment Methods', icon: <Settings size={16} /> },
]

function SidebarLink({ to, label, icon, end }: { to: string; label: string; icon: React.ReactNode; end?: boolean }) {
  return (
    <NavLink
      to={to}
      end={end}
      className={({ isActive }) =>
        `flex items-center gap-2.5 px-2 py-2 rounded-md text-sm transition-colors ${
          isActive
            ? 'bg-gray-100 text-gray-900 font-medium'
            : 'text-gray-500 hover:text-gray-900 hover:bg-gray-50'
        }`
      }
    >
      {icon}
      {label}
    </NavLink>
  )
}

export default function DashboardLayout() {
  const navigate = useNavigate()

  return (
    <div className="flex h-screen overflow-hidden bg-white">
      {/* Sidebar */}
      <aside className="w-52 border-r border-gray-100 flex flex-col shrink-0">
        <div className="h-14 flex items-center px-4 border-b border-gray-100">
          <span className="flex items-center gap-2 text-sm font-bold text-gray-900">
            <Building2 size={16} />
            BuildAgent
          </span>
        </div>

        <div className="flex-1 p-3 flex flex-col gap-4">
          <div>
            <p className="text-xs font-semibold uppercase tracking-widest text-gray-400 px-2 mt-2 mb-2">
              Workspace
            </p>
            <nav className="space-y-0.5">
              {workspaceNav.map((item) => (
                <SidebarLink key={item.to} {...item} />
              ))}
            </nav>
          </div>

          <div>
            <p className="text-xs font-semibold uppercase tracking-widest text-gray-400 px-2 mb-2">
              Admin
            </p>
            <nav className="space-y-0.5">
              {adminNav.map((item) => (
                <SidebarLink key={item.to} {...item} />
              ))}
            </nav>
          </div>
        </div>
      </aside>

      {/* Main */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* Top bar */}
        <div className="h-14 border-b border-gray-100 flex items-center justify-between px-6 shrink-0">
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
        <main className="flex-1 overflow-y-auto p-8">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
