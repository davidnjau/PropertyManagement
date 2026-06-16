import api from './api'

export type MaintenanceRequest = {
  id: string
  title: string
  description: string
  priority: string
  building: string
}

export type CreateMaintenanceInput = Omit<MaintenanceRequest, 'id'>

export const fetchMaintenance = (): Promise<MaintenanceRequest[]> =>
  api.get<MaintenanceRequest[]>('/maintenance-requests').then((r) => r.data)

export const createMaintenanceRequest = (data: CreateMaintenanceInput): Promise<MaintenanceRequest> =>
  api.post<MaintenanceRequest>('/maintenance-requests', data).then((r) => r.data)
