import api from './api'

export type Building = {
  id: string
  name: string
  address: string
  city: string
  units: number
}

export type CreateBuildingInput = Omit<Building, 'id'>

export const fetchBuildings = (): Promise<Building[]> =>
  api.get<Building[]>('/buildings').then((r) => r.data)

export const createBuilding = (data: CreateBuildingInput): Promise<Building> =>
  api.post<Building>('/buildings', data).then((r) => r.data)

export const updateBuilding = (id: string, data: Partial<CreateBuildingInput>): Promise<Building> =>
  api.put<Building>(`/buildings/${id}`, data).then((r) => r.data)

export const deleteBuilding = (id: string): Promise<void> =>
  api.delete(`/buildings/${id}`).then(() => undefined)
