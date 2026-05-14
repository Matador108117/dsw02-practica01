import { describe, expect, it, beforeEach, afterEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { EmpleadosApiService } from './empleados-api.service';

describe('EmpleadosApiService', () => {
  let service: EmpleadosApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [EmpleadosApiService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(EmpleadosApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('lists empleados with pagination', () => {
    service.list(2, 15).subscribe();

    const req = httpMock.expectOne('/api/v3/empleados?page=2&size=15');
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBe(true);
    req.flush({ content: [], page: 2, size: 15, totalElements: 0, totalPages: 0 });
  });

  it('creates empleado with credentials', () => {
    service.create({
      nombre: 'Empleado 1',
      direccion: 'Calle 1',
      telefono: '5550001111',
      correoElectronico: 'empleado@empresa.com',
      contrasena: 'Secret123!',
      rol: 'USER'
    }).subscribe();

    const req = httpMock.expectOne('/api/v3/empleados');
    expect(req.request.method).toBe('POST');
    expect(req.request.withCredentials).toBe(true);
    req.flush({});
  });

  it('updates empleado by clave', () => {
    service.update('EMP-000001', { nombre: 'Empleado actualizado' }).subscribe();

    const req = httpMock.expectOne('/api/v3/empleados/EMP-000001');
    expect(req.request.method).toBe('PUT');
    expect(req.request.withCredentials).toBe(true);
    req.flush({});
  });

  it('deletes empleado by clave', () => {
    service.delete('EMP-000001').subscribe();

    const req = httpMock.expectOne('/api/v3/empleados/EMP-000001');
    expect(req.request.method).toBe('DELETE');
    expect(req.request.withCredentials).toBe(true);
    req.flush(null);
  });

  it('lists empleados by departamento with pagination', () => {
    service.listByDepartamento('DEP-000001', 1, 10).subscribe();

    const req = httpMock.expectOne('/api/v3/departamentos/DEP-000001/empleados?page=1&size=10');
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBe(true);
    req.flush({ content: [], page: 1, size: 10, totalElements: 0, totalPages: 0 });
  });
});
