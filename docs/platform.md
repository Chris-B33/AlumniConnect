# Platform stack (optional extras)

This file is owned by **DevOps / integration**. The **default** Compose stack already includes **PostgreSQL 16** (used by identity, event, and mentorship via Flyway + JPA).

The **`platform`** profile adds **Redis** and **MinIO** for cache / uploads work without changing core services.

## Profile: `platform`

From the repository root (after building JARs as usual):

```bash
docker compose --profile platform up -d
```

Without `--profile platform`, you still get **postgres** plus the Java services; you do **not** get **redis** or **minio**.

## Services

| Service   | When | Host ports | Purpose |
|-----------|------|------------|---------|
| `postgres` | **Always** (default compose) | **5432** | PostgreSQL **16** — `alumni_connect` DB; schemas **`identity`**, **`event`**, **`mentorship`** created/managed by Flyway in each service. |
| `redis`    | Profile `platform` | **6379** | Redis **7** for cache, chat presence, or rate limiting. |
| `minio`    | Profile `platform` | **9000** (S3 API), **9001** (console) | S3-compatible storage for profile images / uploads. |

### Postgres

- **Image:** `postgres:16-alpine`
- **Default user / DB:** see `docker-compose.yml` (`alumni` / `alumni_connect` unless overridden by env file).
- **Schemas:** created by each service’s Flyway migrations (`CREATE SCHEMA IF NOT EXISTS …`). The **`database/*/schema.sql`** files in the repo mirror the DDL for operators and reviews.

**JDBC example (Compose network):**

`jdbc:postgresql://postgres:5432/alumni_connect?currentSchema=mentorship`

(From the host machine, use **`localhost:5432`**.)

### Redis

- **Image:** `redis:7-alpine`
- No password in the default dev definition (suitable only for local Docker networks).

### MinIO

- **Image:** `minio/minio`
- Console: `http://localhost:9001` (default credentials match compose env unless overridden).
- Application S3 endpoint: `http://localhost:9000` from the host; **`http://minio:9000`** from other Compose containers.
