import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class DashboardFacade {
  private role: 'USER' | 'ADMIN' = 'USER';

  setRole(role: 'USER' | 'ADMIN'): void {
    this.role = role;
  }

  canWrite(): boolean {
    return this.role === 'ADMIN';
  }
}
