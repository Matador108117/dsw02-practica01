import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Empleado {
  clave: string;
  prefijo: string;
  consecutivo: number;
  nombre: string;
  direccion: string;
  telefono: string;
  correoElectronico: string;
  rol: 'USER' | 'ADMIN';
  activo: boolean;
  departamentoId: string | null;
}

export interface EmpleadoPageResponse {
  content: Empleado[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface EmpleadoCreateRequest {
  nombre: string;
  direccion: string;
  telefono: string;
  correoElectronico: string;
  contrasena: string;
  rol: 'USER' | 'ADMIN';
  departamentoId?: string;
}

export interface EmpleadoUpdateRequest {
  nombre?: string;
  direccion?: string;
  telefono?: string;
  correoElectronico?: string;
  contrasena?: string;
  rol?: 'USER' | 'ADMIN';
  activo?: boolean;
  departamentoId?: string;
}

@Injectable({ providedIn: 'root' })
export class EmpleadosApiService {
  constructor(private readonly http: HttpClient) {}

  list(page = 0, size = 20): Observable<EmpleadoPageResponse> {
    return this.http.get<EmpleadoPageResponse>(`/api/v3/empleados?page=${page}&size=${size}`, {
      withCredentials: true
    });
  }

  findByClave(clave: string): Observable<Empleado> {
    return this.http.get<Empleado>(`/api/v3/empleados/${encodeURIComponent(clave)}`, {
      withCredentials: true
    });
  }

  create(payload: EmpleadoCreateRequest): Observable<Empleado> {
    return this.http.post<Empleado>('/api/v3/empleados', payload, {
      withCredentials: true
    });
  }

  update(clave: string, payload: EmpleadoUpdateRequest): Observable<Empleado> {
    return this.http.put<Empleado>(`/api/v3/empleados/${encodeURIComponent(clave)}`, payload, {
      withCredentials: true
    });
  }

  delete(clave: string): Observable<void> {
    return this.http.delete<void>(`/api/v3/empleados/${encodeURIComponent(clave)}`, {
      withCredentials: true
    });
  }

  listByDepartamento(departamentoId: string, page = 0, size = 20): Observable<EmpleadoPageResponse> {
    return this.http.get<EmpleadoPageResponse>(
      `/api/v3/departamentos/${encodeURIComponent(departamentoId)}/empleados?page=${page}&size=${size}`,
      { withCredentials: true }
    );
  }
}
