import {
  HttpBackend,
  HttpClient,
  HttpContextToken,
  HttpErrorResponse,
  HttpInterceptorFn
} from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';

import { AuthStore } from '@core/auth/auth.store';

const RETRIED_AFTER_REFRESH = new HttpContextToken<boolean>(() => false);

function readCookie(name: string): string | null {
  if (typeof document === 'undefined') {
    return null;
  }

  const escaped = name.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const match = document.cookie.match(new RegExp(`(^| )${escaped}=([^;]+)`));
  return match ? decodeURIComponent(match[2]) : null;
}

function withBasicHeader<T extends { headers: { has(name: string): boolean }; clone: (update: unknown) => T }>(
  request: T,
  basicHeader: string | null
): T {
  if (!basicHeader || request.headers.has('Authorization')) {
    return request;
  }

  return request.clone({ setHeaders: { Authorization: basicHeader } });
}

export const apiAuthInterceptor: HttpInterceptorFn = (req, next) => {
  const authStore = inject(AuthStore);
  const httpBackend = inject(HttpBackend);
  const rawHttp = new HttpClient(httpBackend);

  const isV3ApiCall = req.url.startsWith('/api/v3/') || req.url.includes('/api/v3/');
  if (!isV3ApiCall) {
    return next(req);
  }

  const request = withBasicHeader(req, authStore.basicAuthHeader());

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      const alreadyRetried = request.context.get(RETRIED_AFTER_REFRESH);
      const csrf = readCookie('XSRF-TOKEN');

      if (error.status !== 401 || alreadyRetried || !csrf) {
        return throwError(() => error);
      }

      return rawHttp.post<{ role: 'USER' | 'ADMIN' }>(
        '/api/v4/auth/refresh',
        {},
        {
          withCredentials: true,
          headers: { 'X-CSRF-Token': csrf }
        }
      ).pipe(
        switchMap((refreshResponse) => {
          authStore.setAuthenticated(refreshResponse.role);

          const retryRequest = withBasicHeader(
            request.clone({ context: request.context.set(RETRIED_AFTER_REFRESH, true) }),
            authStore.basicAuthHeader()
          );

          return next(retryRequest);
        }),
        catchError(() => {
          authStore.clear();
          if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
            window.location.assign('/login');
          }
          return throwError(() => error);
        })
      );
    })
  );
};