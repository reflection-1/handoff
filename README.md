# handoff

Important context often disappears between shifts. **handoff** is a small full-stack application for passing unfinished work, ownership and status updates to whoever starts next.

The scenario is inspired by familiar retail and student-team workflows, but every name and handoff in the demo is fictional.

## what it does

- creates structured shift-handoff notes with an area, owner, shift and priority
- moves work through `new`, `acknowledged` and `done` states
- prevents invalid backwards status changes
- keeps an audit history for every note
- searches and filters handoffs through the API
- summarizes open work and high-priority items
- persists data locally in an H2 database
- returns structured validation and error responses

## stack

- Java 21
- Spring Boot 3.5
- Spring Web and Jakarta Validation
- Spring Data JPA
- H2 database
- JUnit 5 and MockMvc
- HTML, CSS and vanilla JavaScript

## run it

You need Java 21 or newer and Maven 3.6.3 or newer.

```bash
cd handoff
mvn spring-boot:run
```

Open `http://localhost:8080`.

The database is written to `data/`, which is intentionally excluded from Git. Delete that directory if you deliberately want to restore the fictional seed data on the next run.

## test it

```bash
mvn test
```

The integration tests cover creation, validation, allowed status transitions, rejected backwards transitions and audit-history persistence.

## API

| method | route | purpose |
| --- | --- | --- |
| `GET` | `/api/handoffs` | list and filter handoffs |
| `POST` | `/api/handoffs` | create a validated handoff |
| `PATCH` | `/api/handoffs/{id}/status` | move a handoff to its next state |
| `GET` | `/api/handoffs/{id}/history` | read the audit trail |
| `GET` | `/api/metrics` | read board-level metrics |

Supported query parameters for `GET /api/handoffs` are `status`, `priority`, `shiftType` and `query`.

## design decisions

**status changes are explicit.** A handoff can move forward from new to acknowledged or done, but a completed item cannot silently become new again. Invalid transitions return HTTP `409 Conflict`.

**history is stored separately.** The current handoff is easy to query, while the event table preserves how its status changed over time.

**the interface and API fail clearly.** Validation errors identify the affected fields, and the browser checks that responses are actually JSON before trying to parse them.

**the dataset is fictional.** The project demonstrates a realistic workflow without exposing customer, employee or employer information.

**the visual direction supports the workflow.** A cool, tactile kanban layout makes each state feel distinct without relying on colour alone. The faded workspace photograph is by [Walter Randlehoff on Unsplash](https://unsplash.com/photos/book-beside-laptop-computer-2QRq_eSzdgQ) and is used under the Unsplash licence.
