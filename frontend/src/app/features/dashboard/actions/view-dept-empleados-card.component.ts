import { Component } from '@angular/core';
import { ActionToggleCardComponent } from './action-toggle-card.component';

@Component({
  selector: 'app-view-dept-empleados-card',
  standalone: true,
  imports: [ActionToggleCardComponent],
  template: '<app-action-toggle-card label="Ver empleados del departamento" />'
})
export class ViewDeptEmpleadosCardComponent {}
