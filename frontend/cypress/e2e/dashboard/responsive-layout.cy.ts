describe('responsive dashboard', () => {
  const viewports: Array<[number, number]> = [[375, 667], [1366, 768]];

  viewports.forEach(([width, height]) => {
    it(`keeps navigation flow at ${width}x${height}`, () => {
      cy.viewport(width, height);
      cy.visit('/dashboard', { failOnStatusCode: false });
      cy.document().then((doc) => {
        expect(doc.body.scrollWidth).to.eq(doc.documentElement.clientWidth);
      });
    });
  });
});
