# Platform stack (optional)

This file is owned by **DevOps / integration**. It describes **optional** infrastructure that does **not** start with the default Compose stack, so backend and frontend work can continue without JDBC or object storage wired in.

## Profile: `platform`

From the repository root (after building JARs as usual):

```bash
docker compose --profile platform up -d
```

To run **Java stack + platform containers**:

```bash
docker compose --profile platform up
```

Without `--profile platform`, behaviour matches the historical setup: **config-server**, **eureka-server**, **identity**, **mentorship**, **event**, **api-gateway** only.

## Services (when profile is enabled)

| Service   | Host ports | Purpose |
|-----------|------------|---------|
| `postgres` | **5432** | PostgreSQL **16** for future persistence (identity, mentorship, events). |
| `redis`    | **6379** | Redis **7** for future cache, chat presence, or rate limiting. |
| `minio`    | **9000** (S3 API), **9001** (console) | S3-compatible storage for future profile images / uploads. |

### Postgres

- **Image:** `postgres:16-alpine`
- **Default user / DB:** see `docker-compose.yml` (`alumni` / `alumni_connect` unless overridden by env file).
- **Init:** `docker/platform/postgres/init/001-schemas.sql` creates schemas `identity`, `mentorship`, `event` for a single-database multi-schema layout. Teams may switch to **separate databases** per service later via Flyway without changing the Compose service name `postgres`.

**JDBC example (for later use in Spring, not active today):**

`jdbc:postgresql://postgres:5432/alumni_connect?currentSchema=mentorship`

(From a Spring app in Compose, hostname is **`postgres`**. From the host machine, use **`localhost:5432`**.)

### Redis

- **Image:** `redis:7-alpine`
- No password in the default dev definition (suitable only for local Docker networks).

### MinIO

- **Image:** `minio/minio`
- Console: `http://localhost:9001` (default credentials match compose env unless overridden).
- Application S3 endpoint: `http://localhost:9000` from the host; **`http://minio:9000`** from other Compose containers.

## Coordination (who consumes what)

| Platform piece | Primary consumers (when they integrate) |
|----------------|-------------------------------------------|
| Postgres       | **Mark** (identity), **Michael** (mentorship, events) — add Flyway + datasource; **Noa** keeps Compose + documented env. |
| MinIO          | **Mark** / **Chris** (profile images) — presigned URLs or gateway upload; **Noa** tunes gateway body limits if traffic goes through gateway. |
| Redis          | **Michael** / **Mark** (sessions, chat presence, idempotency) — **Noa** exposes service only. |

## Gateway / WebSocket (future)

When **chat** is implemented, agree one of:

- **Dedicated WebSocket route** on `api-gateway` (e.g. `/chat/ws/**` → chat or mentorship service), or  
- **Separate host/port** for WS (document in README and `frontend/.env.example`).

No gateway route is enabled in this scaffold to avoid interfering with current routing.
