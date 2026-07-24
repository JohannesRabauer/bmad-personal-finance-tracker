# Tech Stack

Fixed for v1. These are load-bearing implementation constraints from the brief — the demo exists partly to show conventional Quarkus/Qute patterns, so the stack is prescribed, not discovered.

- **Language / framework:** Java on **Quarkus**.
- **UI rendering:** **Qute** server-side templating. Server-rendered pages, not a separate SPA frontend.
- **UI form factor:** mobile-friendly responsive web, optimized for quick entry on a phone. No native mobile app.
- **Persistence:** relational database via Hibernate/Panache. **[ASSUMPTION]** Postgres; H2/SQLite is a candidate if zero-dependency self-hosting is preferred (see open question in SPEC.md).
- **Deployment:** single self-hosted instance run on the family's own hardware.

Conventions should stay idiomatic and readable, since the codebase doubles as a BMad worked example.
