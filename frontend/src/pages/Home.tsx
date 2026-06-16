import { Link } from 'react-router-dom'
import { CreditCard, Wrench, Users } from 'lucide-react'

const modules = [
  {
    icon: <CreditCard size={18} className="text-gray-400" />,
    title: 'Payment Ledger',
    desc: 'Zero-touch reconciliation with banking integration.',
  },
  {
    icon: <Wrench size={18} className="text-gray-400" />,
    title: 'Maintenance Desk',
    desc: 'Intelligent triage of repair requests and contractor assignment.',
  },
  {
    icon: <Users size={18} className="text-gray-400" />,
    title: 'Tenancy CRM',
    desc: 'From digital signatures to move-out inspections.',
  },
]

export default function Home() {
  return (
    <>
      {/* Hero */}
      <section className="max-w-4xl mx-auto px-6 pt-28 pb-20 text-center">
        <span className="inline-block border border-gray-200 text-xs font-semibold text-gray-500 tracking-widest uppercase px-3 py-1 rounded-full mb-8">
          Next-gen property OS
        </span>
        <h1 className="text-5xl md:text-6xl font-extrabold text-gray-900 leading-tight mb-5">
          The intelligent core of your{' '}
          <span className="text-slate-400">real estate portfolio.</span>
        </h1>
        <p className="text-lg text-gray-500 max-w-xl mx-auto mb-10">
          Replace fragmented spreadsheets with a unified workspace for agents, tenants, and maintenance teams.
        </p>
        <div className="flex justify-center gap-3">
          <Link
            to="/contact"
            className="bg-gray-900 text-white text-sm font-semibold px-6 py-3 rounded-md hover:bg-gray-700 transition-colors"
          >
            Request demo
          </Link>
          <Link
            to="/solutions"
            className="border border-gray-300 text-gray-700 text-sm font-semibold px-6 py-3 rounded-md hover:border-gray-400 transition-colors"
          >
            View modules
          </Link>
        </div>
      </section>

      {/* Module cards */}
      <section className="max-w-6xl mx-auto px-6 pb-24">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {modules.map((m) => (
            <div key={m.title} className="border border-gray-100 rounded-xl p-7 hover:border-gray-200 transition-colors">
              <div className="mb-4">{m.icon}</div>
              <h3 className="text-sm font-semibold text-gray-900 mb-2">{m.title}</h3>
              <p className="text-sm text-gray-500 leading-relaxed">{m.desc}</p>
            </div>
          ))}
        </div>
      </section>
    </>
  )
}
