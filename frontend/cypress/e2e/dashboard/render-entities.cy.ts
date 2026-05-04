describe('dashboard render entities', () => {
  it('renders empleados and departamentos views', () => {
    cy.visit('/dashboard', {
      failOnStatusCode: false,
      onBeforeLoad: (win) => {
        (win as Window & { __E2E_BYPASS_AUTH__?: boolean }).__E2E_BYPASS_AUTH__ = true;
      }
    });
    cy.contains(/Empleados|Departamentos|Tabla/i);
  });
});
