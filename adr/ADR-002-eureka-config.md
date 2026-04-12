# ADR-002: Netflix Eureka + Spring Cloud Config Server

## Status

Accepted

## Context

Microservices need **service discovery** (dynamic instances, Docker hostnames) and **externalised configuration** (ports, messages, non-secret placeholders) without rebuilding JARs for every tweak.

## Decision

Use **Spring Cloud Netflix Eureka** for the registry and **Spring Cloud Config Server** (native filesystem backend under `services/config-server/src/main/resources/config/`) as the single configuration source for Eureka clients.

Services import config via `spring.config.import` (for example `configserver:http://localhost:8888` locally, `configserver:http://config-server:8888` in Compose).

## Consequences

**Positive**

- The gateway can route with `lb://service-id` once instances register.
- Environment-specific overrides can be layered without forking Java code.
- Compose can sequence startup using health checks on Config and Eureka.

**Negative / trade-offs**

- Operational complexity: Config Server and Eureka must be **available before** dependent services in a cold start (documented run order in `README.md`).
- Secrets must not be committed; use placeholders and environment-specific secret stores for real deployments.

## Related

- ADR-001 (Monorepo)
- ADR-003 (API Gateway)
