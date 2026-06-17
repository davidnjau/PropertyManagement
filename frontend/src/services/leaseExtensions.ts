import api from './api'

export type LeaseExtensionRequest = {
  id: string
  agencyId: string
  leaseId: string
  tenantId: string
  currentEndDate: string
  proposedEndDate: string
  durationMonths?: number
  customEndDate?: string
  notes?: string
  status: string
  submittedAt: string
  resolvedAt?: string
  resolvedBy?: string
  agentNotes?: string
}

export type CreateLeaseExtensionInput = {
  leaseId: string
  durationMonths?: number
  customEndDate?: string
  notes?: string
}

export const fetchLeaseExtensions = (): Promise<LeaseExtensionRequest[]> =>
  api.get<{ data: LeaseExtensionRequest[] }>('/admin/lease-extension-requests').then((r) => r.data.data)

export const resolveLeaseExtension = (id: string, status: string, agentNotes?: string): Promise<LeaseExtensionRequest> =>
  api.patch<{ data: LeaseExtensionRequest }>(`/admin/lease-extension-requests/${id}`, { status, agentNotes }).then((r) => r.data.data)
