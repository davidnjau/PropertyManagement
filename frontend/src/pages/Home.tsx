import { Link } from 'react-router-dom'

const personas = [
  {
    letter: 'A',
    color: 'bg-blue-500',
    title: 'The Agent',
    desc: 'Centralise leases, automate rent reconciliation, and keep every KPI in view.',
  },
  {
    letter: 'C',
    color: 'bg-amber-400',
    title: 'The Client',
    desc: 'Deliver real-time portfolio visibility into performance and yield.',
  },
  {
    letter: 'T',
    color: 'bg-green-500',
    title: 'The Tenant',
    desc: 'Pay rent online, log lease books, and access documents at any time.',
  },
  {
    letter: 'V',
    color: 'bg-purple-500',
    title: 'The Vendor',
    desc: 'Receive work orders and submit invoice reports digitally.',
  },
]

const modules = [
  {
    title: 'Payment Ledger',
    desc: 'Track rent, bond, and water transactions with currency integration and real-time reconciliation like the financials.',
  },
  {
    title: 'Maintenance Desk',
    desc: 'Intelligent logging of repair requests and work assignment to trusted contractors.',
  },
  {
    title: 'Tenancy CRM',
    desc: 'From digital signatures to lease inspections: a complete lifecycle manager.',
  },
]

const steps = [
  {
    number: '01',
    title: 'Import Portfolio',
    desc: 'Upload your existing spreadsheets. Our AI automatically maps units, tenants, and lease data.',
  },
  {
    number: '02',
    title: 'Invite the Network',
    desc: 'Onboard your tenants and contractors. They receive access to their specialised portals instantly.',
  },
  {
    number: '03',
    title: 'Automate the Rest',
    desc: 'Set your business rules and let the agent handle reminders, maintenance logs, and reporting.',
  },
]

