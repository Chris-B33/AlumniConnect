# AlumniConnect  
### CS4135 – Software Architectures  
University of Limerick  

<a href="https://github.com/Chris-B33/AlumniConnect/wiki"><img alt="Static Badge" src="https://img.shields.io/badge/wiki-green"></a>

---

## Repository governance

- **Root `pom.xml`, `docker-compose.yml`, and `.github/workflows/**`:** only **Noa** changes these unless another teammate is **delegated in writing** (for example email, chat, or a short doc with Noa’s explicit sign-off).
- **`services/config-server/src/main/resources/config/application.yml`:** only **one person** edits this file **per sprint**; agree the assignee at sprint start so two people do not ship overlapping PRs against it in the same sprint.

---

## Project Overview  

**AlumniConnect** is a distributed, microservices-based web platform designed to facilitate structured engagement between university alumni and current students.

The system supports:

- Mentorship lifecycle management  
- Alumni networking events  
- Career opportunity sharing  
- Secure role-based authentication  
- Administrative oversight  

The architecture has been designed in alignment with **ISO/IEC 42010**, explicitly defining:

- Architectural **elements**
- System **relationships**
- Guiding **design principles**

Architectural decisions are driven primarily by **quality attributes** including scalability, security, maintainability, modifiability, and reliability.

---

# Architectural Design  

## Architectural Style  

AlumniConnect adopts a hybrid architectural strategy combining:

- **Microservices Architecture**
- **Layered Backend Architecture**
- **Component-Based Frontend Architecture**

This design reflects trade-off analysis discussed in the Software Architectures module.

### Why Microservices?

**Advantages**
- Independent service scaling  
- Domain isolation  
- Clear service boundaries  
- Independent deployment potential  

**Trade-offs**
- Increased infrastructure complexity  
- Inter-service communication overhead  
- Gateway configuration requirements  

The microservices model was selected to prioritise scalability and long-term extensibility over structural simplicity.

---

### Benefits

- Separation of concerns  
- Improved testability  
- Reduced coupling  
- Enhanced maintainability  

DTOs are used to decouple internal entity models from external API contracts, preserving abstraction and supporting future system evolution.

---

# Frontend Architecture  

The frontend follows a modular component-based architecture:

- Functional components
- Reusable UI elements
- Redux Toolkit for global state management
- Async lifecycle handling (pending, fulfilled, rejected)
- Protected routes
- Axios interceptors for 401 handling
- Role-based route control
- Environment variable configuration

The structure supports maintainability, scalability, and clean separation of responsibilities.

---

# Security Architecture  

Security is a primary architectural driver.

The system implements:

- Stateless authentication using JSON Web Tokens (JWT)
- Custom authentication filters
- Role-Based Access Control (RBAC) via `@PreAuthorize`
- CORS configuration for frontend-backend integration
- Standardised error responses via global exception handling

Security decisions are aligned with scalability requirements and stateless service principles.

---

# Quality Attributes  

The architecture prioritises the following quality attributes:

| Quality Attribute | Architectural Mechanism |
|-------------------|--------------------------|
| Scalability | Independent microservices |
| Security | JWT + RBAC |
| Maintainability | Layered backend structure |
| Modifiability | Clear domain separation |
| Reliability | Global exception handling |
| Traceability | Structured Git workflow |

Architectural decisions are continuously evaluated against these quality goals.

---

## Backend services (Maven modules)

| Module | Role | Default port (local / Compose host) |
|--------|------|--------------------------------------|
| `services/config-server` | Spring Cloud Config (native repo under `config/`) | **8888** |
| `services/eureka-server` | Service registry | **8761** |
| `services/api-gateway` | Spring Cloud Gateway + Eureka client + load balancer | **8080** |
| `services/identity-service` | Identity API | **8081** |
| `services/mentorship-service` | Mentorship API (calls identity via `@LoadBalanced` RestTemplate, service id **`identity-service`**) | **8082** |
| `services/event-service` | Events API | **8083** |

Root **`pom.xml`** is an aggregator: run **`mvn verify`** (or **`mvn clean package`**) from the repository root to build every module.

---

## API Gateway

