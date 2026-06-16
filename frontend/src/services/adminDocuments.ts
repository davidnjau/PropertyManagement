import api from './api'

export type AdminDocument = {
  id: string
  fileName: string
  fileSize: string
  docType: string
  target: string
  entityType: 'tenant' | 'building'
  notes: string
  uploadedAt: string
}

export const fetchAdminDocuments = (entityType: 'tenant' | 'building'): Promise<AdminDocument[]> =>
  api.get<AdminDocument[]>('/admin/documents', { params: { entityType } }).then((r) => r.data)

export const uploadAdminDocument = (formData: FormData): Promise<AdminDocument> =>
  api
    .post<AdminDocument>('/admin/documents', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    .then((r) => r.data)

export const updateAdminDocument = (id: string, data: Partial<AdminDocument>): Promise<AdminDocument> =>
  api.patch<AdminDocument>(`/admin/documents/${id}`, data).then((r) => r.data)

export const deleteAdminDocument = (id: string): Promise<void> =>
  api.delete(`/admin/documents/${id}`).then(() => undefined)
