import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthApiService } from '@core/api/auth-api.service';
import {
  Empleado,
  EmpleadoCreateRequest,
  EmpleadoUpdateRequest,
  EmpleadosApiService
} from '@core/api/empleados-api.service';
import {
  Departamento,
  DepartamentoCreateRequest,
  DepartamentoUpdateRequest,
  DepartamentosApiService
} from '@core/api/departamentos-api.service';
import { AuthStore } from '@core/auth/auth.store';
import { ActionToggleCardComponent } from './actions/action-toggle-card.component';

type ActionKey = 'add' | 'edit' | 'delete' | 'viewDeptEmployees';
type EntityKey = 'empleados' | 'departamentos';
type TableMode = 'empleados' | 'departamentos' | 'departamento-empleados';

@Component({
  selector: 'app-dashboard-shell',
  standalone: true,
  imports: [CommonModule, FormsModule, ActionToggleCardComponent],
  template: `
    <main class="dashboard-shell">
      <header class="header">
        <div>
          <p class="kicker">FRONT PARA DSW02-PRACTICA01</p>
          <h1>Dashboard Operativo</h1>
          <p class="subtitle-copy">Consulta entidades y ejecuta acciones segun tu rol.</p>
        </div>
        <div class="header-badge">{{ canWrite ? 'ADMIN' : 'USER' }}</div>
      </header>

      <aside class="sidebar">
        <h2>Navegacion</h2>

        <label class="search-box">
          <span>Buscar en tabla</span>
          <input
            type="search"
            [(ngModel)]="searchQuery"
            placeholder="Ej. admin, DEP, USER"
          />
        </label>

        <button
          type="button"
          class="entity-link"
          [class.entity-active]="selectedEntity === 'empleados'"
          (click)="selectEntity('empleados')"
        >
          Empleados
        </button>
        <button
          type="button"
          class="entity-link"
          [class.entity-active]="selectedEntity === 'departamentos'"
          (click)="selectEntity('departamentos')"
        >
          Departamentos
        </button>
        <button type="button" class="logout" (click)="logout()">Cerrar sesion</button>
      </aside>

      <section class="subtitle">
        <p class="subtitle-label">Entidad seleccionada</p>
        <h3>{{ tableTitle }}</h3>
        <p class="subtitle-copy">{{ filteredRows.length }} registros visibles</p>
      </section>

      <section class="table-area">
        <div class="actions">
          <app-action-toggle-card
            badge="CREATE"
            label="Agregar"
            description="Crea un registro de {{ selectedEntity }}"
            [active]="activeAction === 'add'"
            [disabled]="!canWrite"
            (trigger)="toggleAction('add')"
          />

          <app-action-toggle-card
            badge="UPDATE"
            label="Editar"
            description="Actualiza un registro existente"
            [active]="activeAction === 'edit'"
            [disabled]="!canWrite"
            (trigger)="toggleAction('edit')"
          />

          <app-action-toggle-card
            badge="DELETE"
            label="Eliminar"
            description="Elimina por clave o id"
            [active]="activeAction === 'delete'"
            [disabled]="!canWrite"
            (trigger)="toggleAction('delete')"
          />

          <app-action-toggle-card
            badge="DRILLDOWN"
            label="Ver empleados del departamento"
            description="Consulta /api/v3/departamentos/{id}/empleados"
            [active]="activeAction === 'viewDeptEmployees'"
            (trigger)="toggleAction('viewDeptEmployees')"
          />
        </div>

        <section class="action-panel" *ngIf="activeAction === 'add'">
          <h4>Agregar {{ selectedEntity === 'empleados' ? 'empleado' : 'departamento' }}</h4>

          <form class="form-grid" *ngIf="selectedEntity === 'empleados'" (ngSubmit)="createEmpleado()">
            <input [(ngModel)]="addEmpleado.nombre" name="addNombre" placeholder="Nombre" required />
            <input [(ngModel)]="addEmpleado.direccion" name="addDireccion" placeholder="Direccion" required />
            <input [(ngModel)]="addEmpleado.telefono" name="addTelefono" placeholder="Telefono" required />
            <input [(ngModel)]="addEmpleado.correoElectronico" name="addCorreo" type="email" placeholder="Correo" required />
            <input
              [(ngModel)]="addEmpleado.contrasena"
              name="addContrasena"
              type="password"
              placeholder="Contrasena (min. 8 caracteres)"
              minlength="8"
              required
            />
            <select [(ngModel)]="addEmpleado.rol" name="addRol">
              <option value="USER">USER</option>
              <option value="ADMIN">ADMIN</option>
            </select>
            <input [(ngModel)]="addEmpleado.departamentoId" name="addDepartamentoId" placeholder="Departamento ID (opcional, ej. DEP-000001)" />
            <button type="submit" [disabled]="saving">Guardar</button>
          </form>

          <form class="form-grid" *ngIf="selectedEntity === 'departamentos'" (ngSubmit)="createDepartamento()">
            <input [(ngModel)]="addDepartamento.nombre" name="addDepNombre" placeholder="Nombre del departamento" required />
            <button type="submit" [disabled]="saving">Guardar</button>
          </form>
        </section>

        <section class="action-panel" *ngIf="activeAction === 'edit'">
          <h4>Editar {{ selectedEntity === 'empleados' ? 'empleado' : 'departamento' }}</h4>

          <form class="form-grid" *ngIf="selectedEntity === 'empleados'" (ngSubmit)="updateEmpleado()">
            <input [(ngModel)]="editEmpleado.clave" name="editClave" placeholder="Clave (ej. EMP-000001)" required />
            <input [(ngModel)]="editEmpleado.nombre" name="editNombre" placeholder="Nombre (opcional)" />
            <input [(ngModel)]="editEmpleado.direccion" name="editDireccion" placeholder="Direccion (opcional)" />
            <input [(ngModel)]="editEmpleado.telefono" name="editTelefono" placeholder="Telefono (opcional)" />
            <input [(ngModel)]="editEmpleado.correoElectronico" name="editCorreo" type="email" placeholder="Correo (opcional)" />
            <input
              [(ngModel)]="editEmpleado.contrasena"
              name="editContrasena"
              type="password"
              placeholder="Nueva contrasena (opcional, min. 8)"
              minlength="8"
            />
            <select [(ngModel)]="editEmpleado.rol" name="editRol">
              <option value="">Rol (sin cambio)</option>
              <option value="USER">USER</option>
              <option value="ADMIN">ADMIN</option>
            </select>
            <button type="submit" [disabled]="saving">Actualizar</button>
          </form>

          <form class="form-grid" *ngIf="selectedEntity === 'departamentos'" (ngSubmit)="updateDepartamento()">
            <input [(ngModel)]="editDepartamento.id" name="editDepId" placeholder="ID" required />
            <input [(ngModel)]="editDepartamento.nombre" name="editDepNombre" placeholder="Nombre" required />
            <button type="submit" [disabled]="saving">Actualizar</button>
          </form>
        </section>

        <section class="action-panel" *ngIf="activeAction === 'delete'">
          <h4>Eliminar {{ selectedEntity === 'empleados' ? 'empleado' : 'departamento' }}</h4>

          <form class="form-grid" *ngIf="selectedEntity === 'empleados'" (ngSubmit)="deleteEmpleado()">
            <input [(ngModel)]="deleteEmpleadoClave" name="deleteClave" placeholder="Clave a eliminar" required />
            <button type="submit" [disabled]="saving">Eliminar</button>
          </form>

          <form class="form-grid" *ngIf="selectedEntity === 'departamentos'" (ngSubmit)="deleteDepartamento()">
            <input [(ngModel)]="deleteDepartamentoId" name="deleteId" placeholder="ID a eliminar" required />
            <button type="submit" [disabled]="saving">Eliminar</button>
          </form>
        </section>

        <section class="action-panel" *ngIf="activeAction === 'viewDeptEmployees'">
          <h4>Ver empleados por departamento</h4>
          <form class="form-grid" (ngSubmit)="loadDepartamentoEmpleados()">
            <input
              [(ngModel)]="departamentoTargetId"
              name="deptTarget"
              placeholder="Departamento ID (ej. DEP-000001)"
              required
            />
            <button type="submit" [disabled]="saving">Consultar</button>
          </form>
        </section>

        <p class="state loading" *ngIf="loading">Cargando datos...</p>
        <p class="state operation" *ngIf="operationLoading">
          <span class="spinner" aria-hidden="true"></span>
          {{ operationLoadingText }}
        </p>
        <p class="state success" *ngIf="statusMessage">{{ statusMessage }}</p>
        <p class="state error" *ngIf="errorMessage">{{ errorMessage }}</p>

        <div class="table-wrap" role="region" aria-label="Tabla de datos">
          <table>
            <thead>
              <tr>
                <th *ngFor="let col of tableColumns">{{ col }}</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let row of filteredRows">
                <td *ngFor="let col of tableColumns">{{ row[col] }}</td>
              </tr>
              <tr *ngIf="!filteredRows.length">
                <td [attr.colspan]="tableColumns.length || 1">No hay registros para mostrar.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <footer class="footer">v1.0.0-front</footer>
    </main>
  `,
  styleUrls: ['./dashboard-shell.component.scss']
})
export class DashboardShellComponent implements OnInit {
  selectedEntity: EntityKey = 'empleados';
  tableMode: TableMode = 'empleados';
  activeAction: ActionKey | null = null;

