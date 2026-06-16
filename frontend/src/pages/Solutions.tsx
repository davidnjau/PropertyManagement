import { Link } from 'react-router-dom'

const roles = [
  {
    letter: 'A',
    color: 'bg-blue-500',
    title: 'Building Agents',
    desc: 'Centralise portfolios, automate rent roll reconciliation, and ship client reports without leaving the platform.',
    features: ['Portfolio overview', 'Payment tracking', 'Maintenance scheduling', 'Client reporting'],
    align: 'left',
  },
  {
    letter: 'C',
    color: 'bg-amber-400',
    title: 'Property Owners & Clients',
    desc: 'Real-time visibility into how your assets are performing. No more chasing your agent for updates.',
    features: ['Portfolio visibility', 'Payment summaries', 'Document access', 'Owner statements'],
    align: 'right',
  },
  {
    letter: 'T',
    color: 'bg-green-500',
    title: 'Tenants',
    desc: 'Pay rent, view your lease, and log maintenance requests from a clean self-service portal.',
    features: ['Pay rent online', 'View lease & docs', 'Submit maintenance', 'Message your agent'],
    align: 'left',
  },
  {
    letter: 'V',
    color: 'bg-purple-500',
    title: 'Contractors',
    desc: 'Receive work orders, update job status with photo evidence, and submit invoices in one place.',
    features: ['Job orders', 'Status updates', 'Photo evidence', 'Invoice submission'],
    align: 'right',
  },
]

export default function Solutions() {
  return (
    <>
      {/* Hero */}
      <section className="max-w-6xl mx-auto px-6 pt-24 pb-16 text-center">
        <p className="text-xs font-semibold uppercase tracking-widest text-gray-400 mb-6">
          Built for everyone in the building
        </p>
        <h1 className="text-5xl md:text-6xl font-extrabold text-gray-900 leading-tight mb-4">
          Role-based workspaces,
          <br />
          one shared truth.
        </h1>
        <p className="text-lg text-gray-500 max-w-2xl mx-auto">
          Agents, owners, tenants, and contractors each get an interface tuned to their job — all backed by the same audit-ready data.
        </p>
      </section>

      {/* Role cards */}
      <section className="max-w-4xl mx-auto px-6 pb-24 space-y-6">
        {roles.map((r) => (
          <div key={r.title} className="border border-gray-100 rounded-2xl p-8 flex flex-col md:flex-row gap-8 hover:border-gray-200 transition-colors">
            {r.align === 'left' ? (
              <>
                <div className="flex-1">
                  <span
                    className={`inline-flex items-center justify-center w-8 h-8 rounded-full text-white text-sm font-bold mb-4 ${r.color}`}
                  >
                    {r.letter}
                  </span>
                  <h2 className="text-xl font-extrabold text-gray-900 mb-2">{r.title}</h2>
                  <p className="text-sm text-gray-500 leading-relaxed">{r.desc}</p>
                </div>
                <div className="flex-1 grid grid-cols-2 gap-3 content-start">
                  {r.features.map((f) => (
                    <div key={f} className="bg-gray-50 rounded-lg px-4 py-3 text-sm text-gray-700 font-medium">
                      {f}
                    </div>
                  ))}
                </div>
              </>
            ) : (
              <>
                <div className="flex-1 grid grid-cols-2 gap-3 content-start">
                  {r.features.map((f) => (
                    <div key={f} className="bg-gray-50 rounded-lg px-4 py-3 text-sm text-gray-700 font-medium">
                      {f}
                    </div>
                  ))}
                </div>
                <div className="flex-1">
                  <span
                    className={`inline-flex items-center justify-center w-8 h-8 rounded-full text-white text-sm font-bold mb-4 ${r.color}`}
                  >
                    {r.letter}
                  </span>
                  <h2 className="text-xl font-extrabold text-gray-900 mb-2">{r.title}</h2>
                  <p className="text-sm text-gray-500 leading-relaxed">{r.desc}</p>
                </div>
              </>
            )}
          </div>
        ))}
      </section>

      {/* CTA */}
      <section className="max-w-6xl mx-auto px-6 pb-24 text-center">
        <div className="bg-gray-50 rounded-2xl border border-gray-100 py-16 px-8">
          <h2 className="text-2xl font-extrabold text-gray-900 mb-6">Find the workspace that fits your role.</h2>
          <Link
            to="/contact"
            className="inline-block bg-gray-900 text-white text-sm font-semibold px-8 py-3 rounded-md hover:bg-gray-700 transition-colors"
          >
            Talk to Sales
          </Link>
        </div>
      </section>
    </>
  )
}
