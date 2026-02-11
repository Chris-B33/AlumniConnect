# AlumniConnect  
### CS4135 – Software Architectures  
University of Limerick  

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

