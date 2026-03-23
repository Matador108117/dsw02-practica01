import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-dashboard-subtitle',
  standalone: true,
  template: '<h2>{{ subtitle }}</h2>'
})
export class SubtitleComponent {
  @Input() subtitle = 'Entidad';
}
