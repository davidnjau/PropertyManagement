import { useState } from 'react'

export default function Contact() {
  const [form, setForm] = useState({
    fullName: '',
    workEmail: '',
    company: '',
    units: '',
    message: '',
  })

  function handleChange(e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    // form submission placeholder
  }

  return (
    <section className="max-w-6xl mx-auto px-6 pt-24 pb-24">
      <div className="flex flex-col md:flex-row gap-16">
        {/* Left */}
        <div className="flex-1 max-w-sm">
          <p className="text-xs font-semibold uppercase tracking-widest text-gray-400 mb-6">
            Contact
          </p>
          <h1 className="text-4xl font-extrabold text-gray-900 leading-tight mb-4">
            Let's talk about your portfolio.
          </h1>
          <p className="text-sm text-gray-500 leading-relaxed mb-8">
            Tell us how many units you manage today and what you're trying to fix. We'll get back within one business day.
          </p>

          <div className="space-y-5 text-sm">
            <div>
              <p className="text-xs font-semibold uppercase tracking-widest text-gray-400 mb-1">Sales</p>
              <p className="text-gray-700">sales@buildagent.example</p>
            </div>
            <div>
              <p className="text-xs font-semibold uppercase tracking-widest text-gray-400 mb-1">Support</p>
              <p className="text-gray-700">help@buildagent.example</p>
            </div>
            <div>
              <p className="text-xs font-semibold uppercase tracking-widest text-gray-400 mb-1">Office</p>
              <p className="text-gray-700">London · Berlin · Remote</p>
            </div>
          </div>
        </div>

        {/* Form */}
        <div className="flex-1 border border-gray-100 rounded-2xl p-8">
          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="block text-xs font-semibold uppercase tracking-widest text-gray-400 mb-1.5">
                Full name
              </label>
              <input
                name="fullName"
                value={form.fullName}
                onChange={handleChange}
                className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm text-gray-900 focus:outline-none focus:border-gray-400 transition-colors"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-widest text-gray-400 mb-1.5">
                Work email
              </label>
              <input
                name="workEmail"
                type="email"
                value={form.workEmail}
                onChange={handleChange}
                className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm text-gray-900 focus:outline-none focus:border-gray-400 transition-colors"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-widest text-gray-400 mb-1.5">
                Company
              </label>
              <input
                name="company"
                value={form.company}
                onChange={handleChange}
                className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm text-gray-900 focus:outline-none focus:border-gray-400 transition-colors"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-widest text-gray-400 mb-1.5">
                Units under management
              </label>
              <input
                name="units"
                value={form.units}
                onChange={handleChange}
                className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm text-gray-900 focus:outline-none focus:border-gray-400 transition-colors"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-widest text-gray-400 mb-1.5">
                How can we help?
              </label>
              <textarea
                name="message"
                rows={4}
                value={form.message}
                onChange={handleChange}
                className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm text-gray-900 focus:outline-none focus:border-gray-400 transition-colors resize-none"
              />
            </div>

            <button
              type="submit"
              className="w-full bg-gray-900 text-white text-sm font-semibold py-3 rounded-md hover:bg-gray-700 transition-colors"
            >
              Request demo
            </button>
          </form>
        </div>
      </div>
    </section>
  )
}
