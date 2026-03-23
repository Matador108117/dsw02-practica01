type EmpleadoRow = {
  clave: string;
  prefijo: string;
  consecutivo: number;
  nombre: string;
  direccion: string;
  telefono: string;
  correoElectronico: string;
  rol: 'USER' | 'ADMIN';
  activo: boolean;
  departamentoId: string | null;
};

type DepartamentoRow = {
  id: string;
  nombre: string;
};

describe('dashboard crud toggles', () => {
  let empleados: EmpleadoRow[];
  let departamentos: DepartamentoRow[];

  beforeEach(() => {
    empleados = [
      {
        clave: 'EMP-000001',
        prefijo: 'EMP-',
        consecutivo: 1,
        nombre: 'Admin Inicial',
        direccion: 'Av. Central 100',
        telefono: '5551001000',
        correoElectronico: 'admin@empresa.com',
        rol: 'ADMIN',
        activo: true,
        departamentoId: null
      }
    ];

    departamentos = [
      { id: 'DEP-000001', nombre: 'Tecnologia' }
    ];

    cy.intercept('GET', '**/api/v3/empleados*', (req) => {
      req.reply({
        statusCode: 200,
        body: {
          content: empleados,
          page: 0,
          size: 20,
          totalElements: empleados.length,
          totalPages: 1
        }
      });
    }).as('listEmpleados');

    cy.intercept('GET', '**/api/v3/departamentos*', (req) => {
      req.reply({
        statusCode: 200,
        body: {
          content: departamentos,
          page: 0,
          size: 20,
          totalElements: departamentos.length,
          totalPages: 1
        }
      });
    }).as('listDepartamentos');

    cy.intercept('POST', '**/api/v3/empleados', (req) => {
      const body = req.body as {
        nombre: string;
        direccion: string;
        telefono: string;
        correoElectronico: string;
        contrasena: string;
        rol: 'USER' | 'ADMIN';
        departamentoId?: string;
      };

      const created: EmpleadoRow = {
        clave: 'EMP-000002',
        prefijo: 'EMP-',
        consecutivo: 2,
        nombre: body.nombre,
        direccion: body.direccion,
        telefono: body.telefono,
        correoElectronico: body.correoElectronico,
        rol: body.rol,
        activo: true,
        departamentoId: body.departamentoId ?? null
      };

      empleados = [...empleados, created];
      req.reply({ statusCode: 201, body: created });
    }).as('createEmpleado');

    cy.intercept('PUT', '**/api/v3/empleados/*', (req) => {
      const clave = req.url.split('/').pop() ?? '';
      const body = req.body as { nombre?: string; telefono?: string; correoElectronico?: string };
      empleados = empleados.map((item) =>
        item.clave === clave
          ? {
              ...item,
              nombre: body.nombre ?? item.nombre,
              telefono: body.telefono ?? item.telefono,
              correoElectronico: body.correoElectronico ?? item.correoElectronico
            }
          : item
      );
      const updated = empleados.find((item) => item.clave === clave);
      req.reply({ statusCode: 200, body: updated });
    }).as('updateEmpleado');

    cy.intercept('DELETE', '**/api/v3/empleados/*', (req) => {
      const clave = req.url.split('/').pop() ?? '';
      empleados = empleados.filter((item) => item.clave !== clave);
      req.reply({ statusCode: 204, body: '' });
    }).as('deleteEmpleado');

    cy.intercept('POST', '**/api/v3/departamentos', (req) => {
      const body = req.body as { nombre: string };
      const created: DepartamentoRow = {
        id: 'DEP-000002',
        nombre: body.nombre
      };
      departamentos = [...departamentos, created];
      req.reply({ statusCode: 201, body: created });
    }).as('createDepartamento');

    cy.intercept('PUT', '**/api/v3/departamentos/*', (req) => {
      const id = req.url.split('/').pop() ?? '';
      const body = req.body as { nombre: string };
      departamentos = departamentos.map((item) =>
        item.id === id ? { ...item, nombre: body.nombre } : item
      );
      const updated = departamentos.find((item) => item.id === id);
      req.reply({ statusCode: 200, body: updated });
    }).as('updateDepartamento');

    cy.intercept('DELETE', '**/api/v3/departamentos/*', (req) => {
      const id = req.url.split('/').pop() ?? '';
      departamentos = departamentos.filter((item) => item.id !== id);
      req.reply({ statusCode: 204, body: '' });
    }).as('deleteDepartamento');
  });

  it('creates empleado from Agregar action', () => {
    cy.visit('/dashboard', {
      onBeforeLoad: (win) => {
        (win as Window & { __E2E_BYPASS_AUTH__?: boolean }).__E2E_BYPASS_AUTH__ = true;
      }
    });

    cy.wait('@listEmpleados');
    cy.wait('@listDepartamentos');

    cy.contains('button.toggle-card', 'Agregar').click();
    cy.get('input[placeholder="Nombre"]').type('Empleado Cypress');
    cy.get('input[placeholder="Direccion"]').type('Calle E2E 999');
    cy.get('input[placeholder="Telefono"]').type('5552223344');
    cy.get('input[placeholder="Correo"]').type('empleado.cypress@empresa.com');
    cy.get('input[placeholder="Contrasena (min. 8 caracteres)"]').type('Admin123!');
    cy.get('input[placeholder="Departamento ID (opcional, ej. DEP-000001)"]').type('DEP-000001');
    cy.contains('button', 'Guardar').click();

    cy.wait('@createEmpleado')
      .its('request.body')
      .should((body: {
        nombre: string;
        direccion: string;
        telefono: string;
        correoElectronico: string;
        contrasena: string;
        rol: string;
        departamentoId: string;
      }) => {
        expect(body.nombre).to.eq('Empleado Cypress');
        expect(body.direccion).to.eq('Calle E2E 999');
        expect(body.telefono).to.eq('5552223344');
        expect(body.correoElectronico).to.eq('empleado.cypress@empresa.com');
        expect(body.rol).to.eq('USER');
        expect(body.departamentoId).to.eq('DEP-000001');
      });
  });

  it('edits empleado from Editar action', () => {
    cy.visit('/dashboard', {
      onBeforeLoad: (win) => {
        (win as Window & { __E2E_BYPASS_AUTH__?: boolean }).__E2E_BYPASS_AUTH__ = true;
      }
    });

    cy.wait('@listEmpleados');
    cy.wait('@listDepartamentos');

    cy.contains('button.toggle-card', 'Editar').click();
    cy.get('input[placeholder="Clave (ej. EMP-000001)"]').clear().type('EMP-000001');
    cy.get('input[placeholder="Nombre (opcional)"]').type('Admin Inicial Editado');
    cy.get('input[placeholder="Telefono (opcional)"]').type('5550001111');
    cy.get('input[placeholder="Correo (opcional)"]').type('admin.editado@empresa.com');
    cy.contains('button', 'Actualizar').click();

    cy.wait('@updateEmpleado')
      .its('request.body')
      .should((body: { nombre?: string; telefono?: string; correoElectronico?: string }) => {
        expect(body.nombre).to.eq('Admin Inicial Editado');
        expect(body.telefono).to.eq('5550001111');
        expect(body.correoElectronico).to.eq('admin.editado@empresa.com');
      });
  });

  it('deletes empleado from Eliminar action', () => {
    cy.visit('/dashboard', {
      onBeforeLoad: (win) => {
        (win as Window & { __E2E_BYPASS_AUTH__?: boolean }).__E2E_BYPASS_AUTH__ = true;
      }
    });

    cy.wait('@listEmpleados');
    cy.wait('@listDepartamentos');

    cy.contains('button.toggle-card', 'Eliminar').click();
    cy.get('input[placeholder="Clave a eliminar"]').type('EMP-000001');
    cy.get('section.action-panel').contains('button', 'Eliminar').click();

    cy.wait('@deleteEmpleado');
  });

  it('creates departamento from Agregar action', () => {
    cy.visit('/dashboard', {
      onBeforeLoad: (win) => {
        (win as Window & { __E2E_BYPASS_AUTH__?: boolean }).__E2E_BYPASS_AUTH__ = true;
      }
    });

    cy.wait('@listEmpleados');
    cy.wait('@listDepartamentos');

    cy.contains('button.entity-link', 'Departamentos').click();
    cy.contains('h3', 'Departamentos').should('be.visible');

    cy.contains('button.toggle-card', 'Agregar').click();
    cy.get('input[placeholder="Nombre del departamento"]').type('Calidad E2E');
    cy.contains('button', 'Guardar').click();

    cy.wait('@createDepartamento')
      .its('request.body')
      .should('deep.equal', { nombre: 'Calidad E2E' });
  });

  it('edits departamento from Editar action', () => {
    cy.visit('/dashboard', {
      onBeforeLoad: (win) => {
        (win as Window & { __E2E_BYPASS_AUTH__?: boolean }).__E2E_BYPASS_AUTH__ = true;
      }
    });

    cy.wait('@listEmpleados');
    cy.wait('@listDepartamentos');

    cy.contains('button.entity-link', 'Departamentos').click();
    cy.contains('h3', 'Departamentos').should('be.visible');

    cy.contains('button.toggle-card', 'Editar').click();
    cy.get('input[placeholder="ID"]').type('DEP-000001');
    cy.get('input[placeholder="Nombre"]').type('Tecnologia Editado');
    cy.contains('button', 'Actualizar').click();

    cy.wait('@updateDepartamento')
      .its('request.body')
      .should('deep.equal', { nombre: 'Tecnologia Editado' });
  });

  it('deletes departamento from Eliminar action', () => {
    cy.visit('/dashboard', {
      onBeforeLoad: (win) => {
        (win as Window & { __E2E_BYPASS_AUTH__?: boolean }).__E2E_BYPASS_AUTH__ = true;
      }
    });

    cy.wait('@listEmpleados');
    cy.wait('@listDepartamentos');

    cy.contains('button.entity-link', 'Departamentos').click();
    cy.contains('h3', 'Departamentos').should('be.visible');

    cy.contains('button.toggle-card', 'Eliminar').click();
    cy.get('input[placeholder="ID a eliminar"]').type('DEP-000001');
    cy.get('section.action-panel').contains('button', 'Eliminar').click();

    cy.wait('@deleteDepartamento');
  });
});
