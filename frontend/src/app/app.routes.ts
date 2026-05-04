import { Routes } from '@angular/router';
import { LoginPageComponent } from '@features/auth/login-page.component';
import { DashboardShellComponent } from '@features/dashboard/dashboard-shell.component';
import { authGuard } from '@core/auth/auth.guard';

export const APP_ROUTES: Routes = [
  { path: 'login', component: LoginPageComponent },
  { path: 'dashboard', component: DashboardShellComponent, canActivate: [authGuard] },
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: '**', redirectTo: 'dashboard' }
];
