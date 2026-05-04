import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthApiService } from '@core/api/auth-api.service';
import { AuthStore } from '@core/auth/auth.store';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <section class="login-page">
      <article class="login-card">
        <header class="hero">
          <p class="kicker">PLATAFORMA EMPLEADOS</p>
          <h1>Iniciar sesion</h1>
          <p class="subtitle">Accede para gestionar empleados y departamentos.</p>
        </header>

        <form class="login-form" [formGroup]="form" (ngSubmit)="submit()" novalidate>
          <label>
            <span>Correo</span>
            <input type="email" formControlName="email" autocomplete="email" />
          </label>
          <label>
            <span>Contrasena</span>
            <input type="password" formControlName="password" autocomplete="current-password" />
          </label>
          <button type="submit" [disabled]="form.invalid || loading()">
            {{ loading() ? 'Validando...' : 'Entrar' }}
          </button>
        </form>

        <p *ngIf="error()" class="error">{{ error() }}</p>
      </article>
    </section>
  `,
  styles: [
    `
      :host {
        --bg-a: #070709;
        --bg-b: #111420;
        --ink: #f2f5ff;
        --muted: #b4bedb;
        --accent: #ff1f4b;
        --accent-hover: #ff0038;
        --danger: #ff6b89;
        --line: rgba(255, 255, 255, 0.16);
        --panel: rgba(15, 17, 25, 0.86);
        display: block;
        min-height: 100dvh;
      }

      .login-page {
        min-height: 100dvh;
        display: grid;
        place-items: center;
        padding: 2rem 1rem;
        background:
          linear-gradient(130deg, rgba(255, 0, 56, 0.14) 0%, rgba(255, 0, 56, 0) 42%),
          radial-gradient(circle at 80% 82%, rgba(255, 31, 75, 0.2) 0%, rgba(255, 31, 75, 0) 42%),
          radial-gradient(circle at 5% 15%, rgba(120, 129, 165, 0.22) 0%, rgba(120, 129, 165, 0) 28%),
          linear-gradient(142deg, var(--bg-a) 0%, var(--bg-b) 100%);
      }

      .login-card {
        width: min(100%, 520px);
        border-radius: 14px;
        padding: 2rem;
        background:
          linear-gradient(160deg, rgba(28, 31, 44, 0.95) 0%, var(--panel) 100%);
        border: 1px solid var(--line);
        box-shadow:
          0 22px 48px rgba(0, 0, 0, 0.55),
          inset 0 0 0 1px rgba(255, 31, 75, 0.12);
        backdrop-filter: blur(8px);
        position: relative;
        overflow: hidden;
      }

      .login-card::before {
        content: '';
        position: absolute;
        inset: 0 auto 0 0;
        width: 4px;
        background: linear-gradient(180deg, #ff335f 0%, #ff0038 100%);
      }

      .login-card::after {
        content: '';
        position: absolute;
        right: 0;
        top: 0;
        width: 64px;
        height: 64px;
        clip-path: polygon(100% 0, 100% 100%, 0 0);
        background: linear-gradient(135deg, rgba(255, 31, 75, 0.22) 0%, rgba(255, 31, 75, 0.06) 100%);
      }

      .hero h1 {
        margin: 0;
        color: var(--ink);
        font-size: clamp(1.95rem, 3.6vw, 2.6rem);
        line-height: 1.08;
        font-family: 'Orbitron', 'Rajdhani', sans-serif;
        letter-spacing: 0.03em;
      }

      .kicker {
        margin: 0 0 0.35rem;
        letter-spacing: 0.17em;
        font-weight: 700;
        font-size: 0.72rem;
        color: var(--accent);
      }

      .subtitle {
        margin: 0.55rem 0 0;
        color: var(--muted);
      }

      .login-form {
        margin-top: 1.5rem;
        display: grid;
        gap: 1rem;
      }

      label {
        display: grid;
        gap: 0.45rem;
      }

      label span {
        font-size: 0.92rem;
        font-weight: 600;
        color: var(--ink);
      }

      input {
        border: 1px solid var(--line);
        border-radius: 12px;
        padding: 0.78rem 0.92rem;
        font-size: 1rem;
        color: var(--ink);
        background: rgba(8, 9, 14, 0.74);
        transition: border-color 140ms ease, box-shadow 140ms ease;
      }

      input:focus {
        outline: none;
        border-color: var(--accent);
        box-shadow: 0 0 0 3px rgba(255, 31, 75, 0.22);
      }

      button {
        margin-top: 0.35rem;
        border: none;
        border-radius: 12px;
        padding: 0.82rem 1rem;
        font-weight: 700;
        font-size: 0.98rem;
        color: #fff;
        background: linear-gradient(90deg, var(--accent) 0%, var(--accent-hover) 100%);
        cursor: pointer;
        transition: filter 140ms ease, transform 140ms ease;
      }

      button:hover:enabled {
        filter: brightness(1.1);
        transform: translateY(-1px);
      }

      button:disabled {
        cursor: not-allowed;
        opacity: 0.68;
      }

      .error {
        margin: 1rem 0 0;
        color: var(--danger);
        font-weight: 600;
      }

      @media (max-width: 520px) {
        .login-card {
          padding: 1.25rem;
          border-radius: 16px;
        }
      }
    `
  ]
})
export class LoginPageComponent {
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly form;

  constructor(
    private readonly fb: FormBuilder,
    private readonly authApi: AuthApiService,
    private readonly authStore: AuthStore,
    private readonly router: Router
  ) {
    this.form = this.fb.nonNullable.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    const credentials = this.form.getRawValue();
    this.authApi.login(credentials).subscribe({
      next: (response: { role: 'USER' | 'ADMIN' }) => {
        this.authStore.setSession(response.role, credentials.email, credentials.password);
        this.loading.set(false);
        void this.router.navigateByUrl('/dashboard');
      },
      error: (err: { status?: number }) => {
        this.loading.set(false);
        if (err?.status === 401) {
          this.error.set('Credenciales invalidas');
          return;
        }
        this.error.set('No fue posible conectar con el API. Intenta de nuevo.');
      }
    });
  }
}
