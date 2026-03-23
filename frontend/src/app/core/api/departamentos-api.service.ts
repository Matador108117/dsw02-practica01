import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Departamento {
  id: string;
  nombre: string;
}

export interface DepartamentoPageResponse {
  content: Departamento[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface DepartamentoCreateRequest {
  nombre: string;
}

export interface DepartamentoUpdateRequest {
  nombre: string;
}

@Injectable({ providedIn: 'root' })
export class DepartamentosApiService {
  constructor(private readonly http: HttpClient) {}

  list(page = 0, size = 20): Observable<DepartamentoPageResponse> {
    return this.http.get<DepartamentoPageResponse>(`/api/v3/departamentos?page=${page}&size=${size}`, {
      withCredentials: true
    });
  }

  findById(id: string): Observable<Departamento> {
    return this.http.get<Departamento>(`/api/v3/departamentos/${encodeURIComponent(id)}`, {
      withCredentials: true
    });
  }

  create(payload: DepartamentoCreateRequest): Observable<Departamento> {
    return this.http.post<Departamento>('/api/v3/departamentos', payload, {
      withCredentials: true
    });
  }

  update(id: string, payload: DepartamentoUpdateRequest): Observable<Departamento> {
    return this.http.put<Departamento>(`/api/v3/departamentos/${encodeURIComponent(id)}`, payload, {
      withCredentials: true
    });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`/api/v3/departamentos/${encodeURIComponent(id)}`, {
      withCredentials: true
    });
  }
}
