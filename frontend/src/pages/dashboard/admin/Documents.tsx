import { useState, useRef } from 'react'
import { Upload, FileText, X, CheckCircle, Building, Users } from 'lucide-react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { fetchAdminDocuments, uploadAdminDocument } from '../../../services/adminDocuments'

type Tab = 'tenant' | 'building'

const MOCK_TENANTS = [
  'Alice Mwangi', 'Brian Otieno', 'Carol Wanjiku', 'David Kamau',
  'Esther Njeri', 'Francis Oloo', 'Grace Akinyi', 'Hassan Omar',
]

const MOCK_BUILDINGS = [
  'Westlands Heights', 'Kilimani Court', 'Lavington Gardens', 'Karen View Estate',
]

const TENANT_DOC_TYPES = [
  'National ID',
  'Passport',
  'Driving License',
  'KRA PIN Certificate',
  'Lease Agreement',
  'Employment Letter',
  'Bank Statement',
  'Move-in Inspection Report',
  'Reference Letter',
  'Utility Bill',
  'Other',
]

const BUILDING_DOC_TYPES = [
  'Title Deed',
  'Building Plan / Blueprint',
  'Occupation Certificate',
  'Insurance Certificate',
  'Compliance Certificate',
  'Valuation Report',
  'Land Rates Receipt',
  'Utility Agreement',
  'Other',
]

function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(0)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

const emptyTenantForm = { tenant: '', docType: '', notes: '' }
const emptyBuildingForm = { building: '', docType: '', notes: '' }

