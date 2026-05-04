import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { AppComponent } from './app/app.component';
import { APP_ROUTES } from './app/app.routes';
import { csrfInterceptor } from './app/core/http/csrf.interceptor';
import { apiAuthInterceptor } from './app/core/http/api-auth.interceptor';

bootstrapApplication(AppComponent, {
  providers: [provideRouter(APP_ROUTES), provideHttpClient(withInterceptors([apiAuthInterceptor, csrfInterceptor]))]
}).catch((err) => console.error(err));
