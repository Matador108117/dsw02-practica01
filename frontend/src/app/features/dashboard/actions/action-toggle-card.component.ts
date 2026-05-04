import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-action-toggle-card',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button
      type="button"
      class="toggle-card"
      [class.active]="active"
      [class.disabled]="disabled"
      [attr.aria-pressed]="active"
      [disabled]="disabled"
      (click)="trigger.emit()"
    >
      <span class="badge">{{ badge }}</span>
      <strong>{{ label }}</strong>
      <small>{{ description }}</small>
    </button>
  `,
  styles: [
    `
      .toggle-card {
        width: 100%;
        text-align: left;
        border: 1px solid rgba(255, 255, 255, 0.14);
        background: linear-gradient(160deg, rgba(23, 25, 38, 0.95) 0%, rgba(15, 17, 27, 0.95) 100%);
        color: #f2f5ff;
        border-radius: 10px;
        padding: 0.8rem;
        display: grid;
        gap: 0.25rem;
        cursor: pointer;
        transition: transform 140ms ease, border-color 140ms ease, box-shadow 140ms ease;
      }

      .toggle-card:hover:enabled {
        transform: translateY(-1px);
        border-color: rgba(255, 31, 75, 0.52);
        box-shadow: 0 8px 18px rgba(0, 0, 0, 0.28);
      }

      .toggle-card.active {
        border-color: #ff1f4b;
        box-shadow: 0 0 0 3px rgba(255, 31, 75, 0.22);
        background: linear-gradient(180deg, rgba(255, 31, 75, 0.16) 0%, rgba(15, 17, 27, 0.95) 100%);
      }

      .toggle-card.disabled {
        opacity: 0.6;
        cursor: not-allowed;
      }

      .badge {
        display: inline-flex;
        width: fit-content;
        font-size: 0.72rem;
        font-weight: 700;
        letter-spacing: 0.06em;
        color: #ff6e8f;
        font-family: 'Orbitron', 'Rajdhani', sans-serif;
      }

      strong {
        font-size: 0.96rem;
      }

      small {
        color: #aeb7d3;
      }
    `
  ]
})
export class ActionToggleCardComponent {
  @Input() label = 'Accion';
  @Input() description = 'Selecciona para abrir esta accion';
  @Input() badge = 'ACCION';
  @Input() active = false;
  @Input() disabled = false;
  @Output() trigger = new EventEmitter<void>();
}
