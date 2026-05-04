import { Injectable, signal } from '@angular/core';

export type AuthRole = 'USER' | 'ADMIN' | null;

@Injectable({ providedIn: 'root' })
export class AuthStore {
  private readonly basicAuthStorageKey = 'empleados.basicAuthHeader';

  readonly isAuthenticated = signal(false);
  readonly role = signal<AuthRole>(null);
  readonly basicAuthHeader = signal<string | null>(null);

  constructor() {
    if (typeof window === 'undefined') {
      return;
    }
    const persistedBasic = sessionStorage.getItem(this.basicAuthStorageKey);
    if (persistedBasic) {
      this.basicAuthHeader.set(persistedBasic);
    }
  }

  setAuthenticated(role: Exclude<AuthRole, null>): void {
    this.isAuthenticated.set(true);
    this.role.set(role);
  }

  setSession(role: Exclude<AuthRole, null>, email: string, password: string): void {
    this.setAuthenticated(role);
    const basic = 'Basic ' + btoa(`${email}:${password}`);
    this.basicAuthHeader.set(basic);
    if (typeof window !== 'undefined') {
      sessionStorage.setItem(this.basicAuthStorageKey, basic);
    }
  }

  clear(): void {
    this.isAuthenticated.set(false);
    this.role.set(null);
    this.basicAuthHeader.set(null);
    if (typeof window !== 'undefined') {
      sessionStorage.removeItem(this.basicAuthStorageKey);
    }
  }
}