  loading = false;
  saving = false;
  operationLoading = false;
  operationLoadingText = '';
  statusMessage = '';
  errorMessage = '';
  searchQuery = '';

  empleados: Empleado[] = [];
  departamentos: Departamento[] = [];
  departamentoEmpleados: Empleado[] = [];

  departamentoTargetId = '';
  deleteEmpleadoClave = '';
  deleteDepartamentoId = '';

  addEmpleado: EmpleadoCreateRequest = {
    nombre: '',
    direccion: '',
    telefono: '',
    correoElectronico: '',
    contrasena: '',
    rol: 'USER',
    departamentoId: ''
  };

  addDepartamento: DepartamentoCreateRequest = {
    nombre: ''
  };

  editEmpleado: {
    clave: string;
    nombre: string;
    direccion: string;
    telefono: string;
    correoElectronico: string;
    contrasena: string;
    rol: '' | 'USER' | 'ADMIN';
  } = {
    clave: '',
    nombre: '',
    direccion: '',
    telefono: '',
    correoElectronico: '',
    contrasena: '',
    rol: ''
  };

  editDepartamento: {
    id: string;
    nombre: string;
  } = {
    id: '',
    nombre: ''
  };

  private inFlight = 0;

  constructor(
    private readonly empleadosApi: EmpleadosApiService,
    private readonly departamentosApi: DepartamentosApiService,
    private readonly authApi: AuthApiService,
    private readonly authStore: AuthStore,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.hydrateSessionRole();
    this.loadEmpleados();
    this.loadDepartamentos();
  }

