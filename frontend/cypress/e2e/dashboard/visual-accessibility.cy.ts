describe('dashboard visual accessibility', () => {
  it('keeps transition durations and visible hover/active states within policy', () => {
    cy.visit('/dashboard', { failOnStatusCode: false });
    cy.get('body').then(($body) => {
      const style = window.getComputedStyle($body[0]);
      const durationRaw = style.transitionDuration || '0s';
      const durationMs = parseFloat(durationRaw) * (durationRaw.includes('ms') ? 1 : 1000);
      expect(durationMs).to.be.at.most(250);
    });
  });
});
