import api from './api'

export type AuthUser = { id: string; email: string; name: string; role: string }
export type AuthResponse = { token: string; user: AuthUser }

export const signin = (email: string, password: string, role: string) =>
  api.post<AuthResponse>('/auth/signin', { email, password, role }).then((r) => r.data)

export const signup = (name: string, email: string, password: string, role: string) =>
  api.post<AuthResponse>('/auth/signup', { name, email, password, role }).then((r) => r.data)