export default function Home() {
  return (
    <>
      {/* Hero */}
      <section className="max-w-6xl mx-auto px-6 pt-24 pb-20 text-center">
        <p className="text-xs font-semibold uppercase tracking-widest text-gray-400 mb-6">
          Next-gen property AI
        </p>
        <h1 className="text-5xl md:text-6xl font-extrabold text-gray-900 leading-tight mb-4">
          The intelligent core of your
          <br />
          <span className="text-green-600">real estate portfolio.</span>
        </h1>
        <p className="text-lg text-gray-500 max-w-xl mx-auto mb-10">
          Replace fragmented spreadsheets with a unified AI-assisted workspace for agents,
          tenants, and maintenance teams.
        </p>
        <div className="flex justify-center gap-3">
          <Link
            to="/contact"
            className="bg-gray-900 text-white text-sm font-semibold px-6 py-3 rounded-md hover:bg-gray-700 transition-colors"
          >
            Request Demo
          </Link>
          <Link
            to="/solutions"
            className="border border-gray-300 text-gray-700 text-sm font-semibold px-6 py-3 rounded-md hover:border-gray-400 transition-colors"
          >
            View Modules
          </Link>
        </div>
      </section>

      {/* Persona cards */}
      <section className="max-w-6xl mx-auto px-6 pb-24">
        <p className="text-xs font-semibold uppercase tracking-widest text-gray-400 text-center mb-8">
          One platform, four experiences
        </p>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {personas.map((p) => (
            <div key={p.title} className="bg-gray-50 rounded-xl p-6 border border-gray-100">
              <span
                className={`inline-flex items-center justify-center w-8 h-8 rounded-full text-white text-sm font-bold mb-4 ${p.color}`}
              >
                {p.letter}
              </span>
              <h3 className="text-sm font-semibold text-gray-900 mb-2">{p.title}</h3>
              <p className="text-sm text-gray-500 leading-relaxed">{p.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Portfolio module highlight */}
      <section className="max-w-6xl mx-auto px-6 pb-24">
        <div className="bg-gray-50 rounded-2xl p-10 md:p-14 flex flex-col md:flex-row gap-12 items-center border border-gray-100">
          <div className="flex-1">
            <h2 className="text-3xl font-extrabold text-gray-900 mb-2">
              Master your units with the{' '}
              <span className="text-green-600">Portfolio Module.</span>
            </h2>
            <p className="text-gray-500 mb-6 text-sm leading-relaxed">
              Unified asset tracking with AI-powered occupancy forecasting and document storage.
            </p>
            <ul className="space-y-2 text-sm text-gray-700">
              <li className="flex items-center gap-2">
                <span className="text-green-600">→</span> Smart Asset Registry
              </li>
              <li className="flex items-center gap-2">
                <span className="text-green-600">→</span> Automated Compliance Alerts
              </li>
              <li className="flex items-center gap-2">
                <span className="text-green-600">→</span> Portfolio Performance Analytics
              </li>
            </ul>
          </div>
          {/* Mock dashboard */}
          <div className="flex-1 bg-white rounded-xl border border-gray-200 shadow-sm p-6 min-h-[220px] flex flex-col gap-4">
            <div className="flex items-center justify-between">
              <p className="text-xs font-semibold text-gray-400 uppercase tracking-widest">Balance</p>
              <div className="flex gap-2">
                <div className="w-4 h-4 rounded-full bg-gray-200" />
                <div className="w-4 h-4 rounded-full bg-gray-200" />
              </div>
            </div>
            <div className="flex gap-4">
              <div className="flex-1 bg-gray-50 rounded-lg p-4">
                <p className="text-xs text-gray-400 mb-1">Units</p>
                <p className="text-xl font-bold text-gray-900">148</p>
              </div>
              <div className="flex-1 bg-gray-50 rounded-lg p-4">
                <p className="text-xs text-gray-400 mb-1">Occupied</p>
                <p className="text-xl font-bold text-green-600">96%</p>
              </div>
            </div>
            {/* Mini bar chart */}
            <div className="flex items-end gap-1 h-14">
              {[40, 60, 45, 80, 65, 90, 75, 88, 70, 95].map((h, i) => (
                <div
                  key={i}
                  className="flex-1 bg-green-100 rounded-sm"
                  style={{ height: `${h}%` }}
                />
              ))}
            </div>
            <div className="flex gap-4 pt-1">
              <div className="text-xs text-gray-500">
                <span className="font-semibold text-gray-900">1.9%</span> vs last month
              </div>
              <div className="text-xs text-gray-500">
                <span className="font-semibold text-gray-900">2.3%</span> vs last year
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Three module cards */}
      <section className="max-w-6xl mx-auto px-6 pb-24">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {modules.map((m) => (
            <div key={m.title} className="border border-gray-100 rounded-xl p-7 hover:border-gray-300 transition-colors">
              <h3 className="text-sm font-semibold text-gray-900 mb-2">{m.title}</h3>
              <p className="text-sm text-gray-500 leading-relaxed">{m.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Dark deploy section */}
      <section className="bg-gray-900 text-white">
        <div className="max-w-6xl mx-auto px-6 py-20">
          <div className="flex flex-col md:flex-row gap-16">
            <div className="flex-1">
              <h2 className="text-3xl font-extrabold mb-4">Ready to deploy in days, not months.</h2>
              <p className="text-gray-400 text-sm leading-relaxed mb-8">
                Our onboarding specialists migrate your data for free, ensuring you never miss a payment.
              </p>
              <div className="flex gap-8">
                <div>
                  <p className="text-3xl font-extrabold text-green-400">12k+</p>
                  <p className="text-xs text-gray-500 mt-1 uppercase tracking-wider">Units managed</p>
                </div>
                <div>
                  <p className="text-3xl font-extrabold text-green-400">99%</p>
                  <p className="text-xs text-gray-500 mt-1 uppercase tracking-wider">Collection rate</p>
                </div>
              </div>
            </div>
            <div className="flex-1 grid grid-cols-1 gap-8">
              {steps.map((s) => (
                <div key={s.number} className="flex gap-4">
                  <span className="text-xs font-bold text-green-400 mt-0.5">{s.number}</span>
                  <div>
                    <p className="text-sm font-semibold mb-1">{s.title}</p>
                    <p className="text-sm text-gray-400 leading-relaxed">{s.desc}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* Final CTA */}
      <section className="max-w-6xl mx-auto px-6 py-24 text-center">
        <div className="bg-gray-50 rounded-2xl border border-gray-100 py-16 px-8">
          <h2 className="text-3xl font-extrabold text-gray-900 mb-6">Modernize your management today.</h2>
          <Link
            to="/contact"
            className="inline-block bg-gray-900 text-white text-sm font-semibold px-8 py-3 rounded-md hover:bg-gray-700 transition-colors"
          >
            Start My Free Trial
          </Link>
          <p className="text-xs text-gray-400 mt-4">No credit card required. 14-day free trial on all plans.</p>
        </div>
      </section>
    </>
  )
}
