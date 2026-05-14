import { describe, expect, it } from 'vitest';
import { DashboardFacade } from './dashboard.facade';

describe('DashboardFacade', () => {
  it('allows write actions only for admin role', () => {
    const facade = new DashboardFacade();

    expect(facade.canWrite()).toBe(false);

    facade.setRole('ADMIN');
    expect(facade.canWrite()).toBe(true);
  });
});
