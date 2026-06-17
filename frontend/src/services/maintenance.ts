import api from './api'

export type MaintenanceRequest = {
  id: string
  agencyId: string
  unitId: string
  reportedByType: string
  reportedById: string
  category: string
  priority: string
  status: string
  title: string
  description: string
  contractorName?: string
  contractorPhone?: string
  assignedDate?: string
  attendedDate?: string
  completedDate?: string
  closedDate?: string
  invoiceAmount?: number
  notes?: string
  slaTargetDate?: string
  createdAt: string
  updatedAt: string
}

export type CreateMaintenanceInput = {
  unitId: string
  category: string
  priority: string
  title: string
  description: string
}

type PagedResponse<T> = { data: T[]; meta: { total: number; page: number; limit: number; pages: number } }

export const fetchMaintenance = (): Promise<MaintenanceRequest[]> =>
  api.get<PagedResponse<MaintenanceRequest>>('/maintenance').then((r) => r.data.data)

export const createMaintenanceRequest = (data: CreateMaintenanceInput): Promise<MaintenanceRequest> =>
  api.post<{ data: MaintenanceRequest }>('/maintenance', data).then((r) => r.data.data)
