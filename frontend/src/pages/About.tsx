const stats = [
  { value: '12k+', label: 'Units managed' },
  { value: '99%', label: 'Collection rate' },
  { value: '450+', label: 'Agencies' },
  { value: '5', label: 'Core modules' },
]

const problems = [
  'No centralised view of buildings, units, or tenancy status.',
  'Payment tracking spread across bank statements, spreadsheets, and emails.',
  'Manual, error-prone rent not reconciliation each month.',
  'Clients chasing agents for updates instead of getting real-time visibility.',
  'No audit trail for maintenance work, inspections, or financial decisions.',
]

export default function About() {
  return (
    <>
      {/* Hero */}
      <section className="max-w-4xl mx-auto px-6 pt-24 pb-16 text-center">
        <p className="text-xs font-semibold uppercase tracking-widest text-gray-400 mb-6">
          About BuildAgent
        </p>
        <h1 className="text-5xl md:text-6xl font-extrabold text-gray-900 leading-tight mb-6">
          Property management
          <br />
          deserves better tools.
        </h1>
        <p className="text-gray-500 max-w-2xl mx-auto leading-relaxed">
          We're building the workspace agents wish they had — one that replaces the spreadsheet tabs, email chains, and paper inspection forms with a single, auditable system of record.
        </p>
      </section>

      {/* Vision + Problem */}
      <section className="max-w-3xl mx-auto px-6 pb-16 space-y-12">
        <div>
          <h2 className="text-lg font-extrabold text-gray-900 mb-3">Our vision</h2>
          <p className="text-sm text-gray-500 leading-relaxed">
            To deliver a modern, AI-assisted property management platform that replaces fragmented spreadsheets and paper-based workflows with a real-time, collaborative, and auditable system — enabling building agents to manage more properties with less effort.
          </p>
        </div>

        <div>
          <h2 className="text-lg font-extrabold text-gray-900 mb-4">The problem we solve</h2>
          <ul className="space-y-2">
            {problems.map((p) => (
              <li key={p} className="flex items-start gap-2 text-sm text-gray-500">
                <span className="text-green-600 mt-0.5">—</span>
                {p}
              </li>
            ))}
          </ul>
        </div>
      </section>

      {/* Stats */}
      <section className="max-w-3xl mx-auto px-6 pb-24">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
          {stats.map((s) => (
            <div key={s.label} className="text-center">
              <p className="text-3xl font-extrabold text-gray-900">{s.value}</p>
              <p className="text-xs text-gray-500 mt-1 uppercase tracking-wider">{s.label}</p>
            </div>
          ))}
        </div>
      </section>
    </>
  )
}
