import { Link } from 'react-router-dom'

export default function Footer() {
  return (
    <footer className="border-t border-gray-100 bg-white">
      <div className="max-w-6xl mx-auto px-6 py-12">
        <div className="flex flex-col md:flex-row justify-between gap-10">
          <div className="max-w-xs">
            <p className="text-base font-bold text-gray-900 mb-2">BuildAgent</p>
            <p className="text-sm text-gray-500">The workspace for modern property professionals.</p>
          </div>

          <div className="flex gap-16">
            <div>
              <p className="text-xs font-semibold uppercase tracking-widest text-gray-400 mb-3">Product</p>
              <ul className="space-y-2">
                <li><Link to="/" className="text-sm text-gray-600 hover:text-gray-900">Features</Link></li>
                <li><Link to="/solutions" className="text-sm text-gray-600 hover:text-gray-900">Solutions</Link></li>
                <li><Link to="/pricing" className="text-sm text-gray-600 hover:text-gray-900">Pricing</Link></li>
              </ul>
            </div>
            <div>
              <p className="text-xs font-semibold uppercase tracking-widest text-gray-400 mb-3">Company</p>
              <ul className="space-y-2">
                <li><Link to="/about" className="text-sm text-gray-600 hover:text-gray-900">About</Link></li>
                <li><Link to="/contact" className="text-sm text-gray-600 hover:text-gray-900">Contact</Link></li>
              </ul>
            </div>
            <div>
              <p className="text-xs font-semibold uppercase tracking-widest text-gray-400 mb-3">Legal</p>
              <ul className="space-y-2">
                <li><a href="#" className="text-sm text-gray-600 hover:text-gray-900">Privacy</a></li>
                <li><a href="#" className="text-sm text-gray-600 hover:text-gray-900">Terms</a></li>
              </ul>
            </div>
          </div>
        </div>

        <div className="mt-10 pt-6 border-t border-gray-100">
          <p className="text-xs text-gray-400">© 2026 BuildAgent Technologies Inc. All rights reserved.</p>
        </div>
      </div>
    </footer>
  )
}
