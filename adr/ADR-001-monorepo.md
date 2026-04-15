# ADR-001: Monorepo for AlumniConnect

## Status

Accepted

## Context

The team is building several Spring Boot microservices (Config Server, Eureka, API Gateway, Identity, Mentorship, Events) plus shared operational assets (Docker Compose, CI). Work is coordinated for a coursework hand-in with clear ownership per service.

## Decision

Keep all backend modules and top-level automation in a **single Git repository (monorepo)** with one Maven aggregator `pom.xml` at the root and each runnable service under `services/<name>/`.

## Consequences

**Positive**

- One clone gives the full system; reviewers and markers see consistent versions of APIs, gateway routes, and Compose wiring.
- CI runs `mvn verify` once for the whole reactor; integration issues surface early.
- Cross-cutting docs (`README.md`, `docs/`, `adr/`) live beside the code they describe.

**Negative / trade-offs**

- Pull requests must respect **merge boundaries** (feature branches per area) so one person’s gateway or aggregator change does not silently overwrite another’s work without review.
- The root aggregator is shared; changes there need explicit coordination (see team workflow in `README.md`).

## Related

- ADR-002 (Eureka + Config Server)
- ADR-003 (API Gateway)
