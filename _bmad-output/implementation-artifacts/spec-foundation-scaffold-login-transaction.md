---
title: 'Foundation slice: Quarkus scaffold, static login, log & list transactions'
type: 'feature'
created: '2026-07-24'
status: 'done'
review_loop_iteration: 0
baseline_commit: '5e336e5df847490207ea73dc49d3743bcf117dad'
context:
  - '{project-root}/_bmad-output/specs/spec-personal-finance-tracker/SPEC.md'
  - '{project-root}/_bmad-output/specs/spec-personal-finance-tracker/stack.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** The Personal Finance Tracker has a validated SPEC and brief but no code. We need the first runnable vertical slice so a family member can log in and record/see their own transactions — the foundation every later capability (filtering, dashboard) builds on.

**Approach:** Scaffold a Quarkus + Qute + H2 (file-based) app. Add demo-grade form authentication with 2–3 users hard-coded in `application.properties`. Model a `Transaction` (Panache) owned by a username, with a fixed `Category` enum. Serve a mobile-friendly server-rendered page to add an in/out transaction and list the logged-in user's own transactions.

## Boundaries & Constraints

**Always:**
- Quarkus + Qute server-side templates; Hibernate ORM + Panache; H2 file-based persistence (survives restart).
- Every transaction is owned by the authenticated username; a user only sees/acts on their own rows (CAP-5).
- Amounts are `BigDecimal` shown as EUR (€); dates are `LocalDate`; categories come from a fixed `Category` enum.
- Idiomatic, readable code — this doubles as a BMad worked example.

**Ask First:**
- Any dependency beyond: quarkus-rest, quarkus-rest-qute, quarkus-hibernate-orm-panache, quarkus-jdbc-h2, quarkus-security, quarkus-junit5, rest-assured.
- Changing the auth mechanism (Quarkus form auth + embedded users) or the DB choice.

**Never:**
- Real auth (registration, password reset, hashing/roles beyond one demo role).
- Filtering UI, category management, charts/dashboard, bank import, multi-currency — deferred to later specs.
- Exposing or letting a user mutate another user's transactions.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Log valid OUT | Authed as alice; amount=12.50, date=today, type=OUT, category=GROCERIES | Row persisted with owner=alice; redirect to list showing it | N/A |
| Log valid IN | Authed as bob; amount=2000, type=IN, category=SALARY | Row persisted with owner=bob | N/A |
| List is scoped | Authed as alice; bob also has rows | List shows only alice's rows | N/A |
| Invalid amount | amount missing or <= 0 | Re-render form with error message; nothing persisted | Field validation |
| Unauthenticated | No session; GET /transactions | Redirected to login page | 302 to /login |
| Bad credentials | Wrong username/password on login | Login page re-shown as failed | Redirect to login error |

</frozen-after-approval>

## Code Map

- `pom.xml` -- Quarkus Maven project (BOM, plugins, `maven.compiler.release=21`).
- `src/main/resources/application.properties` -- H2 file datasource, Hibernate DDL, embedded users, form auth.
- `src/main/java/com/bmad/finance/Transaction.java` -- Panache entity + scoped finder.
- `src/main/java/com/bmad/finance/TransactionType.java` -- enum `IN`, `OUT`.
- `src/main/java/com/bmad/finance/Category.java` -- fixed category enum.
- `src/main/java/com/bmad/finance/TransactionResource.java` -- list/create routes; `SecurityIdentity` for current user; returns Qute `TemplateInstance`.
- `src/main/resources/templates/transactions.html` -- entry form + owned list (mobile-friendly).
- `src/main/resources/templates/login.html` -- login form posting to `j_security_check`.
- `src/test/java/com/bmad/finance/TransactionResourceTest.java` -- scoping + validation tests via `@TestSecurity`.

## Tasks & Acceptance

**Execution:**
- [x] `pom.xml` -- Generate/define Quarkus app (group `com.bmad`, artifact `finance-tracker`) with the allowed extensions; set compiler release 21.
- [x] `src/main/resources/application.properties` -- H2 file datasource (`jdbc:h2:file:./data/finance`), `quarkus.hibernate-orm.schema-management.strategy=update`, embedded users (alice, bob, carol) with role `user`, form auth (login `/login`, landing `/transactions`); protect all paths except login and static assets.
- [x] `src/main/java/com/bmad/finance/TransactionType.java` -- Enum `IN`, `OUT`.
- [x] `src/main/java/com/bmad/finance/Category.java` -- Fixed enum (e.g. GROCERIES, DINING, TRANSPORT, UTILITIES, RENT, HEALTH, ENTERTAINMENT, SALARY, GIFT, OTHER) with a display label.
- [x] `src/main/java/com/bmad/finance/Transaction.java` -- PanacheEntity: `owner`, `type`, `amount` (BigDecimal), `date` (LocalDate), `category`, `createdAt`; static `listForOwner(owner)` ordered by date desc.
- [x] `src/main/java/com/bmad/finance/TransactionResource.java` -- `GET /transactions` renders form + owned list; `POST /transactions` validates and persists a row owned by the current principal then redirects; `GET /login` renders login page. Reject amount <= 0 with a re-rendered error.
- [x] `src/main/resources/templates/TransactionResource/transactions.html` -- Responsive add form (type, amount, date defaulting to today, category select) + list of owned transactions with EUR formatting; validation error slot.
- [x] `src/main/resources/templates/TransactionResource/login.html` -- Form posting username/password to `/j_security_check`; shows error on the failure page.
- [x] `src/test/java/com/bmad/finance/TransactionResourceTest.java` -- Tests covering the I/O matrix: scoped listing, valid IN/OUT persist, invalid amount rejected, unauthenticated redirect.

