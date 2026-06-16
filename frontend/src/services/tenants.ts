import api from './api'

export type Tenant = {
  id: string
  fullName: string
  email: string
  phone: string
  unit: string
  building: string
  monthlyRent: number
  deposit: number
  leaseStart: string
  leaseEnd: string
}

export type CreateTenantInput = Omit<Tenant, 'id'>

export const fetchTenants = (): Promise<Tenant[]> =>
  api.get<Tenant[]>('/tenants').then((r) => r.data)

export const createTenant = (data: CreateTenantInput): Promise<Tenant> =>
  api.post<Tenant>('/tenants', data).then((r) => r.data)

export const updateTenant = (id: string, data: Partial<CreateTenantInput>): Promise<Tenant> =>
  api.put<Tenant>(`/tenants/${id}`, data).then((r) => r.data)

export const deleteTenant = (id: string): Promise<void> =>
  api.delete(`/tenants/${id}`).then(() => undefined)
