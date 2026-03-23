import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStore } from './auth.store';

export const authGuard: CanActivateFn = () => {
  const authStore = inject(AuthStore);
  const router = inject(Router);

  const isE2eBypass = typeof window !== 'undefined' && Boolean((window as { __E2E_BYPASS_AUTH__?: boolean }).__E2E_BYPASS_AUTH__);
  if (isE2eBypass) {
    return true;
  }

  if (!authStore.isAuthenticated()) {
    return router.createUrlTree(['/login']);
  }

  return true;
};
