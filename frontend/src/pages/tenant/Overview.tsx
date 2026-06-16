import { Link } from 'react-router-dom'
import { CreditCard, FileText, Wrench, AlertCircle } from 'lucide-react'

const recentPayments = [
  { month: 'May 2026', amount: 45000, method: 'M-Pesa', status: 'Paid' },
  { month: 'Apr 2026', amount: 45000, method: 'M-Pesa', status: 'Paid' },
  { month: 'Mar 2026', amount: 45000, method: 'Bank Transfer', status: 'Paid' },
]

export default function TenantOverview() {
  return (
    <div className="w-full">
      <h1 className="text-2xl font-bold text-gray-900 mb-1">Good morning</h1>
      <p className="text-sm text-gray-500 mb-8">Here's a summary of your tenancy.</p>

      {/* Rent due card */}
      <div className="bg-gray-900 text-white rounded-2xl p-6 mb-6 flex items-center justify-between">
        <div>
          <p className="text-xs font-semibold uppercase tracking-widest text-gray-400 mb-2">Rent due</p>
          <p className="text-3xl font-extrabold mb-1">KES 45,000</p>
          <p className="text-sm text-gray-400">Due 1 Jul 2026</p>
        </div>
        <div className="flex flex-col items-end gap-3">
          <span className="text-xs font-semibold bg-amber-400 text-gray-900 px-2.5 py-1 rounded-full">
            Upcoming
          </span>
          <Link
            to="/tenant/pay-rent"
            className="text-sm font-semibold bg-white text-gray-900 px-4 py-2 rounded-md hover:bg-gray-100 transition-colors"
          >
            Pay now
          </Link>
        </div>
      </div>

      {/* Quick links */}
      <div className="grid grid-cols-3 gap-3 mb-8">
        {[
          { to: '/tenant/pay-rent', icon: <CreditCard size={16} />, label: 'Pay Rent' },
          { to: '/tenant/lease', icon: <FileText size={16} />, label: 'My Lease' },
          { to: '/tenant/maintenance', icon: <Wrench size={16} />, label: 'Report Issue' },
        ].map((q) => (
          <Link
            key={q.to}
            to={q.to}
            className="border border-gray-100 rounded-xl p-4 flex flex-col items-center gap-2 hover:border-gray-300 transition-colors text-center"
          >
            <span className="text-gray-500">{q.icon}</span>
            <span className="text-sm font-medium text-gray-700">{q.label}</span>
          </Link>
        ))}
      </div>

      {/* Notice */}
      <div className="flex items-start gap-3 bg-amber-50 border border-amber-100 rounded-xl px-5 py-4 mb-8">
        <AlertCircle size={16} className="text-amber-500 mt-0.5 shrink-0" />
        <p className="text-sm text-amber-800">
          Your lease expires on <span className="font-semibold">31 Dec 2026</span>. Contact your agent to discuss renewal.
        </p>
      </div>

      {/* Recent payments */}
      <div className="border border-gray-100 rounded-xl overflow-hidden">
        <div className="border-b border-gray-100 px-5 py-3 flex items-center justify-between">
          <p className="text-sm font-semibold text-gray-900">Recent payments</p>
          <Link to="/tenant/pay-rent" className="text-xs text-gray-400 hover:text-gray-700 transition-colors">
            View all →
          </Link>
        </div>
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-100 bg-gray-50">
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Period</th>
              <th className="text-right px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Amount</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Method</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Status</th>
            </tr>
          </thead>
          <tbody>
            {recentPayments.map((p) => (
              <tr key={p.month} className="border-b border-gray-50">
                <td className="px-5 py-3 text-gray-900 font-medium">{p.month}</td>
                <td className="px-5 py-3 text-right text-gray-900">KES {p.amount.toLocaleString()}</td>
                <td className="px-5 py-3 text-gray-500">{p.method}</td>
                <td className="px-5 py-3">
                  <span className="text-xs font-medium bg-green-50 text-green-700 px-2 py-0.5 rounded-full">
                    {p.status}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
