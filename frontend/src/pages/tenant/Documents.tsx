import { useState, useRef } from 'react'
import { FileText, Download, Upload, X, CheckCircle } from 'lucide-react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { fetchTenantDocuments, uploadTenantDocument } from '../../services/tenantPortal'

function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(0)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

export default function TenantDocuments() {
  const qc = useQueryClient()

  const { data: docs = [], isLoading, isError } = useQuery({
    queryKey: ['tenant-documents'],
    queryFn: fetchTenantDocuments,
  })

  const mutation = useMutation({
    mutationFn: uploadTenantDocument,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['tenant-documents'] }),
  })

  const [showModal, setShowModal] = useState(false)
  const [files, setFiles] = useState<File[]>([])
  const [uploaded, setUploaded] = useState(false)
  const [dragging, setDragging] = useState(false)
  const inputRef = useRef<HTMLInputElement>(null)

  function handleFiles(selected: FileList | null) {
    if (!selected) return
    setFiles((prev) => [...prev, ...Array.from(selected)])
  }

  function removeFile(index: number) {
    setFiles((prev) => prev.filter((_, i) => i !== index))
  }

  function handleDrop(e: React.DragEvent) {
    e.preventDefault()
    setDragging(false)
    handleFiles(e.dataTransfer.files)
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (files.length === 0) return

    const uploadNext = (index: number) => {
      if (index >= files.length) {
        setFiles([])
        setShowModal(false)
        setUploaded(true)
        setTimeout(() => setUploaded(false), 4000)
        return
      }
      const fd = new FormData()
      fd.append('file', files[index])
      mutation.mutate(fd, {
        onSuccess: () => uploadNext(index + 1),
      })
    }
    uploadNext(0)
  }

  return (
    <div className="w-full">
      <div className="flex items-start justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 mb-1">Documents</h1>
          <p className="text-sm text-gray-500">Your lease documents and notices.</p>
        </div>
        <button
          onClick={() => setShowModal(true)}
          className="flex items-center gap-1.5 bg-gray-900 text-white text-sm font-medium px-4 py-2 rounded-md hover:bg-gray-700 transition-colors"
        >
          <Upload size={14} />
          Add document
        </button>
      </div>

      {uploaded && (
        <div className="flex items-center gap-3 bg-green-50 border border-green-100 rounded-xl px-5 py-4 mb-6">
          <CheckCircle size={16} className="text-green-600 shrink-0" />
          <p className="text-sm text-green-800 font-medium">Document(s) uploaded successfully.</p>
        </div>
      )}

      <div className="border border-gray-100 rounded-xl overflow-hidden">
        <div className="border-b border-gray-100 px-5 py-3 flex items-center justify-between">
          <p className="text-sm font-semibold text-gray-900">All documents</p>
          <p className="text-xs text-gray-400">{docs.length} file{docs.length !== 1 ? 's' : ''}</p>
        </div>
        {isLoading ? (
          <div className="px-5 py-8 text-center">
            <p className="text-sm text-gray-400">Loading...</p>
          </div>
        ) : isError ? (
          <div className="px-5 py-8 text-center">
            <p className="text-sm text-red-500">Failed to load data.</p>
          </div>
        ) : (
          <ul className="divide-y divide-gray-50">
            {docs.map((d) => (
              <li key={d.id} className="flex items-center justify-between px-5 py-4 hover:bg-gray-50 transition-colors">
                <div className="flex items-center gap-3 min-w-0">
                  <FileText size={16} className="text-gray-400 shrink-0" />
                  <div className="min-w-0">
                    <p className="text-sm font-medium text-gray-900 truncate">{d.name}</p>
                    <p className="text-xs text-gray-400">
                      {d.type} · {d.size} · {d.date}
                      {d.uploaded && (
                        <span className="ml-2 text-green-600 font-medium">Uploaded by you</span>
                      )}
                    </p>
                  </div>
                </div>
                <a
                  href={d.url ?? '#'}
                  className="text-gray-400 hover:text-gray-700 transition-colors p-1 shrink-0 ml-3"
                >
                  <Download size={15} />
                </a>
              </li>
            ))}
          </ul>
        )}
      </div>

      {/* Upload Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl w-full max-w-md shadow-xl">
            <div className="flex items-center justify-between px-7 pt-7 pb-5 border-b border-gray-100">
              <h2 className="text-lg font-bold text-gray-900">Add document</h2>
              <button onClick={() => { setShowModal(false); setFiles([]) }} className="text-gray-400 hover:text-gray-600">
                <X size={18} />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="px-7 py-6 space-y-5">
              {/* Drop zone */}
              <div
                onDragOver={(e) => { e.preventDefault(); setDragging(true) }}
                onDragLeave={() => setDragging(false)}
                onDrop={handleDrop}
                onClick={() => inputRef.current?.click()}
                className={`border-2 border-dashed rounded-xl px-6 py-10 text-center cursor-pointer transition-colors ${
                  dragging ? 'border-gray-400 bg-gray-50' : 'border-gray-200 hover:border-gray-400 hover:bg-gray-50'
                }`}
              >
                <Upload size={24} className="mx-auto text-gray-300 mb-3" />
                <p className="text-sm font-medium text-gray-700">Click to browse or drag & drop</p>
                <p className="text-xs text-gray-400 mt-1">PDF, DOC, DOCX, JPG, PNG — up to 10 MB each</p>
                <input
                  ref={inputRef}
                  type="file"
                  multiple
                  accept=".pdf,.doc,.docx,.jpg,.jpeg,.png"
                  className="hidden"
                  onChange={(e) => handleFiles(e.target.files)}
                />
              </div>

              {/* Selected files list */}
              {files.length > 0 && (
                <ul className="space-y-2">
                  {files.map((f, i) => (
                    <li key={i} className="flex items-center justify-between bg-gray-50 border border-gray-100 rounded-md px-3 py-2.5">
                      <div className="flex items-center gap-2 min-w-0">
                        <FileText size={14} className="text-gray-400 shrink-0" />
                        <span className="text-sm text-gray-700 truncate">{f.name}</span>
                        <span className="text-xs text-gray-400 shrink-0">{formatBytes(f.size)}</span>
                      </div>
                      <button type="button" onClick={() => removeFile(i)}
                        className="text-gray-400 hover:text-gray-700 ml-2 shrink-0">
                        <X size={14} />
                      </button>
                    </li>
                  ))}
                </ul>
              )}

              <div className="flex gap-3 pt-1">
                <button type="button"
                  onClick={() => { setShowModal(false); setFiles([]) }}
                  className="flex-1 border border-gray-200 text-gray-700 text-sm font-medium py-2.5 rounded-md hover:border-gray-400 transition-colors">
                  Cancel
                </button>
                <button type="submit"
                  disabled={files.length === 0 || mutation.isPending}
                  className="flex-1 bg-gray-900 text-white text-sm font-semibold py-2.5 rounded-md hover:bg-gray-700 transition-colors disabled:opacity-40 disabled:cursor-not-allowed">
                  {mutation.isPending ? 'Uploading…' : `Upload${files.length > 0 ? ` (${files.length})` : ''}`}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
