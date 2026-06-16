import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Building2 } from 'lucide-react'

type Role = 'agent' | 'tenant'

export default function Auth() {
  const [tab, setTab] = useState<'signin' | 'signup'>('signin')
  const [role, setRole] = useState<Role>('agent')
  const [form, setForm] = useState({ email: '', password: '', name: '' })
  const navigate = useNavigate()

  function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    navigate(role === 'tenant' ? '/tenant' : '/dashboard')
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center px-4">
      <div className="flex items-center gap-2 text-base font-bold text-gray-900 mb-8">
        <Building2 size={20} />
        BuildAgent
      </div>

      <div className="w-full max-w-sm bg-white border border-gray-100 rounded-2xl p-8 shadow-sm">
        <h1 className="text-lg font-bold text-gray-900 mb-1">Welcome</h1>
        <p className="text-sm text-gray-500 mb-6">Sign in or create your workspace.</p>

        {/* Sign in / Sign up toggle */}
        <div className="flex bg-gray-100 rounded-lg p-1 mb-4">
          {(['signin', 'signup'] as const).map((t) => (
            <button
              key={t}
              onClick={() => setTab(t)}
              className={`flex-1 text-sm font-medium py-2 rounded-md transition-colors ${
                tab === t ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              {t === 'signin' ? 'Sign in' : 'Sign up'}
            </button>
          ))}
        </div>

        {/* Role selector */}
        <div className="flex bg-gray-100 rounded-lg p-1 mb-6">
          {([
            { value: 'agent', label: 'Agent / Admin' },
            { value: 'tenant', label: 'Tenant' },
          ] as { value: Role; label: string }[]).map((r) => (
            <button
              key={r.value}
              onClick={() => setRole(r.value)}
              className={`flex-1 text-sm font-medium py-2 rounded-md transition-colors ${
                role === r.value ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              {r.label}
            </button>
          ))}
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          {tab === 'signup' && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Full name</label>
              <input
                name="name"
                value={form.name}
                onChange={handleChange}
                className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 transition-colors"
              />
            </div>
          )}

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Email</label>
            <input
              name="email"
              type="email"
              value={form.email}
              onChange={handleChange}
              className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 transition-colors"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Password</label>
            <input
              name="password"
              type="password"
              value={form.password}
              onChange={handleChange}
              className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 transition-colors"
            />
          </div>

          <button
            type="submit"
            className="w-full bg-gray-900 text-white text-sm font-semibold py-3 rounded-md hover:bg-gray-700 transition-colors mt-2"
          >
            {tab === 'signin' ? 'Sign in' : 'Create account'}
          </button>
        </form>
      </div>
    </div>
  )
}
