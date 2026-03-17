---
agent: speckit.constitution
---

# Agent Workflow Principles

When executing constitution updates and implementation tasks, adhere to these critical directives:

## Current Branch Discipline

- **MUST work on the currently checked-out branch** unless the user explicitly instructs you to create or switch branches.
- **MUST NOT create feature branches** without explicit user instruction.
- **MUST NOT checkout different branches** without explicit user instruction.
- If branch-switching is required to complete a task, request explicit user confirmation before proceeding.

## Phase-Based Implementation Resilience

- **MUST complete each assigned phase** to full completion despite transient repository changes:
  - File modifications by concurrent processes
  - New commits from other sources (CI/CD, other developers)
  - Repository state fluctuations
- **MUST NOT abort or halt execution** without explicit user instruction.
- **MUST treat incomplete phases as blockers** to subsequent phases; verify completion before proceeding.
- **MUST document progress and blockers** in visible task tracking (e.g., manage_todo_list) before continuing.

## Explicit Instruction Requirement

- **MUST only stop execution** when the user provides explicit instruction to abort, halt, or pause.
- Transient errors, file conflicts, or repository changes do NOT justify stopping without user direction.
- When encountering blockers:
  1. Document the blocker clearly in task tracking
  2. Propose recovery actions
  3. Continue execution unless explicitly asked to stop

## Constitution Compliance

- Amendments to this constitution require incrementing the version number according to semantic versioning:
  - MAJOR: incompatible governance changes or principle removals
  - MINOR: new principles or materially expanded guidance
  - PATCH: clarifications and non-semantic refinements
- All Sync Impact Reports MUST be prepended to the updated constitution file
- All agent instructions and templates MUST be validated for consistency with updated principles
