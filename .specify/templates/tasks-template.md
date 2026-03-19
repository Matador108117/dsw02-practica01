---

description: "Task list template for feature implementation"
---

# Tasks: [FEATURE NAME]

**Input**: Design documents from `/specs/[###-feature-name]/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: The examples below include test tasks. Tests are OPTIONAL - only include them if explicitly requested in the feature specification.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Spring backend**: `src/main/java/`, `src/main/resources/`, `src/test/java/`
- **Official frontend**: `frontend/`, `frontend/src/`, `frontend/cypress/`
- **Docker assets**: `docker/` or repository root `docker-compose.yml`
- Paths shown below assume Spring Boot backend - adjust package names per plan.md

<!-- 
  ============================================================================
  IMPORTANT: The tasks below are SAMPLE TASKS for illustration purposes only.
  
  The /speckit.tasks command MUST replace these with actual tasks based on:
  - User stories from spec.md (with their priorities P1, P2, P3...)
  - Feature requirements from plan.md
  - Entities from data-model.md
  - Endpoints from contracts/
  
  Tasks MUST be organized by user story so each story can be:
  - Implemented independently
  - Tested independently
  - Delivered as an MVP increment
  
  DO NOT keep these sample tasks in the generated tasks.md file.
  ============================================================================
-->

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [ ] T001 Create project structure per implementation plan
- [ ] T002 Initialize Java 17 Spring Boot 3 dependencies (web, security, data-jpa, validation)
- [ ] T003 [P] Configure linting and formatting tools
- [ ] T004 [P] Add springdoc-openapi and expose Swagger UI endpoint

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

Examples of foundational tasks (adjust based on your project):

- [ ] T005 Setup PostgreSQL schema and migrations framework
- [ ] T006 [P] Configure Docker Compose for PostgreSQL local/CI runtime
- [ ] T007 [P] Implement mandatory HTTP Basic (`type=http`, `scheme=basic`) on
  API methods with Spring Security, using `correo_electronico` as username and
  role-based authorization (`USER` read-only, `ADMIN` CRUD)
- [ ] T007a [P] Enforce required `correo_electronico` and `contrasena_hash`
  attributes in `empleado` schema/entity validation and migration scripts;
  treat `contrasena` as input-only (no plaintext persistence) and validate
  authentication by hash comparison against `contrasena_hash`
- [ ] T008 [P] Setup API routing and global exception handling
- [ ] T009 [P] Establish API versioning baseline (`/api/v1`) and route conventions
- [ ] T009a [P] If public contract expands or new public endpoints are added,
  create tasks to increment API major version and update migration notes/OpenAPI
- [ ] T009b [P] Implement UTC-based sunset enforcement for deprecated versions
  with `410 Gone` response behavior
- [ ] T009c [P] For Department-domain scope, add schema/model tasks for explicit
  FK-backed `Departamento (1) -> (N) Empleados` relation with nullable employee
  department assignment
- [ ] T010 [P] Scaffold official frontend with Angular 22 LTS + TypeScript,
  enforce Node management via nvm
- [ ] T011 [P] Containerize frontend and integrate service into existing
  docker-compose stack
- [ ] T012 [P] Define reusable pagination request/response model and defaults
- [ ] T013 [P] Add Cypress E2E framework and configure CI build to fail when
  required E2E suite does not pass
- [ ] T014 Create base entities/repositories shared by user stories
- [ ] T015 Configure structured logging and environment configuration

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - [Title] (Priority: P1) 🎯 MVP

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 1 (OPTIONAL - only if tests requested) ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T016 [P] [US1] Contract test for [endpoint] in src/test/java/contract/[Name]ContractTest.java
- [ ] T017 [P] [US1] Integration test for [user journey] in src/test/java/integration/[Name]IT.java
- [ ] T018 [P] [US1] Cypress E2E scenarios in frontend/cypress/e2e/
  (login success/failure, employee rendering, department rendering, CRUD)

### Implementation for User Story 1

- [ ] T019 [P] [US1] Create [Entity1] in src/main/java/.../model/[Entity1].java
- [ ] T020 [P] [US1] Create [Entity2] in src/main/java/.../model/[Entity2].java
- [ ] T021 [US1] Implement [Service] in src/main/java/.../service/[Service].java (depends on T019, T020)
- [ ] T022 [US1] Implement [endpoint/feature] in src/main/java/.../controller/[Controller].java
- [ ] T023 [US1] Add validation, error handling, role auth rules, and pagination behavior
- [ ] T024 [US1] Update OpenAPI annotations with versioned path and verify Swagger rendering

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - [Title] (Priority: P2)

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 2 (OPTIONAL - only if tests requested) ⚠️

- [ ] T025 [P] [US2] Contract test for [endpoint] in src/test/java/contract/[Name]ContractTest.java
- [ ] T026 [P] [US2] Integration test for [user journey] in src/test/java/integration/[Name]IT.java

### Implementation for User Story 2

- [ ] T027 [P] [US2] Create [Entity] in src/main/java/.../model/[Entity].java
- [ ] T028 [US2] Implement [Service] in src/main/java/.../service/[Service].java
- [ ] T029 [US2] Implement [endpoint/feature] in src/main/java/.../controller/[Controller].java
- [ ] T030 [US2] Integrate with User Story 1 components (if needed)

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - [Title] (Priority: P3)

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 3 (OPTIONAL - only if tests requested) ⚠️

- [ ] T031 [P] [US3] Contract test for [endpoint] in src/test/java/contract/[Name]ContractTest.java
- [ ] T032 [P] [US3] Integration test for [user journey] in src/test/java/integration/[Name]IT.java

### Implementation for User Story 3

- [ ] T033 [P] [US3] Create [Entity] in src/main/java/.../model/[Entity].java
- [ ] T034 [US3] Implement [Service] in src/main/java/.../service/[Service].java
- [ ] T035 [US3] Implement [endpoint/feature] in src/main/java/.../controller/[Controller].java

**Checkpoint**: All user stories should now be independently functional

---

[Add more user story phases as needed, following the same pattern]

---

## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] TXXX [P] OpenAPI/Swagger documentation updates in source and docs/
- [ ] TXXX [P] Frontend API integration verification against official endpoints only
- [ ] TXXX [P] Frontend version/tag update using independent format (`vX.Y.Z-front`)
- [ ] TXXX [P] Validate API versioning and pagination consistency across all endpoints
- [ ] TXXX Code cleanup and refactoring
- [ ] TXXX Performance optimization across all stories
- [ ] TXXX [P] Additional unit tests (if requested) in src/test/java/unit/
- [ ] TXXX [P] Cypress E2E full-suite execution evidence attached to PR/build
- [ ] TXXX Security hardening and auth/role regression checks
- [ ] TXXX Confirm commit history is atomic and PR links all tasks/spec requirements
- [ ] TXXX Run quickstart.md validation

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - May integrate with US1 but should be independently testable
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) - May integrate with US1/US2 but should be independently testable

### Within Each User Story

- Tests (if included) MUST be written and FAIL before implementation
- Models before services
- Services before endpoints
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes, all user stories can start in parallel (if team capacity allows)
- All tests for a user story marked [P] can run in parallel
- Models within a story marked [P] can run in parallel
- Different user stories can be worked on in parallel by different team members

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together (if tests requested):
Task: "Contract test for [endpoint] in tests/contract/test_[name].py"
Task: "Integration test for [user journey] in tests/integration/test_[name].py"

# Launch all models for User Story 1 together:
Task: "Create [Entity1] model in src/models/[entity1].py"
Task: "Create [Entity2] model in src/models/[entity2].py"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test User Story 1 independently
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo
4. Add User Story 3 → Test independently → Deploy/Demo
5. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1
   - Developer B: User Story 2
   - Developer C: User Story 3
3. Stories complete and integrate independently

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing
- Commit after each task or logical group with clear, traceable messages
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
