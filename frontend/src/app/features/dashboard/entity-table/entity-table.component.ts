import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-entity-table',
  standalone: true,
  imports: [CommonModule],
  template: `
    <table>
      <thead>
        <tr><th *ngFor="let col of columns">{{ col }}</th></tr>
      </thead>
      <tbody>
        <tr *ngFor="let row of rows">
          <td *ngFor="let col of columns">{{ row[col] }}</td>
        </tr>
      </tbody>
    </table>
  `
})
export class EntityTableComponent {
  @Input() columns: string[] = [];
  @Input() rows: Array<Record<string, string | number | boolean | null>> = [];
}
