import api from './api'

export type Building = {
  id: string
  agencyId: string
  clientId?: string
  name?: string
  address: string
  suburb: string
  state: string
  postcode: string
  country: string
  buildingType: string
  yearBuilt?: number
  notes?: string
  isActive: boolean
  createdAt: string
  updatedAt: string
  unitCount: number
}

export type CreateBuildingInput = {
  name?: string
  address: string
  suburb: string
  state: string
  postcode: string
  country?: string
  notes?: string
}

type PagedResponse<T> = { data: T[]; meta: { total: number; page: number; limit: number; pages: number } }

export const fetchBuildings = (): Promise<Building[]> =>
  api.get<PagedResponse<Building>>('/buildings').then((r) => r.data.data)

export const createBuilding = (data: CreateBuildingInput): Promise<Building> =>
  api.post<{ data: Building }>('/buildings', data).then((r) => r.data.data)

export const updateBuilding = (id: string, data: Partial<CreateBuildingInput>): Promise<Building> =>
  api.put<{ data: Building }>(`/buildings/${id}`, data).then((r) => r.data.data)

export const deleteBuilding = (id: string): Promise<void> =>
  api.delete(`/buildings/${id}`).then(() => undefined)