  get canWrite(): boolean {
    return this.isE2eBypass || this.authStore.role() === 'ADMIN';
  }

  get isE2eBypass(): boolean {
    if (typeof window === 'undefined') {
      return false;
    }
    return Boolean((window as Window & { __E2E_BYPASS_AUTH__?: boolean }).__E2E_BYPASS_AUTH__);
  }

  get tableTitle(): string {
    if (this.tableMode === 'departamento-empleados') {
      return 'Empleados del departamento';
    }
    return this.tableMode === 'empleados' ? 'Empleados' : 'Departamentos';
  }

  get tableColumns(): string[] {
    const first = this.filteredRows[0];
    return first ? Object.keys(first) : this.tableMode === 'departamentos' ? ['ID', 'Nombre'] : ['Clave', 'Nombre'];
  }

  get filteredRows(): Array<Record<string, string>> {
    const q = this.searchQuery.trim().toLowerCase();
    const rows = this.currentRows;
    if (!q) {
      return rows;
    }
    return rows.filter((row) => Object.values(row).some((value) => value.toLowerCase().includes(q)));
  }

  selectEntity(entity: EntityKey): void {
    this.selectedEntity = entity;
    this.tableMode = entity;
    this.activeAction = null;
    this.statusMessage = '';
    this.errorMessage = '';
  }

  toggleAction(action: ActionKey): void {
    if (!this.canWrite && (action === 'add' || action === 'edit' || action === 'delete')) {
      this.errorMessage = 'Solo ADMIN puede ejecutar acciones de escritura.';
      this.statusMessage = '';
      return;
    }
    this.activeAction = this.activeAction === action ? null : action;
    this.statusMessage = '';
    this.errorMessage = '';
  }

  createEmpleado(): void {
    const password = this.addEmpleado.contrasena.trim();
    if (password.length < 8) {
      this.errorMessage = 'La contrasena debe tener al menos 8 caracteres.';
      return;
    }

    const departamentoId = this.trimOrUndefined(this.addEmpleado.departamentoId ?? '');
    if (departamentoId && !this.isValidDepartamentoId(departamentoId)) {
      this.errorMessage = 'El departamento debe tener formato DEP-000001.';
      return;
    }

    this.beginOperation('Creando empleado...');

    const payload: EmpleadoCreateRequest = {
      ...this.addEmpleado,
      contrasena: password,
      departamentoId
    };

    this.empleadosApi.create(payload).subscribe({
      next: () => {
        this.endOperation();
        this.statusMessage = 'Empleado creado correctamente.';
        this.resetAddEmpleadoForm();
        this.loadEmpleados();
      },
      error: (error: HttpErrorResponse) => {
        this.endOperation();
        this.errorMessage = this.extractBackendError(error, 'No se pudo crear el empleado.');
      }
    });
  }

