import { describe, expect, it, beforeEach, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { LoginPageComponent } from './login-page.component';
import { AuthApiService } from '@core/api/auth-api.service';
import { AuthStore } from '@core/auth/auth.store';
import { Router } from '@angular/router';

describe('LoginPageComponent', () => {
  beforeEach(() => {
    sessionStorage.clear();
  });

  it('submits credentials and navigates on success', () => {
    const authApi = {
      login: vi.fn(() => of({ status: 'ACCEPTED', role: 'ADMIN' }))
    } as Partial<AuthApiService> as AuthApiService;
    const router = {
      navigateByUrl: vi.fn()
    } as Partial<Router> as Router;

    TestBed.configureTestingModule({
      imports: [LoginPageComponent],
      providers: [
        AuthStore,
        { provide: AuthApiService, useValue: authApi },
        { provide: Router, useValue: router }
      ]
    });

    const fixture = TestBed.createComponent(LoginPageComponent);
    const component = fixture.componentInstance;
    const authStore = TestBed.inject(AuthStore);
    const sessionSpy = vi.spyOn(authStore, 'setSession');

    component.form.setValue({ email: 'admin@empresa.com', password: 'Admin123!' });
    component.submit();

    expect(authApi.login).toHaveBeenCalledWith({ email: 'admin@empresa.com', password: 'Admin123!' });
    expect(sessionSpy).toHaveBeenCalledWith('ADMIN', 'admin@empresa.com', 'Admin123!');
    expect(router.navigateByUrl).toHaveBeenCalledWith('/dashboard');
    expect(component.loading()).toBe(false);
  });

  it('shows invalid credentials message on 401', () => {
    const authApi = {
      login: vi.fn(() => throwError(() => ({ status: 401 })))
    } as Partial<AuthApiService> as AuthApiService;
    const router = {
      navigateByUrl: vi.fn()
    } as Partial<Router> as Router;

    TestBed.configureTestingModule({
      imports: [LoginPageComponent],
      providers: [
        AuthStore,
        { provide: AuthApiService, useValue: authApi },
        { provide: Router, useValue: router }
      ]
    });

    const fixture = TestBed.createComponent(LoginPageComponent);
    const component = fixture.componentInstance;

    component.form.setValue({ email: 'admin@empresa.com', password: 'Wrong123!' });
    component.submit();

    expect(component.loading()).toBe(false);
    expect(component.error()).toBe('Credenciales invalidas');
    expect(router.navigateByUrl).not.toHaveBeenCalled();
  });
});
