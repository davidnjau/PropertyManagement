import { Link } from 'react-router-dom'

const plans = [
  {
    name: 'Starter',
    price: 'Free',
    sub: 'up to 10 units',
    features: ['Portfolio Manager', 'Tenancy Manager', 'Payment Ledger', 'Email support'],
    cta: 'Start free',
    ctaStyle: 'bg-gray-900 text-white hover:bg-gray-700',
    highlight: false,
  },
  {
    name: 'Agency',
    price: '$2',
    sub: 'per unit / month',
    features: [
      'Everything in Starter',
      'Maintenance Hub',
      'Client Portal',
      'Contractor accounts',
      'Priority support',
    ],
    cta: 'Start trial',
    ctaStyle: 'bg-green-500 text-white hover:bg-green-600',
    highlight: true,
  },
  {
    name: 'Enterprise',
    price: 'Custom',
    sub: '500+ units',
    features: [
      'Everything in Agency',
      'SSO & SCIM',
      'White-label client portal',
      'Dedicated CSM',
      'Custom SLAs',
    ],
    cta: 'Contact sales',
    ctaStyle: 'bg-gray-900 text-white hover:bg-gray-700',
    highlight: false,
  },
]

export default function Pricing() {
  return (
    <>
      {/* Hero */}
      <section className="max-w-6xl mx-auto px-6 pt-24 pb-16 text-center">
        <p className="text-xs font-semibold uppercase tracking-widest text-gray-400 mb-6">
          Pricing
        </p>
        <h1 className="text-5xl md:text-6xl font-extrabold text-gray-900 leading-tight mb-4">
          Pay for the units you
          <br />
          manage.
        </h1>
        <p className="text-lg text-gray-500">
          No per-seat fees. No surprise upgrades. Cancel anytime.
        </p>
      </section>

      {/* Pricing cards */}
      <section className="max-w-4xl mx-auto px-6 pb-16">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 items-start">
          {plans.map((p) => (
            <div
              key={p.name}
              className={`rounded-2xl p-7 flex flex-col gap-5 ${
                p.highlight
                  ? 'bg-gray-900 text-white'
                  : 'bg-white border border-gray-100'
              }`}
            >
              <div>
                <p className={`text-xs font-semibold uppercase tracking-widest mb-3 ${p.highlight ? 'text-gray-400' : 'text-gray-400'}`}>
                  {p.name}
                </p>
                <p className={`text-4xl font-extrabold ${p.highlight ? 'text-white' : 'text-gray-900'}`}>
                  {p.price}
                </p>
                <p className={`text-xs mt-1 ${p.highlight ? 'text-gray-400' : 'text-gray-500'}`}>
                  {p.sub}
                </p>
              </div>

              <ul className="space-y-2 flex-1">
                {p.features.map((f) => (
                  <li key={f} className={`text-sm flex items-center gap-2 ${p.highlight ? 'text-gray-300' : 'text-gray-600'}`}>
                    <span className={p.highlight ? 'text-green-400' : 'text-green-600'}>—</span>
                    {f}
                  </li>
                ))}
              </ul>

              <Link
                to="/contact"
                className={`text-sm font-semibold text-center py-2.5 rounded-md transition-colors ${p.ctaStyle}`}
              >
                {p.cta}
              </Link>
            </div>
          ))}
        </div>
      </section>

      {/* Audit trail note */}
      <section className="max-w-4xl mx-auto px-6 pb-24">
        <div className="bg-gray-50 border border-gray-100 rounded-2xl px-10 py-10 text-center">
          <h2 className="text-lg font-extrabold text-gray-900 mb-2">Every plan includes a full audit trail.</h2>
          <p className="text-sm text-gray-500 max-w-xl mx-auto">
            Role-based access for Agent, Client, Tenant, and Admin. Document storage. Maintenance lifecycle. No feature-gating on the basics.
          </p>
        </div>
      </section>
    </>
  )
}
