import { Link } from 'react-router-dom'
import { Building2 } from 'lucide-react'

export default function Navbar() {
  return (
    <header className="sticky top-0 z-50 bg-white border-b border-gray-100">
      <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
        <Link to="/" className="flex items-center gap-2 text-base font-bold text-gray-900 tracking-tight">
          <Building2 size={20} />
          BuildAgent
        </Link>

        <div className="flex items-center gap-4">
          <Link
            to="/auth"
            className="text-sm font-medium text-gray-600 hover:text-gray-900 transition-colors"
          >
            Sign in
          </Link>
          <Link
            to="/auth"
            className="flex items-center gap-1.5 bg-gray-900 text-white text-sm font-medium px-4 py-2 rounded-md hover:bg-gray-700 transition-colors"
          >
            Get started
            <span aria-hidden>→</span>
          </Link>
        </div>
      </div>
    </header>
  )
}
