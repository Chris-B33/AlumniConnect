# ADR-003: Spring Cloud Gateway as the HTTP edge

## Status

Accepted

## Context

Multiple services expose similar paths (for example `/api/ping`). Browsers and a future SPA expect **one origin** for APIs, with **CORS** handled consistently. Internal services register with Eureka under logical names.

## Decision

Expose **Spring Cloud Gateway** on port **8080** as the primary HTTP entry. Routes use a **first-path segment** (`/identity`, `/mentorship`, `/event`) with **`StripPrefix=1`**, forwarding to `lb://<service-id>` so downstream apps keep a uniform `/api/...` layout without path collisions.

## Consequences

**Positive**

- Single public port for API traffic; CORS is configured once on the gateway.
- Path-based routing maps cleanly to bounded contexts and team ownership.
- Load balancing via Eureka integrates without hard-coding hostnames in clients.

**Negative / trade-offs**

- Gateway configuration is a **shared merge hotspot**; changes require review alongside service route contracts.
- Authentication/authorisation at the edge (future work) must align with identity service contracts.

## Related

- ADR-001 (Monorepo)
- ADR-002 (Eureka + Config)
