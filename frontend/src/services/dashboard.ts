import api from './api'

export type DashboardStats = {
  buildings: number
  tenants: number
  payments: number
  openMaintenance: number
}

export const fetchDashboardStats = (): Promise<DashboardStats> =>
  api.get<DashboardStats>('/dashboard/stats').then((r) => r.data)
