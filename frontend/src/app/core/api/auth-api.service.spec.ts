import { describe, expect, it, beforeEach, afterEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthApiService } from './auth-api.service';

describe('AuthApiService', () => {
  let service: AuthApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AuthApiService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(AuthApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('posts login with credentials', () => {
    service.login({ email: 'admin@empresa.com', password: 'Admin123!' }).subscribe();

    const req = httpMock.expectOne('/api/v4/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.withCredentials).toBe(true);
    expect(req.request.body).toEqual({ email: 'admin@empresa.com', password: 'Admin123!' });
    req.flush({ status: 'ACCEPTED', role: 'ADMIN' });
  });

  it('posts refresh with CSRF header', () => {
    service.refresh('csrf-token').subscribe();

    const req = httpMock.expectOne('/api/v4/auth/refresh');
    expect(req.request.method).toBe('POST');
    expect(req.request.withCredentials).toBe(true);
    expect(req.request.headers.get('X-CSRF-Token')).toBe('csrf-token');
    req.flush({ status: 'ACCEPTED', role: 'USER', expiresIn: 900 });
  });

  it('posts logout with CSRF header', () => {
    service.logout('csrf-logout').subscribe();

    const req = httpMock.expectOne('/api/v4/auth/logout');
    expect(req.request.method).toBe('POST');
    expect(req.request.withCredentials).toBe(true);
    expect(req.request.headers.get('X-CSRF-Token')).toBe('csrf-logout');
    req.flush(null);
  });
});