export default function AdminDocuments() {
  const [tab, setTab] = useState<Tab>('tenant')
  const qc = useQueryClient()

  const { data: docs = [], isLoading: docsLoading } = useQuery({
    queryKey: ['admin-documents', tab],
    queryFn: () => fetchAdminDocuments(tab),
  })

  const mutation = useMutation({
    mutationFn: uploadAdminDocument,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin-documents', tab] }),
  })

  // Tenant upload state
  const [tenantForm, setTenantForm] = useState(emptyTenantForm)
  const [tenantFile, setTenantFile] = useState<File | null>(null)
  const [tenantDragging, setTenantDragging] = useState(false)
  const tenantInputRef = useRef<HTMLInputElement>(null)

  // Building upload state
  const [buildingForm, setBuildingForm] = useState(emptyBuildingForm)
  const [buildingFile, setBuildingFile] = useState<File | null>(null)
  const [buildingDragging, setBuildingDragging] = useState(false)
  const buildingInputRef = useRef<HTMLInputElement>(null)

  const [success, setSuccess] = useState(false)

  function flashSuccess() {
    setSuccess(true)
    setTimeout(() => setSuccess(false), 4000)
  }

  function handleTenantChange(e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) {
    setTenantForm({ ...tenantForm, [e.target.name]: e.target.value })
  }

  function handleTenantFile(files: FileList | null) {
    if (files?.[0]) setTenantFile(files[0])
  }

  function handleTenantSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!tenantFile) return
    const fd = new FormData()
    fd.append('file', tenantFile)
    fd.append('docType', tenantForm.docType)
    fd.append('target', tenantForm.tenant)
    fd.append('entityType', 'tenant')
    fd.append('notes', tenantForm.notes)
    mutation.mutate(fd, {
      onSuccess: () => {
        setTenantForm(emptyTenantForm)
        setTenantFile(null)
        flashSuccess()
      },
    })
  }

  function handleBuildingChange(e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) {
    setBuildingForm({ ...buildingForm, [e.target.name]: e.target.value })
  }

  function handleBuildingFile(files: FileList | null) {
    if (files?.[0]) setBuildingFile(files[0])
  }

  function handleBuildingSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!buildingFile) return
    const fd = new FormData()
    fd.append('file', buildingFile)
    fd.append('docType', buildingForm.docType)
    fd.append('target', buildingForm.building)
    fd.append('entityType', 'building')
    fd.append('notes', buildingForm.notes)
    mutation.mutate(fd, {
      onSuccess: () => {
        setBuildingForm(emptyBuildingForm)
        setBuildingFile(null)
        flashSuccess()
      },
    })
  }

  const tenantCanSave = tenantForm.tenant && tenantForm.docType && tenantFile
  const buildingCanSave = buildingForm.building && buildingForm.docType && buildingFile

  return (
    <div className="w-full">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-1">Documents</h1>
        <p className="text-sm text-gray-500">Upload and manage documents for tenants and buildings.</p>
      </div>

      {success && (
        <div className="flex items-center gap-3 bg-green-50 border border-green-100 rounded-xl px-5 py-4 mb-6">
          <CheckCircle size={16} className="text-green-600 shrink-0" />
          <p className="text-sm text-green-800 font-medium">Document uploaded successfully.</p>
        </div>
      )}

      {/* Tab switcher */}
      <div className="flex bg-gray-100 rounded-lg p-1 mb-6 w-fit">
        {([
          { id: 'tenant', label: 'Tenant Documents', icon: <Users size={14} /> },
          { id: 'building', label: 'Building Documents', icon: <Building size={14} /> },
        ] as { id: Tab; label: string; icon: React.ReactNode }[]).map((t) => (
          <button
            key={t.id}
            onClick={() => setTab(t.id)}
            className={`flex items-center gap-2 px-4 py-2 rounded-md text-sm font-medium transition-colors ${
              tab === t.id ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-500 hover:text-gray-700'
            }`}
          >
            {t.icon}
            {t.label}
          </button>
        ))}
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-5 gap-6">

        {/* Upload form */}
        <div className="xl:col-span-2 border border-gray-100 rounded-2xl overflow-hidden">
          <div className="border-b border-gray-100 px-6 py-4 bg-gray-50">
            <p className="text-sm font-semibold text-gray-900">
              Upload {tab === 'tenant' ? 'tenant' : 'building'} document
            </p>
          </div>

          {tab === 'tenant' ? (
            <form onSubmit={handleTenantSubmit} className="p-6 space-y-5">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Tenant</label>
                <select name="tenant" value={tenantForm.tenant} onChange={handleTenantChange}
                  className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 bg-white">
                  <option value="">Select tenant</option>
                  {MOCK_TENANTS.map((t) => <option key={t}>{t}</option>)}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Document type</label>
                <select name="docType" value={tenantForm.docType} onChange={handleTenantChange}
                  className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 bg-white">
                  <option value="">Select type</option>
                  {TENANT_DOC_TYPES.map((t) => <option key={t}>{t}</option>)}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">File</label>
                <div
                  onDragOver={(e) => { e.preventDefault(); setTenantDragging(true) }}
                  onDragLeave={() => setTenantDragging(false)}
                  onDrop={(e) => { e.preventDefault(); setTenantDragging(false); handleTenantFile(e.dataTransfer.files) }}
                  onClick={() => tenantInputRef.current?.click()}
                  className={`border-2 border-dashed rounded-xl px-4 py-8 text-center cursor-pointer transition-colors ${
                    tenantDragging ? 'border-gray-400 bg-gray-50' : 'border-gray-200 hover:border-gray-400 hover:bg-gray-50'
                  }`}
                >
                  {tenantFile ? (
                    <div className="flex items-center justify-between bg-gray-50 border border-gray-100 rounded-md px-3 py-2.5">
                      <div className="flex items-center gap-2 min-w-0">
                        <FileText size={14} className="text-gray-400 shrink-0" />
                        <span className="text-sm text-gray-700 truncate">{tenantFile.name}</span>
                        <span className="text-xs text-gray-400 shrink-0">{formatBytes(tenantFile.size)}</span>
                      </div>
                      <button type="button" onClick={(e) => { e.stopPropagation(); setTenantFile(null) }}
                        className="text-gray-400 hover:text-gray-700 ml-2 shrink-0">
                        <X size={14} />
                      </button>
                    </div>
                  ) : (
                    <>
                      <Upload size={20} className="mx-auto text-gray-300 mb-2" />
                      <p className="text-sm text-gray-500">Click or drag & drop</p>
                      <p className="text-xs text-gray-400 mt-1">PDF, DOC, JPG, PNG — up to 10 MB</p>
                    </>
                  )}
                  <input ref={tenantInputRef} type="file" className="hidden"
                    accept=".pdf,.doc,.docx,.jpg,.jpeg,.png"
                    onChange={(e) => handleTenantFile(e.target.files)} />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">
                  Notes <span className="text-gray-400 font-normal">(optional)</span>
                </label>
                <textarea name="notes" value={tenantForm.notes} onChange={handleTenantChange} rows={2}
                  placeholder="e.g. ID expires Dec 2028"
                  className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 resize-none placeholder:text-gray-300" />
              </div>

              <button type="submit" disabled={!tenantCanSave || mutation.isPending}
                className="w-full flex items-center justify-center gap-2 bg-gray-900 text-white text-sm font-semibold py-3 rounded-md hover:bg-gray-700 transition-colors disabled:opacity-40 disabled:cursor-not-allowed">
                <Upload size={14} />
                {mutation.isPending ? 'Uploading…' : 'Upload document'}
              </button>
            </form>
          ) : (
            <form onSubmit={handleBuildingSubmit} className="p-6 space-y-5">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Building</label>
                <select name="building" value={buildingForm.building} onChange={handleBuildingChange}
                  className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 bg-white">
                  <option value="">Select building</option>
                  {MOCK_BUILDINGS.map((b) => <option key={b}>{b}</option>)}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Document type</label>
                <select name="docType" value={buildingForm.docType} onChange={handleBuildingChange}
                  className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 bg-white">
                  <option value="">Select type</option>
                  {BUILDING_DOC_TYPES.map((t) => <option key={t}>{t}</option>)}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">File</label>
                <div
                  onDragOver={(e) => { e.preventDefault(); setBuildingDragging(true) }}
                  onDragLeave={() => setBuildingDragging(false)}
                  onDrop={(e) => { e.preventDefault(); setBuildingDragging(false); handleBuildingFile(e.dataTransfer.files) }}
                  onClick={() => buildingInputRef.current?.click()}
                  className={`border-2 border-dashed rounded-xl px-4 py-8 text-center cursor-pointer transition-colors ${
                    buildingDragging ? 'border-gray-400 bg-gray-50' : 'border-gray-200 hover:border-gray-400 hover:bg-gray-50'
                  }`}
                >
                  {buildingFile ? (
                    <div className="flex items-center justify-between bg-gray-50 border border-gray-100 rounded-md px-3 py-2.5">
                      <div className="flex items-center gap-2 min-w-0">
                        <FileText size={14} className="text-gray-400 shrink-0" />
                        <span className="text-sm text-gray-700 truncate">{buildingFile.name}</span>
                        <span className="text-xs text-gray-400 shrink-0">{formatBytes(buildingFile.size)}</span>
                      </div>
                      <button type="button" onClick={(e) => { e.stopPropagation(); setBuildingFile(null) }}
                        className="text-gray-400 hover:text-gray-700 ml-2 shrink-0">
                        <X size={14} />
                      </button>
                    </div>
                  ) : (
                    <>
                      <Upload size={20} className="mx-auto text-gray-300 mb-2" />
                      <p className="text-sm text-gray-500">Click or drag & drop</p>
                      <p className="text-xs text-gray-400 mt-1">PDF, DOC, JPG, PNG — up to 10 MB</p>
                    </>
                  )}
                  <input ref={buildingInputRef} type="file" className="hidden"
                    accept=".pdf,.doc,.docx,.jpg,.jpeg,.png"
                    onChange={(e) => handleBuildingFile(e.target.files)} />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">
                  Notes <span className="text-gray-400 font-normal">(optional)</span>
                </label>
                <textarea name="notes" value={buildingForm.notes} onChange={handleBuildingChange} rows={2}
                  placeholder="e.g. Title deed for Block A"
                  className="w-full border border-gray-200 rounded-md px-3 py-2.5 text-sm focus:outline-none focus:border-gray-400 resize-none placeholder:text-gray-300" />
              </div>

              <button type="submit" disabled={!buildingCanSave || mutation.isPending}
                className="w-full flex items-center justify-center gap-2 bg-gray-900 text-white text-sm font-semibold py-3 rounded-md hover:bg-gray-700 transition-colors disabled:opacity-40 disabled:cursor-not-allowed">
                <Upload size={14} />
                {mutation.isPending ? 'Uploading…' : 'Upload document'}
              </button>
            </form>
          )}
        </div>

        {/* Document list */}
        <div className="xl:col-span-3 border border-gray-100 rounded-2xl overflow-hidden">
          <div className="border-b border-gray-100 px-6 py-4 bg-gray-50 flex items-center justify-between">
            <p className="text-sm font-semibold text-gray-900">
              Uploaded {tab === 'tenant' ? 'tenant' : 'building'} documents
            </p>
            <p className="text-xs text-gray-400">{docs.length} file{docs.length !== 1 ? 's' : ''}</p>
          </div>

          {docsLoading ? (
            <div className="flex flex-col items-center justify-center py-20 text-center px-6">
              <p className="text-sm text-gray-400">Loading...</p>
            </div>
          ) : docs.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-20 text-center px-6">
              <FileText size={32} className="text-gray-200 mb-4" />
              <p className="text-sm text-gray-400">No documents uploaded yet.</p>
              <p className="text-xs text-gray-300 mt-1">Use the form on the left to upload one.</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-gray-100 bg-gray-50">
                    <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                      {tab === 'tenant' ? 'Tenant' : 'Building'}
                    </th>
                    <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Type</th>
                    <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">File</th>
                    <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Notes</th>
                    <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Uploaded</th>
                  </tr>
                </thead>
                <tbody>
                  {docs.map((d) => (
                    <tr key={d.id} className="border-b border-gray-50 hover:bg-gray-50">
                      <td className="px-5 py-3 font-medium text-gray-900 text-xs font-mono">{d.targetId.slice(0, 8)}…</td>
                      <td className="px-5 py-3">
                        <span className="text-xs font-medium bg-gray-100 text-gray-700 px-2 py-0.5 rounded-full">
                          {d.docType}
                        </span>
                      </td>
                      <td className="px-5 py-3">
                        <div className="flex items-center gap-2">
                          <FileText size={13} className="text-gray-400 shrink-0" />
                          <div>
                            <a href={d.fileUrl} target="_blank" rel="noreferrer"
                              className="text-sm text-gray-700 truncate max-w-[160px] hover:underline block">{d.fileName}</a>
                            <p className="text-xs text-gray-400">{(d.fileSize / 1024).toFixed(0)} KB</p>
                          </div>
                        </div>
                      </td>
                      <td className="px-5 py-3 text-xs text-gray-400 max-w-[140px]">
                        <span className="truncate block">{d.notes || '—'}</span>
                      </td>
                      <td className="px-5 py-3 text-xs text-gray-400 whitespace-nowrap">{d.uploadedAt.slice(0, 10)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

      </div>
    </div>
  )
}
