describe('departamento empleados toggle', () => {
  it('shows view empleados by departamento action', () => {
    cy.visit('/dashboard', {
      failOnStatusCode: false,
      onBeforeLoad: (win) => {
        (win as Window & { __E2E_BYPASS_AUTH__?: boolean }).__E2E_BYPASS_AUTH__ = true;
      }
    });
    cy.contains(/Ver empleados del departamento/i);
  });
});
