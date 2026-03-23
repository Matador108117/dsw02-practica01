describe('dashboard render performance', () => {
  it('tracks first render under threshold', () => {
    const start = Date.now();
    cy.visit('/dashboard', {
      failOnStatusCode: false,
      onBeforeLoad: (win) => {
        (win as Window & { __E2E_BYPASS_AUTH__?: boolean }).__E2E_BYPASS_AUTH__ = true;
      }
    });
    cy.contains(/Tabla|Dashboard|Front para dsw02-practica01/i).then(() => {
      const elapsed = Date.now() - start;
      expect(elapsed).to.be.lessThan(2500);
    });
  });
});
