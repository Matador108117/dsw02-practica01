# Frontend Release Policy

## Versioning

Frontend versions are independent from backend API versions.
Tag format: `vMAJOR.MINOR.PATCH-front`.

## Release verification gate

A release is valid only if:

1. Footer in dashboard displays the same frontend version shipped in artifact metadata.
2. Docker image tag includes frontend version and git SHA.
3. CI stores Cypress evidence artifacts for the release candidate.

## Traceability checklist

- Git tag references commit SHA.
- Commit SHA is present in image metadata/label.
- PR links to tasks and spec requirements.
