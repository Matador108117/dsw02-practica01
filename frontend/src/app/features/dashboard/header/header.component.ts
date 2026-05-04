import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-dashboard-header',
  standalone: true,
  template: '<h1>{{ title }}</h1>'
})
export class HeaderComponent {
  @Input() title = 'Front para dsw02-practica01';
}
