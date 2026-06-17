import api from './api'

export type SentAlert = {
  id: string
  agencyId: string
  sentBy: string
  targetType: string
  targetLabel: string
  buildingId?: string
  rentDueFilter?: string
  channels: string[]
  subject: string
  message: string
  recipientCount: number
  status: string
  failureReason?: string
  sentAt: string
}

export type CreateAlertInput = {
  targetType: string
  buildingId?: string
  tenantIds?: string[]
  rentDueFilter?: string
  channels: string[]
  subject: string
  message: string
}

export const fetchAlerts = (): Promise<SentAlert[]> =>
  api.get<{ data: SentAlert[] }>('/admin/alerts').then((r) => r.data.data)

export const createAlert = (data: CreateAlertInput): Promise<SentAlert> =>
  api.post<{ data: SentAlert }>('/admin/alerts', data).then((r) => r.data.data)
