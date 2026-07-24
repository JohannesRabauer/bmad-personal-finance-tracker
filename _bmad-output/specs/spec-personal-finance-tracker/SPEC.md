---
id: SPEC-personal-finance-tracker
companions:
  - stack.md
sources:
  - ../../planning-artifacts/briefs/brief-bmad-personal-finance-tracker-2026-07-24/brief.md
---

> **Canonical contract.** This SPEC and the files in `companions:` are the complete, preservation-validated contract for what to build, test, and validate. Source documents listed in frontmatter are for traceability — consult them only if you need narrative rationale or prose color this contract intentionally omits.

# Personal Finance Tracker

## Why

This work exists to **realize a vision** and to **solve a real pain**, in that order. The primary force is a canonical **demonstration project for the BMad method**: a domain everyone understands, sized so a viewer can trace one feature from idea to shipped code without drowning in incidental complexity. The secondary, honesty-keeping force is a genuine one — a family wants a shared, private place to record income and spending and see where their money goes, without handing financial data to a cloud service or wrestling with accountant-grade tools. Every downstream trade-off resolves against keeping the build small, readable, and traceable while still genuinely useful to the family that runs it.

## Capabilities

- **CAP-1**
  - **intent:** A user logs a transaction as an "in" (income) or "out" (spending), recording amount, date, and a purpose/category.
  - **success:** The entry persists and appears in that user's history with the correct type, amount, date, and category.
- **CAP-2**
  - **intent:** A user classifies transactions with categories that capture their purpose.
  - **success:** A category assigned at entry is used to group that transaction consistently in lists and charts.
- **CAP-3**
  - **intent:** A user reviews transaction history through a list with basic filtering.
  - **success:** The list shows the user's transactions and filtering (e.g. by type, category, or date) narrows it to the correct subset.
- **CAP-4**
  - **intent:** A user sees income and spending broken down by category and over time.
  - **success:** A dashboard renders a by-category breakdown and an over-time trend that match the underlying transactions.
- **CAP-5**
  - **intent:** Each family member accesses the app through a static login and has their own scoped view.
  - **success:** After logging in with static credentials, a user sees the correct scoped data and cannot act as another user.

## Constraints

- Self-hosted as a single instance on the family's own hardware; data stays private, with no third-party cloud dependency.
- UI is server-rendered and mobile-friendly, optimized for quick entry on a phone (bends toward server-side rendering over a SPA).
- Authentication is demo-grade only: static, hard-coded usernames and passwords — rules out registration, password reset, and production authN/authZ.
- Tech stack is fixed to Quarkus + Qute + a relational database (see `stack.md`); implementation stays idiomatic because the code doubles as a BMad worked example.
- Scope stays small and traceable: every feature must trace back to the brief — no gold-plating.

## Non-goals

- Real or production-grade authentication (registration, password reset, role-based access).
- Bank connections, CSV/API import, or automatic transaction sync.
- Budgets, limits, alerts, or goal tracking.
- Multi-currency handling.
- Native mobile applications (mobile-friendly web only).
- Shared/joint accounts or complex household roles beyond per-user logins.

## Success signal

A family member logs a purchase on their phone in a few taps, and the dashboard immediately shows the month's spending broken down by category. The whole path — from this brief through to that running feature — is traceable as a coherent BMad worked example a newcomer can follow.

## Assumptions

- The family's status quo today is spreadsheets or nothing.
- The family is a small group of roughly 2–5 users.
- Persistence uses a relational database, likely Postgres via Hibernate/Panache (H2/SQLite is a simpler self-hosting candidate).
- A single currency is used for v1.

## Open Questions

- **Data visibility:** Does each user see only their own transactions, or a shared household view? The brief implies both ("their own view" vs. "where *our* money went") — this is load-bearing for CAP-4 and CAP-5 and needs a human decision.
- **Database choice:** Postgres, or H2/SQLite for zero-dependency self-hosting?
- **Currency:** Which single currency and display format, and is it configurable per instance?
- **Users & credentials:** How many static users, and where is the credential set defined (config file vs. compiled-in)?
- **Categories:** Fixed predefined list, or user-managed (create/edit/delete)?