  createDepartamento(): void {
    this.beginOperation('Creando departamento...');

    this.departamentosApi.create({ nombre: this.addDepartamento.nombre.trim() }).subscribe({
      next: () => {
        this.endOperation();
        this.statusMessage = 'Departamento creado correctamente.';
        this.addDepartamento.nombre = '';
        this.loadDepartamentos();
      },
      error: (error: HttpErrorResponse) => {
        this.endOperation();
        this.errorMessage = this.extractBackendError(error, 'No se pudo crear el departamento.');
      }
    });
  }

  updateEmpleado(): void {
    const clave = this.editEmpleado.clave.trim();
    if (!clave) {
      this.errorMessage = 'Debes indicar la clave del empleado.';
      return;
    }

    const newPassword = this.editEmpleado.contrasena.trim();
    if (newPassword && newPassword.length < 8) {
      this.errorMessage = 'La nueva contrasena debe tener al menos 8 caracteres.';
      return;
    }

    const payload: EmpleadoUpdateRequest = {
      nombre: this.trimOrUndefined(this.editEmpleado.nombre),
      direccion: this.trimOrUndefined(this.editEmpleado.direccion),
      telefono: this.trimOrUndefined(this.editEmpleado.telefono),
      correoElectronico: this.trimOrUndefined(this.editEmpleado.correoElectronico),
      contrasena: this.trimOrUndefined(this.editEmpleado.contrasena),
      rol: this.editEmpleado.rol || undefined
    };

    this.beginOperation('Actualizando empleado...');

    this.empleadosApi.update(clave, payload).subscribe({
      next: () => {
        this.endOperation();
        this.statusMessage = 'Empleado actualizado correctamente.';
        this.resetEditEmpleadoForm();
        this.loadEmpleados();
      },
      error: (error: HttpErrorResponse) => {
        this.endOperation();
        this.errorMessage = this.extractBackendError(error, 'No se pudo actualizar el empleado.');
      }
    });
  }

  updateDepartamento(): void {
    const id = this.editDepartamento.id.trim();
    const nombre = this.editDepartamento.nombre.trim();
    if (!id || !nombre) {
      this.errorMessage = 'ID y nombre son obligatorios para actualizar departamento.';
      return;
    }

    this.beginOperation('Actualizando departamento...');

    this.departamentosApi.update(id, { nombre }).subscribe({
      next: () => {
        this.endOperation();
        this.statusMessage = 'Departamento actualizado correctamente.';
        this.editDepartamento = { id: '', nombre: '' };
        this.loadDepartamentos();
      },
      error: (error: HttpErrorResponse) => {
        this.endOperation();
        this.errorMessage = this.extractBackendError(error, 'No se pudo actualizar el departamento.');
      }
    });
  }

  deleteEmpleado(): void {
    const clave = this.deleteEmpleadoClave.trim();
    if (!clave) {
      this.errorMessage = 'Debes indicar la clave del empleado a eliminar.';
      return;
    }

    this.beginOperation('Eliminando empleado...');

    this.empleadosApi.delete(clave).subscribe({
      next: () => {
        this.endOperation();
        this.statusMessage = 'Empleado eliminado correctamente.';
        this.deleteEmpleadoClave = '';
        this.loadEmpleados();
      },
      error: (error: HttpErrorResponse) => {
        this.endOperation();
        this.errorMessage = this.extractBackendError(error, 'No se pudo eliminar el empleado.');
      }
    });
  }

  deleteDepartamento(): void {
    const id = this.deleteDepartamentoId.trim();
    if (!id) {
      this.errorMessage = 'Debes indicar el ID del departamento a eliminar.';
      return;
    }

    this.beginOperation('Eliminando departamento...');

    this.departamentosApi.delete(id).subscribe({
      next: () => {
        this.endOperation();
        this.statusMessage = 'Departamento eliminado correctamente.';
        this.deleteDepartamentoId = '';
        this.loadDepartamentos();
      },
      error: (error: HttpErrorResponse) => {
        this.endOperation();
        this.errorMessage = this.extractBackendError(error, 'No se pudo eliminar el departamento.');
      }
    });
  }

  loadDepartamentoEmpleados(): void {
    const departamentoId = this.departamentoTargetId.trim();
    if (!departamentoId) {
      this.errorMessage = 'Debes indicar un departamento para consultar empleados.';
      return;
    }

    this.beginOperation('Consultando empleados del departamento...');

    this.empleadosApi.listByDepartamento(departamentoId).subscribe({
      next: (response) => {
        this.endOperation();
        this.departamentoEmpleados = response.content;
        this.tableMode = 'departamento-empleados';
        this.statusMessage = `Consulta completada para ${departamentoId}.`;
      },
      error: (error: HttpErrorResponse) => {
        this.endOperation();
        this.errorMessage = this.extractBackendError(error, 'No se pudo consultar empleados del departamento.');
      }
    });
  }

