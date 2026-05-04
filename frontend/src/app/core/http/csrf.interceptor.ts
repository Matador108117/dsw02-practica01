import { HttpInterceptorFn } from '@angular/common/http';

function readCookie(name: string): string | null {
  const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
  return match ? decodeURIComponent(match[2]) : null;
}

export const csrfInterceptor: HttpInterceptorFn = (req, next) => {
  const csrf = readCookie('XSRF-TOKEN');
  if (!csrf) {
    return next(req);
  }

  const isSensitive = ['POST', 'PUT', 'PATCH', 'DELETE'].includes(req.method);
  if (!isSensitive) {
    return next(req);
  }

  return next(req.clone({ setHeaders: { 'X-CSRF-Token': csrf }, withCredentials: true }));
};