**Acceptance Criteria:**
- Given a running instance, when an unauthenticated user requests `/transactions`, then they are redirected to the login page.
- Given valid demo credentials, when a user logs in and submits a valid transaction, then it persists with `owner` = their username and appears in their list after redirect.
- Given two users with transactions, when one views `/transactions`, then only their own rows are shown.
- Given a submission with a non-positive amount, when the form is posted, then no row is persisted and the form re-renders with an error.
- Given the app is stopped and restarted, when a user logs back in, then their previously logged transactions are still present (H2 file persistence).

## Spec Change Log

- **Review (iteration 1) — added extensions:** The approved auth mechanism (embedded users + form auth) and its tests require three extensions beyond the initial allowed list: `quarkus-elytron-security-properties-file` (provides the embedded identity store for `quarkus.security.users.embedded.*`), `quarkus-test-security` (`@TestSecurity`), and `rest-assured` (HTTP tests). These are intrinsic enablers of the already-approved approach, not new capabilities. KEEP: embedded-users + form-auth remains the auth design.
- **Review (iteration 1) — amount precision:** Money is validated to ≤ 2 decimal places and normalized to scale 2 before persist, so values store and display consistently (e.g. `2000` → `2000.00`). KEEP: reject over-precision rather than silently rounding money.

## Design Notes

- Auth: Quarkus **form authentication** (`quarkus.http.auth.form.enabled=true`) with **embedded users** (`quarkus.security.users.embedded.*`, plain-text, role `user`) — matches "hard-coded credentials in config" and provides session cookies with no custom code. Current user via injected `SecurityIdentity.getPrincipal().getName()`.
- One shared `Category` enum for IN/OUT (income + spending) keeps the demo small.
- Qute: `@Inject Template` or `@CheckedTemplate`; format amounts as `€ {amount}`.

## Verification

**Commands:**
- `mvn -q -DskipTests package` -- expected: BUILD SUCCESS, app compiles.
- `mvn test` -- expected: all TransactionResourceTest cases pass.

**Manual checks:**
- `mvn quarkus:dev`, open `http://localhost:8080/transactions`, confirm redirect to login; log in as alice, add an OUT and an IN, confirm they list; log in as bob and confirm alice's rows are not visible.

## Suggested Review Order

**Per-user scoping (the core invariant)**

- Entry point: every write stamps the row with the authenticated principal.
  [`TransactionResource.java:59`](../../src/main/java/com/bmad/finance/TransactionResource.java#L59)

- Current user resolved from the injected SecurityIdentity — no client-supplied owner.
  [`TransactionResource.java:44`](../../src/main/java/com/bmad/finance/TransactionResource.java#L44)

- Reads are scoped by owner at the query, so users only ever see their own history.
  [`Transaction.java:60`](../../src/main/java/com/bmad/finance/Transaction.java#L60)

**Input validation (money correctness)**

- Amount rejected when non-numeric, ≤ 0, or over 2 decimals; normalized to scale 2.
  [`TransactionResource.java:59`](../../src/main/java/com/bmad/finance/TransactionResource.java#L59)

**Demo-grade auth (form login + embedded users)**

- Path protection: everything authenticated except the login pages and callback.
  [`application.properties:38`](../../src/main/resources/application.properties#L38)

- Static, hard-coded users defined in config (not production auth).
  [`application.properties:16`](../../src/main/resources/application.properties#L16)

- Login form posts to the framework's `j_security_check`.
  [`login.html:29`](../../src/main/resources/templates/TransactionResource/login.html#L29)

**Domain model**

- Fixed, predefined categories (not user-managed in v1).
  [`Category.java:7`](../../src/main/java/com/bmad/finance/Category.java#L7)

- Owned entity mapping.
  [`Transaction.java:24`](../../src/main/java/com/bmad/finance/Transaction.java#L24)

**UI & tests (supporting)**

- Mobile-friendly entry form + owned history list.
  [`transactions.html:47`](../../src/main/resources/templates/TransactionResource/transactions.html#L47)

- End-to-end real form login exercises embedded users and the session cookie.
  [`TransactionResourceTest.java:64`](../../src/test/java/com/bmad/finance/TransactionResourceTest.java#L64)

- Scoping asserted over HTTP: another user's row must not appear.
  [`TransactionResourceTest.java:207`](../../src/test/java/com/bmad/finance/TransactionResourceTest.java#L207)