  logout(): void {
    const csrf = this.readCookie('XSRF-TOKEN') ?? '';
    this.authApi.logout(csrf).subscribe({
      next: () => {
        this.authStore.clear();
        void this.router.navigateByUrl('/login');
      },
      error: () => {
        this.authStore.clear();
        void this.router.navigateByUrl('/login');
      }
    });
  }

  private loadEmpleados(): void {
    this.startLoading();
    this.empleadosApi.list().subscribe({
      next: (response) => {
        this.stopLoading();
        this.empleados = response.content;
      },
      error: (error: HttpErrorResponse) => {
        this.stopLoading();
        this.errorMessage = this.extractBackendError(error, 'No se pudo cargar empleados del API.');
      }
    });
  }

  private loadDepartamentos(): void {
    this.startLoading();
    this.departamentosApi.list().subscribe({
      next: (response) => {
        this.stopLoading();
        this.departamentos = response.content;
      },
      error: (error: HttpErrorResponse) => {
        this.stopLoading();
        this.errorMessage = this.extractBackendError(error, 'No se pudo cargar departamentos del API.');
      }
    });
  }

  private isValidDepartamentoId(departamentoId: string): boolean {
    return /^DEP-\d{6}$/.test(departamentoId);
  }

  private get currentRows(): Array<Record<string, string>> {
    if (this.tableMode === 'departamentos') {
      return this.departamentos.map((departamento) => ({
        ID: departamento.id,
        Nombre: departamento.nombre
      }));
    }

    const empleados = this.tableMode === 'departamento-empleados' ? this.departamentoEmpleados : this.empleados;
    return empleados.map((empleado) => ({
      Clave: empleado.clave,
      Nombre: empleado.nombre,
      Correo: empleado.correoElectronico,
      Rol: empleado.rol,
      Activo: empleado.activo ? 'SI' : 'NO',
      Departamento: empleado.departamentoId ?? '-'
    }));
  }

  private resetAddEmpleadoForm(): void {
    this.addEmpleado = {
      nombre: '',
      direccion: '',
      telefono: '',
      correoElectronico: '',
      contrasena: '',
      rol: 'USER',
      departamentoId: ''
    };
  }

  private resetEditEmpleadoForm(): void {
    this.editEmpleado = {
      clave: '',
      nombre: '',
      direccion: '',
      telefono: '',
      correoElectronico: '',
      contrasena: '',
      rol: ''
    };
  }

  private hydrateSessionRole(): void {
    const csrf = this.readCookie('XSRF-TOKEN');
    if (!csrf) {
      return;
    }

    this.authApi.refresh(csrf).subscribe({
      next: (response) => {
        this.authStore.setAuthenticated(response.role);
      },
      error: () => {
        // Ignore here; route guard and API calls will handle invalid sessions.
      }
    });
  }

  private extractBackendError(error: HttpErrorResponse, fallback: string): string {
    if (error.status === 401) {
      return 'Sesion no autorizada. Inicia sesion nuevamente.';
    }
    if (error.status === 403) {
      return 'No tienes permisos para ejecutar esta accion.';
    }

    const apiMessage = (error.error as { message?: string } | null)?.message;
    if (typeof apiMessage === 'string' && apiMessage.trim()) {
      return apiMessage;
    }

    return fallback;
  }

  private readCookie(name: string): string | null {
    if (typeof document === 'undefined') {
      return null;
    }

    const escapedName = name.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    const match = document.cookie.match(new RegExp(`(^| )${escapedName}=([^;]+)`));
    return match ? decodeURIComponent(match[2]) : null;
  }

  private trimOrUndefined(value: string): string | undefined {
    const trimmed = value.trim();
    return trimmed ? trimmed : undefined;
  }

  private clearMessages(): void {
    this.statusMessage = '';
    this.errorMessage = '';
  }

  private beginOperation(message: string): void {
    this.saving = true;
    this.operationLoading = true;
    this.operationLoadingText = message;
    this.clearMessages();
  }

  private endOperation(): void {
    this.saving = false;
    this.operationLoading = false;
    this.operationLoadingText = '';
  }

  private startLoading(): void {
    this.inFlight += 1;
    this.loading = true;
  }

  private stopLoading(): void {
    this.inFlight = Math.max(0, this.inFlight - 1);
    this.loading = this.inFlight > 0;
  }
}
