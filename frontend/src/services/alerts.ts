import api from './api'

export type SentAlert = {
  id: string
  sentAt: string
  target: string
  channels: string
  subject: string
  message: string
  recipients: number
  status: 'Sent' | 'Failed'
}

export type CreateAlertInput = {
  target: string
  building?: string
  tenants?: string[]
  rentDueFilter?: string
  channels: string[]
  subject: string
  message: string
}

export const fetchAlerts = (): Promise<SentAlert[]> =>
  api.get<SentAlert[]>('/admin/alerts').then((r) => r.data)

export const createAlert = (data: CreateAlertInput): Promise<SentAlert> =>
  api.post<SentAlert>('/admin/alerts', data).then((r) => r.data)
