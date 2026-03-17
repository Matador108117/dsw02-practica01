# GitHub Pull Request Template

## Description
<!-- Describe the changes in this pull request. Link to related feature or issue. -->

## Feature Branch Information
- **Feature ID**: <!-- e.g., 004 -->
- **Branch Name**: <!-- e.g., 004-empleado-auth-roles -->
- **Related Spec**: <!-- e.g., /specs/004-empleado-auth-roles/spec.md -->

## Tasks Completed
<!-- List all tasks completed in this PR. Use task IDs from tasks.md -->
- [ ] T###: Task description
- [ ] T###: Task description

## Type of Change
- [ ] Bug fix (non-breaking change fixing an issue)
- [ ] New feature (non-breaking change adding functionality)
- [ ] Breaking change (fix or feature causing existing functionality to change)
- [ ] Documentation update

## Testing
<!-- Describe the testing performed -->
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Contract tests validated
- [ ] Manual testing completed

## Code Quality Checklist
- [ ] Code follows project style guide
- [ ] Comments added for complex logic
- [ ] No new warnings introduced
- [ ] Tests pass locally: `mvn clean test`
- [ ] Build passes: `mvn clean package`

## Security & Data Considerations
- [ ] No plaintext credentials or sensitive data exposed
- [ ] No SQL injection vulnerabilities
- [ ] No XSS vulnerabilities
- [ ] Password hashing properly implemented (if applicable)
- [ ] API contract validated against OpenAPI spec
- [ ] Database migrations tested for idempotency

## Performance
- [ ] No performance regressions
- [ ] Large query performance validated
- [ ] Cache strategy validated (if applicable)

## Documentation
- [ ] README updated (if applicable)
- [ ] API documentation updated (OpenAPI contract)
- [ ] Inline code comments added (if applicable)
- [ ] Quickstart.md updated (if applicable)

## Constitution & Governance
- [ ] Branch follows naming convention: `feature/*`, `fix/*`, or `chore/*`
- [ ] Commits are atomic and meaningful
- [ ] No mix of unrelated concerns (one feature per PR)
- [ ] All required checklists completed
- [ ] No breaking API changes without major version bump

## Breaking Changes
- [ ] No breaking changes, OR
- [ ] Breaking changes documented and version bumped to new major version

## Screenshots (if applicable)
<!-- Add screenshots or diagrams if helpful -->

## Additional Notes
<!-- Any additional context or notes for reviewers -->

---

**Ready for Review**: Yes / No
