describe('auth login and logout', () => {
  it('redirects unauthenticated users to login when visiting dashboard', () => {
    cy.visit('/dashboard', { failOnStatusCode: false });
    cy.url().should('include', '/login');
  });

  it('logs in successfully and navigates to dashboard', () => {
    cy.intercept('POST', '**/api/v4/auth/login', {
      statusCode: 200,
      body: { status: 'ACCEPTED', role: 'ADMIN' }
    }).as('loginSuccess');

    cy.intercept('GET', '**/api/v3/empleados*', {
      statusCode: 200,
      body: {
        content: [
          {
            clave: 'EMP-000001',
            prefijo: 'EMP-',
            consecutivo: 1,
            nombre: 'Admin Principal',
            direccion: 'Calle 1',
            telefono: '5551001000',
            correoElectronico: 'admin@empresa.com',
            rol: 'ADMIN',
            activo: true,
            departamentoId: null
          }
        ],
        page: 0,
        size: 20,
        totalElements: 1,
        totalPages: 1
      }
    }).as('listEmpleados');

    cy.intercept('GET', '**/api/v3/departamentos*', {
      statusCode: 200,
      body: {
        content: [
          { id: 'DEP-000001', nombre: 'Tecnologia' }
        ],
        page: 0,
        size: 20,
        totalElements: 1,
        totalPages: 1
      }
    }).as('listDepartamentos');

    cy.visit('/login');
    cy.get('input[type="email"]').type('admin@empresa.com');
    cy.get('input[type="password"]').type('Admin123!');
    cy.contains('button', 'Entrar').click();

    cy.wait('@loginSuccess')
      .its('request.body')
      .should('deep.equal', { email: 'admin@empresa.com', password: 'Admin123!' });
    cy.wait('@listEmpleados');
    cy.wait('@listDepartamentos');
    cy.url().should('include', '/dashboard');
    cy.contains('h1', 'Dashboard Operativo').should('be.visible');
  });

  it('shows message for invalid credentials', () => {
    cy.intercept('POST', '**/api/v4/auth/login', {
      statusCode: 401,
      body: {
        code: 'AUTH_INVALID_CREDENTIALS',
        message: 'Invalid email or password',
        timestamp: '2026-03-23T00:00:00Z'
      }
    }).as('loginInvalid');

    cy.visit('/login');
    cy.get('input[type="email"]').type('admin@empresa.com');
    cy.get('input[type="password"]').type('wrong-password');
    cy.contains('button', 'Entrar').click();

    cy.wait('@loginInvalid');
    cy.contains('Credenciales invalidas').should('be.visible');
    cy.url().should('include', '/login');
  });

  it('logs out and returns to login', () => {
    cy.intercept('GET', '**/api/v3/empleados*', {
      statusCode: 200,
      body: {
        content: [],
        page: 0,
        size: 20,
        totalElements: 0,
        totalPages: 0
      }
    }).as('listEmpleadosEmpty');

    cy.intercept('GET', '**/api/v3/departamentos*', {
      statusCode: 200,
      body: {
        content: [],
        page: 0,
        size: 20,
        totalElements: 0,
        totalPages: 0
      }
    }).as('listDepartamentosEmpty');

    cy.intercept('POST', '**/api/v4/auth/logout', {
      statusCode: 204,
      body: ''
    }).as('logout');

    cy.visit('/dashboard', {
      onBeforeLoad: (win) => {
        (win as Window & { __E2E_BYPASS_AUTH__?: boolean }).__E2E_BYPASS_AUTH__ = true;
      }
    });

    cy.wait('@listEmpleadosEmpty');
    cy.wait('@listDepartamentosEmpty');
    cy.contains('button', 'Cerrar sesion').click();
    cy.wait('@logout');
    cy.url().should('include', '/login');
    cy.contains('h1', 'Iniciar sesion').should('be.visible');
  });
});
