import { Link, NavLink } from 'react-router-dom'

export default function Navbar() {
  return (
    <header className="sticky top-0 z-50 bg-white border-b border-gray-100">
      <div className="max-w-6xl mx-auto px-6 h-14 flex items-center justify-between">
        <Link to="/" className="text-base font-bold text-gray-900 tracking-tight">
          BuildAgent
        </Link>

        <nav className="hidden md:flex items-center gap-7">
          {['Product', 'Solutions', 'Pricing', 'About', 'Contact'].map((item) => (
            <NavLink
              key={item}
              to={item === 'Product' ? '/' : `/${item.toLowerCase()}`}
              className={({ isActive }) =>
                `text-sm font-medium transition-colors ${
                  isActive ? 'text-gray-900' : 'text-gray-500 hover:text-gray-900'
                }`
              }
            >
              {item}
            </NavLink>
          ))}
        </nav>

        <Link
          to="/contact"
          className="bg-gray-900 text-white text-sm font-medium px-4 py-2 rounded-md hover:bg-gray-700 transition-colors"
        >
          Get Started
        </Link>
      </div>
    </header>
  )
}
