import { usePaymentMethods } from '../../../context/PaymentMethodsContext'

function Toggle({ enabled, onToggle }: { enabled: boolean; onToggle: () => void }) {
  return (
    <button
      type="button"
      onClick={onToggle}
      className={`relative inline-flex h-5 w-9 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors focus:outline-none ${
        enabled ? 'bg-gray-900' : 'bg-gray-200'
      }`}
    >
      <span
        className={`pointer-events-none inline-block h-4 w-4 rounded-full bg-white shadow transition-transform ${
          enabled ? 'translate-x-4' : 'translate-x-0'
        }`}
      />
    </button>
  )
}

export default function PaymentMethodsAdmin() {
  const { methods, toggleMethod, toggleBank } = usePaymentMethods()
  const bankTransfer = methods.find((m) => m.id === 'bank_transfer')

  return (
    <div className="max-w-2xl">
      <h1 className="text-2xl font-bold text-gray-900 mb-1">Payment Methods</h1>
      <p className="text-sm text-gray-500 mb-8">
        Configure which payment channels are available when logging tenant payments.
      </p>

      {/* Methods */}
      <div className="border border-gray-100 rounded-xl overflow-hidden mb-6">
        <div className="border-b border-gray-100 px-5 py-3 bg-gray-50">
          <p className="text-xs font-semibold uppercase tracking-widest text-gray-500">Channels</p>
        </div>
        <div className="divide-y divide-gray-50">
          {methods.map((m) => (
            <div key={m.id} className="flex items-center justify-between px-5 py-4">
              <div>
                <p className="text-sm font-medium text-gray-900">{m.label}</p>
                {m.id === 'bank_transfer' && (
                  <p className="text-xs text-gray-400 mt-0.5">
                    {m.banks?.filter((b) => b.enabled).length ?? 0} of {m.banks?.length ?? 0} banks enabled
                  </p>
                )}
              </div>
              <Toggle enabled={m.enabled} onToggle={() => toggleMethod(m.id)} />
            </div>
          ))}
        </div>
      </div>

      {/* Bank list — only shown when Bank Transfer is enabled */}
      {bankTransfer?.enabled && (
        <div className="border border-gray-100 rounded-xl overflow-hidden">
          <div className="border-b border-gray-100 px-5 py-3 bg-gray-50 flex items-center justify-between">
            <p className="text-xs font-semibold uppercase tracking-widest text-gray-500">
              Kenyan Banks
            </p>
            <p className="text-xs text-gray-400">
              {bankTransfer.banks?.filter((b) => b.enabled).length} enabled
            </p>
          </div>
          <div className="divide-y divide-gray-50 max-h-[420px] overflow-y-auto">
            {bankTransfer.banks?.map((bank) => (
              <div key={bank.id} className="flex items-center justify-between px-5 py-3">
                <p className={`text-sm ${bank.enabled ? 'text-gray-900' : 'text-gray-400'}`}>
                  {bank.name}
                </p>
                <Toggle enabled={bank.enabled} onToggle={() => toggleBank(bank.id)} />
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
