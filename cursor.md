# AI-Assisted Architecture Guardrails (Copy-Paste Identical)

This repo will be developed by different AI tools across a group. Keep the architecture clean and predictable by following these conventions every time you add or change code.

---

## Goals (what we optimize for)

1. Security first: authorization and data-access controls must be correct by construction.
2. Clear layering: domain logic is independent of Spring/Web/DB/S3.
3. Auditability: every important activity produces an audit record.
4. Real-time updates: changes are emitted as events and delivered to clients.

---

## Project Stack

- Java 25
- Spring Boot + Maven
- JUnit tests
- Docker (local dev + integration)
- Server-side templates: Thymeleaf or JTE-templates

## Target Clean Architecture (Spring Boot)

Use a consistent package split under your base package (`org.example.projektarendehantering`).

### Recommended package layout

- `...domain`
  - Pure domain model: entities/aggregates, value objects, domain services, domain policies
  - No Spring annotations
  - No direct JDBC/JPA/S3/Web dependencies

- `...application`
  - Use cases (application services) that orchestrate domain + ports
  - Permission checks happen here (not only in controllers)
  - Defines port interfaces (e.g. `EventPublisher`, `CaseRepository`, `FileStorage`)

- `...presentation`
  - Controllers (REST), WebSocket handlers, request/response DTOs
  - Translates between HTTP/Web payloads and application commands/queries
  - Keep controllers thin: no heavy logic, no direct persistence/S3 calls

- `...infrastructure`
  - Adapters that implement application ports
  - Persistence (JPA repositories, migrations)
  - S3-compatible storage client/adapter
  - Real-time delivery adapter (WebSocket/SSE)
  - Security adapter integrating with Spring Security
  - Framework-specific configuration

- `...common` (optional but recommended)
  - Cross-cutting domain-independent utilities: IDs, error types, time abstraction, shared DTO base types

### Folder structure (mirrors packages)

Maintain the following in `src/main/java`:

```text
src/main/java/
  org/example/projektarendehantering/
    ProjektArendehanteringApplication.java
    common/
    domain/
    application/
    presentation/
    infrastructure/
```

Maintain the following in `src/test/java`:

```text
src/test/java/
  org/example/projektarendehantering/
    domain/        (unit tests)
    application/   (use-case tests with mocks/fakes)
    infrastructure/(adapter tests with testcontainers or fakes)
    presentation/  (controller tests)
```

---

## Naming conventions (enforce consistency)

1. Use-case classes: suffix with `UseCase` or `Service` (pick one style and stick to it).
2. Port interfaces (application boundary): name them as nouns + suffix `Port`.
   - Example: `FileStoragePort`, `CaseEventPublisherPort`
3. Adapter implementations (infrastructure): suffix with `*Adapter` or `*JpaRepository` as appropriate.
   - Example: `S3FileStorageAdapter implements FileStoragePort`
4. Controller classes: suffix with `*Controller`.
5. DTOs:
   - Incoming: `*Request`
   - Outgoing: `*Response`
6. Domain objects:
   - Entities: nouns (e.g. `Case`)
   - Value objects: `*Id`, `*Name`, `*Policy`, etc.

---

## Security & Authorization (central requirement)

### Where checks must happen

- Authorization must be enforced in the `application` layer.
- Controllers must not assume the user is allowed; they pass commands/queries to application services, which verify permissions.

### How to structure authorization

1. Define role/user concepts in `domain` (or `common` if shared).
2. Create an application-level authorization component (port + implementation).
   - Example ports:
     - `CurrentUserPort` (who am I?)
     - `AuthorizationPolicyPort` (am I allowed?)
3. Permission logic lives in domain/application policies, not spread across controllers.

### Audit on security-sensitive actions

- If access is denied, decide whether to audit it (at least log internally).
- If access is granted and data is returned (e.g., file download), audit the event.

---

## File handling & S3 (strict access)

### Recommended approach

1. Store only file metadata + S3 key in the database (not the file bytes).
2. For download:
   - Application service authorizes the user for that specific case + document.
   - Infrastructure adapter streams from S3 only after successful authorization.
3. For upload:
   - Application service authorizes the user for the target case.
   - Infrastructure adapter uploads bytes and returns the stored S3 key + metadata.

### Ports

Create ports in `application`:
- `FileStoragePort` (upload/download/delete)
- `DocumentMetadataRepositoryPort` (persist metadata + link to cases)

Implement in `infrastructure`:
- `S3FileStorageAdapter` (S3-compatible client)

---

## Logging & Audit Trail (transparency)

Every important activity must create an audit event.

### Define an audit event model

In `domain`, define:
- `AuditEvent` (type, timestamp, actor, target identifiers, details)

### Persist and publish audit

In `application`:
- Use an application port like `AuditLogPort` to persist audit events
- Optionally also publish to the real-time system

In `infrastructure`:
- Implement persistence (`AuditLogJpaAdapter`, etc.)
- Optionally forward to external log/stream

---

## Real-time updates (comments, changes, lifecycle events)

### Event-driven pattern

1. Application services create domain events (or audit events that double as domain events).
2. Application publishes events via an application port.
3. Infrastructure delivery adapter sends them to clients.

Example ports:
- `CaseEventPublisherPort` (publish lifecycle/comment/file-activity events)

Example delivery adapters:
- `WebSocketCaseEventsAdapter`
- `SseCaseEventsAdapter` (if using SSE)

---

## AI Tool Usage Rules (how teammates should prompt/code)

1. When you propose code, explain:
   - which layer you touched (`domain`/`application`/`presentation`/`infrastructure`)
   - what port/interface boundary is used
   - where authorization and audit are enforced
2. Never add Spring annotations in `domain`.
3. Never call S3 or DB directly from `presentation`. Use application ports/use cases.
4. Prefer interfaces in `application`; implement them in `infrastructure`.
5. Add tests:
   - domain unit tests for pure behavior
   - application tests for use-case orchestration (mock ports)
   - integration/adapters tests for S3/JPA/WebSocket only when needed
6. Keep configuration:
   - in `src/main/resources` (application properties)
   - framework wiring in `infrastructure` config classes

---

## Initial module checklist (when we start implementing)

At minimum, we will eventually add:

1. `domain`: Case lifecycle model + permissions concepts
2. `application`: use cases for create/follow/assign/update/close; plus ports for files, audit, events, auth context
3. `infrastructure`: S3 adapter, persistence adapters, auth adapter, event delivery adapter
4. `presentation`: REST endpoints + DTOs + WebSocket endpoints

Start small: add one vertical slice (e.g., create case + audit + real-time notification) and repeat.

