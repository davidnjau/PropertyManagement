import api from './api'

export type LeaseExtensionRequest = {
  id: string
  tenantId: string
  duration: string
  customDate?: string
  notes: string
  status: 'Pending' | 'Approved' | 'Rejected'
  createdAt: string
}

export type CreateLeaseExtensionInput = {
  duration: string
  customDate?: string
  notes: string
}

export const fetchLeaseExtensions = (): Promise<LeaseExtensionRequest[]> =>
  api.get<LeaseExtensionRequest[]>('/admin/lease-extension-requests').then((r) => r.data)

export const createLeaseExtension = (data: CreateLeaseExtensionInput): Promise<LeaseExtensionRequest> =>
  api.post<LeaseExtensionRequest>('/admin/lease-extension-requests', data).then((r) => r.data)
