import { Component } from '@angular/core';
import { ActionToggleCardComponent } from './action-toggle-card.component';

@Component({
  selector: 'app-delete-card',
  standalone: true,
  imports: [ActionToggleCardComponent],
  template: '<app-action-toggle-card label="Eliminar" />'
})
export class DeleteCardComponent {}
