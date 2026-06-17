import api from './api'

export type Tenant = {
  id: string
  agencyId: string
  fullName: string
  email: string
  phone?: string
  dateOfBirth?: string
  idType?: string
  idReference?: string
  emergencyContactName?: string
  emergencyContactPhone?: string
  notes?: string
  isActive: boolean
  createdAt: string
  updatedAt: string
}

export type CreateTenantInput = {
  fullName: string
  email: string
  phone?: string
  dateOfBirth?: string
  idType?: string
  idReference?: string
  emergencyContactName?: string
  emergencyContactPhone?: string
  notes?: string
}

type PagedResponse<T> = { data: T[]; meta: { total: number; page: number; limit: number; pages: number } }

export const fetchTenants = (): Promise<Tenant[]> =>
  api.get<PagedResponse<Tenant>>('/tenants').then((r) => r.data.data)

export const createTenant = (data: CreateTenantInput): Promise<Tenant> =>
  api.post<{ data: Tenant }>('/tenants', data).then((r) => r.data.data)

export const updateTenant = (id: string, data: Partial<CreateTenantInput>): Promise<Tenant> =>
  api.patch<{ data: Tenant }>(`/tenants/${id}`, data).then((r) => r.data.data)

export const deleteTenant = (id: string): Promise<void> =>
  api.delete(`/tenants/${id}`).then(() => undefined)
