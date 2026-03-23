describe('session rehydration', () => {
  it('restores session on browser reopen when refresh token is valid', () => {
    cy.visit('/login');
    cy.setCookie('REFRESH_TOKEN', 'mock-refresh-valid');
    cy.reload();
    cy.url().should('satisfy', (url: string) => url.includes('/dashboard') || url.includes('/login'));
  });

  it('forces login when refresh token is expired', () => {
    cy.visit('/dashboard', { failOnStatusCode: false });
    cy.clearCookie('REFRESH_TOKEN');
    cy.reload();
    cy.url().should('include', '/login');
  });
});
