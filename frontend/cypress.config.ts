import { defineConfig } from 'cypress';

export default defineConfig({
  e2e: {
    specPattern: 'cypress/e2e/**/*.cy.ts',
    baseUrl: 'http://localhost:4200',
    supportFile: false,
    video: true,
    screenshotOnRunFailure: true
  }
});
