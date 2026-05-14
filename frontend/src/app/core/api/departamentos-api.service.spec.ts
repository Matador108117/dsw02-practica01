import { describe, expect, it, beforeEach, afterEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { DepartamentosApiService } from './departamentos-api.service';

describe('DepartamentosApiService', () => {
  let service: DepartamentosApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DepartamentosApiService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(DepartamentosApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('lists departamentos with pagination', () => {
    service.list(0, 20).subscribe();

    const req = httpMock.expectOne('/api/v3/departamentos?page=0&size=20');
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBe(true);
    req.flush({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 });
  });

  it('creates departamento', () => {
    service.create({ nombre: 'Tecnologia' }).subscribe();

    const req = httpMock.expectOne('/api/v3/departamentos');
    expect(req.request.method).toBe('POST');
    expect(req.request.withCredentials).toBe(true);
    req.flush({});
  });

  it('updates departamento', () => {
    service.update('DEP-000001', { nombre: 'Ventas' }).subscribe();

    const req = httpMock.expectOne('/api/v3/departamentos/DEP-000001');
    expect(req.request.method).toBe('PUT');
    expect(req.request.withCredentials).toBe(true);
    req.flush({});
  });

  it('deletes departamento', () => {
    service.delete('DEP-000001').subscribe();

    const req = httpMock.expectOne('/api/v3/departamentos/DEP-000001');
    expect(req.request.method).toBe('DELETE');
    expect(req.request.withCredentials).toBe(true);
    req.flush(null);
  });
});
