import { Component } from '@angular/core';
import { ActionToggleCardComponent } from './action-toggle-card.component';

@Component({
  selector: 'app-add-card',
  standalone: true,
  imports: [ActionToggleCardComponent],
  template: '<app-action-toggle-card label="Agregar" />'
})
export class AddCardComponent {}
