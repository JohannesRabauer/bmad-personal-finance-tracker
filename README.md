# Personal Finance Tracker

> A demo project built **live** using the [BMAD Method](https://github.com/bmad-code-org/BMAD-METHOD) — an agentic, spec-driven development workflow — together with **Brian Madison** and **Johannes Rabauer**.

[![Watch the live build on YouTube](https://img.youtube.com/vi/Gki9fAlefyw/maxresdefault.jpg)](https://youtube.com/live/Gki9fAlefyw)

▶️ **Watch the full session:** [Building this app live with the BMAD Method](https://youtube.com/live/Gki9fAlefyw)

---

## About this project

This repository is a small **Personal Finance Tracker** created as a demonstration of the [BMAD Method](https://github.com/bmad-code-org/BMAD-METHOD) — writing a machine-readable **spec** first, then letting agents scaffold, implement, review, and verify the code against that spec.

It is intentionally **demo-grade**: the goal is to show the method and produce a clean, runnable vertical slice — not to be a production-hardened finance app.

## What it does

A user logs in and tracks their own income and expenses:

- 🔐 **Login** — demo-grade form login with a few hard-coded users (no sign-up)
- ➕ **Log a transaction** — amount (in EUR €), income/expense, date, and a category
- 📋 **See your history** — a list of your own transactions
- 👤 **Per-user isolation** — every user sees only their own data; queries are scoped to the logged-in owner
- 🏷️ **Fixed categories** — a predefined category list (Groceries, Rent, Salary, …)

Data is stored in a local **H2 file database**, so it survives restarts with zero external setup.

## Tech stack

| Concern         | Choice                                             |
| --------------- | -------------------------------------------------- |
| Framework       | [Quarkus](https://quarkus.io/)                     |
| Templating / UI | [Qute](https://quarkus.io/guides/qute) (server-rendered) |
| Persistence     | Hibernate ORM with Panache + **H2** (file-based)   |
| Auth            | Quarkus form authentication + embedded users       |
| Language        | Java 21                                            |
| Tests           | JUnit 5 + REST Assured (11 tests)                  |

## Running it

Dev mode with live reload:

```shell
./mvnw quarkus:dev
```

Then open <http://localhost:8080/transactions> — you'll be redirected to the login page.

**Demo users** (username / password): `alice` / `alice-pw`, `bob` / `bob-pw`, `carol` / `carol-pw`.

> **Note:** Requires JDK 21+.

Run the tests:

```shell
./mvnw test
```

Package and run as a jar:

```shell
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

## How it was built (the BMAD Method)

Rather than jumping straight to code, the work flowed through the BMAD spec-driven workflow:

1. **Spec** — the desired behavior was distilled into a machine-readable spec.
2. **Plan** — the spec was scoped down to a single foundational vertical slice.
3. **Implement** — the app was scaffolded and hand-written to satisfy the spec.
4. **Review** — adversarial, edge-case, and verification-gap review passes hardened it.
5. **Verify** — the build and 11 automated tests confirm the behavior.

The governing spec and deferred work live under [`_bmad-output/`](./_bmad-output/implementation-artifacts/).

### Not built yet (deferred)

Kept out of this first slice on purpose — see [`deferred-work.md`](./_bmad-output/implementation-artifacts/deferred-work.md):

- Filtering / searching transactions
- Dashboard and spending charts
- Production-grade auth (CSRF, real password hashing, sign-up)

---

*Built with the [BMAD Method](https://github.com/bmad-code-org/BMAD-METHOD) · [Watch the live session](https://youtube.com/live/Gki9fAlefyw)*
