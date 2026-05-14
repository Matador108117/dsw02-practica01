import { describe, expect, it, beforeEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, provideRouter, Router } from '@angular/router';
import { authGuard } from './auth.guard';
import { AuthStore } from './auth.store';

describe('authGuard', () => {
  let authStore: AuthStore;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AuthStore, provideRouter([])]
    });
    authStore = TestBed.inject(AuthStore);
    router = TestBed.inject(Router);
    authStore.clear();
  });

  const route = {} as ActivatedRouteSnapshot;
  const state = { url: '/dashboard' } as RouterStateSnapshot;

  it('allows navigation when e2e bypass is enabled', () => {
    (window as { __E2E_BYPASS_AUTH__?: boolean }).__E2E_BYPASS_AUTH__ = true;

    const result = TestBed.runInInjectionContext(() => authGuard(route, state));

    delete (window as { __E2E_BYPASS_AUTH__?: boolean }).__E2E_BYPASS_AUTH__;
    expect(result).toBe(true);
  });

  it('redirects to login when not authenticated', () => {
    const result = TestBed.runInInjectionContext(() => authGuard(route, state));
    const serialized = router.serializeUrl(result as UrlTree);

    expect(serialized).toBe('/login');
  });

  it('allows navigation when authenticated', () => {
    authStore.setAuthenticated('ADMIN');

    const result = TestBed.runInInjectionContext(() => authGuard(route, state));

    expect(result).toBe(true);
  });
});
