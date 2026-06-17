import api from './api'

export type AdminDocument = {
  id: string
  agencyId: string
  targetType: string
  targetId: string
  docType: string
  fileName: string
  fileSize: number
  mimeType: string
  notes?: string
  uploadedBy: string
  fileUrl: string
  uploadedAt: string
}

export const fetchAdminDocuments = (entityType: 'tenant' | 'building'): Promise<AdminDocument[]> =>
  api.get<{ data: AdminDocument[] }>('/admin/documents', { params: { entityType } }).then((r) => r.data.data)

export const uploadAdminDocument = (formData: FormData): Promise<AdminDocument> =>
  api
    .post<{ data: AdminDocument }>('/admin/documents', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    .then((r) => r.data.data)

export const deleteAdminDocument = (id: string): Promise<void> =>
  api.delete(`/admin/documents/${id}`).then(() => undefined)
