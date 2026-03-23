import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-dashboard-footer',
  standalone: true,
  template: '<footer>{{ version }}</footer>'
})
export class FooterComponent {
  @Input() version = 'v1.0.0-front';
}
