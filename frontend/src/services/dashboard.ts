import api from './api'

export type UnitStats = { total: number; occupied: number; vacant: number }

export type DashboardStats = {
  buildings: number
  units: UnitStats
  occupancyRate: number
  overduePayments: number
  expiringLeases: number
  openMaintenance: number
  slaBreached: number
}

export const fetchDashboardStats = (): Promise<DashboardStats> =>
  api.get<{ data: DashboardStats }>('/dashboard/stats').then((r) => r.data.data)