The **`api-gateway`** service (port **8080**) is the single HTTP entry point for routed traffic to services registered in Eureka.

**Path prefixes** — each downstream app serves **`/api/...`** on its own container port; the gateway uses a **first-path-segment** prefix so identical paths (for example `/api/ping`) do not clash. Routes use **`StripPrefix=1`**, so the first segment (`identity`, `mentorship`, or `event`) is removed before the request is forwarded.

| Prefix on gateway (`http://localhost:8080`) | Target |
|-----------------------------------------------|--------|
| `/identity/**` | `lb://identity-service` |
| `/mentorship/**` | `lb://mentorship-service` |
| `/event/**` | `lb://event-service` |

**Examples** (with Config, Eureka, backends, and gateway running):

- `http://localhost:8080/identity/api/identity/status`
- `http://localhost:8080/mentorship/api/mentorship/check`
- `http://localhost:8080/event/api/ping`

**CORS** is enabled for typical Vite (**`5173`**) and CRA (**`3000`**) dev origins.

---

## Docker Compose

**Prerequisites:** [Docker](https://docs.docker.com/get-docker/) with Compose v2, and fat JARs built from the repo root.

From the repository root:

1. `mvn clean package -DskipTests`
2. `docker compose up`

Compose project name: **`alumniconnect`**. Images use **`eclipse-temurin:17-jre`**; each service runs **`java -jar`** against the matching **`target/*-0.0.1-SNAPSHOT.jar`** mounted read-only.

| Service | Host port | Notes |
|---------|-----------|--------|
| config-server | 8888 | No Eureka registration |
| eureka-server | 8761 | Dashboard: `http://localhost:8761` |
| api-gateway | 8080 | Uses Eureka only (config is classpath-based) |
| identity-service | 8081 | |
| mentorship-service | 8082 | |
| event-service | 8083 | |

**Environment (Compose):**

- **`ALUMNI_CONFIG_IMPORT=configserver:http://config-server:8888`** — Config Server URL for **eureka-server**, **identity-service**, **mentorship-service**, and **event-service**. In each service, `application.yml` uses `${ALUMNI_CONFIG_IMPORT:configserver:http://localhost:8888}` so a normal local run still targets **`http://localhost:8888`** when the variable is unset.
- **`EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/`** — Eureka for all Eureka clients in Compose.

**Healthchecks:** TCP checks on **config-server:8888** and **eureka-server:8761** so dependent containers start only after those ports accept connections.

---

## Verifying the stack

After **`docker compose up`**, quick checks:

- **Eureka:** `http://localhost:8761` — instances for API-GATEWAY, IDENTITY-SERVICE, MENTORSHIP-SERVICE, EVENT-SERVICE should be **UP**.
- **Actuator (JSON):** `http://localhost:8081/actuator/health`, `8082`, `8083`, `8080` — expect **`"status":"UP"`**.
- **Through the gateway:** the three example URLs under [API Gateway](#api-gateway) should return **HTTP 200**.

### Identity — register and login (JWT)

`POST /api/auth/register` and `POST /api/auth/login` on **identity-service** accept JSON bodies. Through the gateway, prefix with **`/identity`** (first segment stripped before forwarding).

**Register** (direct to service):

```http
POST http://localhost:8081/api/auth/register
Content-Type: application/json

{"email":"student@example.com","password":"password123","role":"Student"}
```

**Login** (via gateway):

```http
POST http://localhost:8080/identity/api/auth/login
Content-Type: application/json

{"email":"student@example.com","password":"password123"}
```

Responses are JSON: **`accessToken`** (JWT), **`tokenType`** (`Bearer`), **`expiresInSeconds`**. `role` must be **`Student`** or **`Alumni`**. Password must be at least **8** characters. Users are stored **in memory** in this thin slice (restart clears them). Signing material comes from **`identity-service.yml`** on the Config Server (`jwt.secret`, `jwt.expiration-seconds`); do not commit real production secrets.

---

## Continuous integration

GitHub Actions workflow **`.github/workflows/ci.yml`** runs **`mvn -B verify`** on pushes and pull requests to **`main`** and **`develop`** (JDK **17**, Temurin, Maven cache enabled).

---

