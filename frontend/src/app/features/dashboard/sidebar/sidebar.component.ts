import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule],
  template: `
    <input type="search" placeholder="Buscar entidad" />
    <ul>
      <li *ngFor="let entity of entities" (click)="select.emit(entity)">{{ entity }}</li>
    </ul>
    <button type="button" (click)="logout.emit()">Cerrar sesion</button>
  `
})
export class SidebarComponent {
  @Input() entities: string[] = ['empleados', 'departamentos'];
  @Output() select = new EventEmitter<string>();
  @Output() logout = new EventEmitter<void>();
}
