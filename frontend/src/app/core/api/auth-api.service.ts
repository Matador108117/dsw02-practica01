import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  status: 'ACCEPTED';
  role: 'USER' | 'ADMIN';
}

export interface RefreshResponse {
  status: 'ACCEPTED';
  role: 'USER' | 'ADMIN';
  expiresIn: number;
}

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly baseUrl = '/api/v4/auth';

  constructor(private readonly http: HttpClient) {}

  login(payload: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/login`, payload, { withCredentials: true });
  }

  refresh(csrfToken: string): Observable<RefreshResponse> {
    return this.http.post<RefreshResponse>(`${this.baseUrl}/refresh`, {}, {
      withCredentials: true,
      headers: { 'X-CSRF-Token': csrfToken }
    });
  }

  logout(csrfToken: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/logout`, {}, {
      withCredentials: true,
      headers: { 'X-CSRF-Token': csrfToken }
    });
  }
}
