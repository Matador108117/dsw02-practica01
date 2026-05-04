import { Component } from '@angular/core';
import { ActionToggleCardComponent } from './action-toggle-card.component';

@Component({
  selector: 'app-edit-card',
  standalone: true,
  imports: [ActionToggleCardComponent],
  template: '<app-action-toggle-card label="Editar" />'
})
export class EditCardComponent {}
