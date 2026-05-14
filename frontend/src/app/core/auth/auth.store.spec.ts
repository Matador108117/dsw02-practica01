import { beforeEach, describe, expect, it } from 'vitest';
import { AuthStore } from './auth.store';

type BtoaLike = (input: string) => string;

const ensureBtoa = () => {
  if (typeof globalThis.btoa !== 'function') {
    globalThis.btoa = ((input: string) => Buffer.from(input, 'binary').toString('base64')) as BtoaLike;
  }
};

describe('AuthStore', () => {
  beforeEach(() => {
    ensureBtoa();
    sessionStorage.clear();
  });

  it('persists and exposes the basic auth header on setSession', () => {
    const store = new AuthStore();

    store.setSession('ADMIN', 'admin@empresa.com', 'Admin123!');

    const expectedHeader = `Basic ${btoa('admin@empresa.com:Admin123!')}`;
    expect(store.basicAuthHeader()).toBe(expectedHeader);
    expect(sessionStorage.getItem('empleados.basicAuthHeader')).toBe(expectedHeader);
    expect(store.isAuthenticated()).toBe(true);
    expect(store.role()).toBe('ADMIN');
  });

  it('restores the stored basic auth header on init', () => {
    const expectedHeader = `Basic ${btoa('user@empresa.com:Secret123!')}`;
    sessionStorage.setItem('empleados.basicAuthHeader', expectedHeader);

    const store = new AuthStore();

    expect(store.basicAuthHeader()).toBe(expectedHeader);
  });

  it('clears all auth state on clear', () => {
    const store = new AuthStore();
    store.setSession('USER', 'user@empresa.com', 'Secret123!');

    store.clear();

    expect(store.isAuthenticated()).toBe(false);
    expect(store.role()).toBe(null);
    expect(store.basicAuthHeader()).toBe(null);
    expect(sessionStorage.getItem('empleados.basicAuthHeader')).toBe(null);
  });
});
