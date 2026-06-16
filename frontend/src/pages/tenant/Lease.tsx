export default function TenantLease() {
  const lease = {
    unit: 'Unit 4B',
    building: 'Westlands Heights',
    floor: '4th Floor',
    startDate: '1 Jan 2026',
    endDate: '31 Dec 2026',
    monthlyRent: 'KES 45,000',
    deposit: 'KES 90,000',
    status: 'Active',
  }

  return (
    <div className="max-w-2xl">
      <h1 className="text-2xl font-bold text-gray-900 mb-1">My Lease</h1>
      <p className="text-sm text-gray-500 mb-8">Your current tenancy agreement details.</p>

      <div className="border border-gray-100 rounded-2xl overflow-hidden mb-6">
        <div className="border-b border-gray-100 px-6 py-4 flex items-center justify-between bg-gray-50">
          <p className="text-sm font-semibold text-gray-900">{lease.unit} — {lease.building}</p>
          <span className="text-xs font-semibold bg-green-50 text-green-700 px-2.5 py-1 rounded-full">
            {lease.status}
          </span>
        </div>

        <dl className="divide-y divide-gray-50">
          {[
            { label: 'Building', value: lease.building },
            { label: 'Unit', value: lease.unit },
            { label: 'Floor', value: lease.floor },
            { label: 'Lease start', value: lease.startDate },
            { label: 'Lease end', value: lease.endDate },
            { label: 'Monthly rent', value: lease.monthlyRent },
            { label: 'Security deposit', value: lease.deposit },
          ].map(({ label, value }) => (
            <div key={label} className="flex items-center justify-between px-6 py-3.5">
              <dt className="text-sm text-gray-500">{label}</dt>
              <dd className="text-sm font-medium text-gray-900">{value}</dd>
            </div>
          ))}
        </dl>
      </div>

      <p className="text-xs text-gray-400">
        To request a lease amendment or renewal, contact your agent directly.
      </p>
    </div>
  )
}
