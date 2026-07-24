---
title: "Product Brief: Personal Finance Tracker"
status: draft
created: 2026-07-24
updated: 2026-07-24
---

# Product Brief: Personal Finance Tracker

## Executive Summary

A modern, self-hosted personal finance tracker for a single household. Family members log money coming **in** and going **out**, tag each entry with a purpose/category, and see where their money goes through simple charts — all from a phone or laptop browser.

The application is deliberately small and honest in scope, because its **primary purpose is to serve as a demonstration project for the BMad method**. The finance tracker is the vehicle: a domain everyone understands, with just enough real-world complexity (data modeling, multi-user, UI, persistence, reporting) to exercise the full BMad workflow — brainstorming → brief → PRD → architecture → epics/stories → implementation — end to end, without drowning in incidental complexity.

Built on **Quarkus** (Java) with **Qute** server-side templating and a mobile-friendly UI, it is meant to be readable, buildable, and followable as a worked example, while still being genuinely useful for the family that runs it.

## The Problem

Two problems, one project:

1. **The demo problem (primary).** Teaching or showcasing the BMad method needs a reference project that is realistic but not overwhelming — something a viewer can grasp in minutes, with a clear thread from idea to shipped feature. Toy examples feel contrived; large apps bury the method under domain noise.
2. **The real problem (secondary, keeps it honest).** A family wants a shared, private place to record income and spending and understand their habits, without handing financial data to a third-party cloud service or wrestling with a heavyweight tool built for accountants.

The status quo for the family today is [ASSUMPTION] spreadsheets or nothing — no shared view, no easy categorization, no at-a-glance sense of where the money goes.

## The Solution

A server-side web app where each family member:

- Records transactions as **ins** (income) and **outs** (spending), each with an amount, a date, and a **purpose/category**.
- Browses and filters their transaction history.
- Sees spending and income broken down **by category** and **over time** through simple charts.

It is **self-hosted** — the family runs one instance on their own hardware, keeping the data private. The experience is optimized for quick entry on a phone, since most spending gets logged on the go.

Because it is a BMad demo, the solution also optimizes for **clarity of construction**: a clean domain model, conventional Quarkus/Qute patterns, and a scope small enough that every architectural and story decision is traceable back to this brief.

## What Makes This Different

- **Honesty of scope.** It does exactly what a household needs and nothing more. No fabricated "AI insights," no bank-integration ambitions in v1.
- **Self-hosted and private.** Data stays on the family's own server — a real differentiator versus cloud finance apps for the privacy-conscious.
- **Exemplary, not just functional.** As a BMad reference, the value is partly in *how* it is built — a project others can read and learn the method from. That is an execution/pedagogy advantage, not a technical moat, and we state it plainly.

## Who This Serves

- **Primary (as a product): the family.** A small, trusted group [ASSUMPTION] (~2–5 people) who each log their own transactions and want a shared, private, easy-to-use record. Success = "logging a purchase takes seconds and I can see where our money went this month."
- **Primary (as a demo): BMad learners and the author.** People evaluating or learning the BMad method who want a complete, followable example. Success = "I can trace one feature from brief to running code and understand the method."

## Success Criteria

**As a demo (primary):**
- The full BMad workflow is exercised and the artifacts (brief, PRD, architecture, epics/stories, implemented code) form a coherent, followable trail.
- A newcomer can read the project and understand both the finance app *and* the method.

**As a product (secondary):**
- A family member can log an in/out with a category in a few taps on their phone.
- The dashboard shows spending/income by category and over time at a glance.
- The whole family shares one instance without stepping on each other's data.

## Scope

**In (v1):**
- Manual entry of **ins** and **outs** (amount, date, purpose/category).
- **Categories** for classifying transactions.
- Transaction list with basic filtering.
- **Charts**: spending/income by category and over time.
- **Multi-user** with **static, hard-coded usernames and passwords** (demo-grade auth) so each family member has their own view.
- Mobile-friendly, server-rendered UI (Quarkus + Qute).
- Persistence to [ASSUMPTION] a relational database (e.g. Postgres via Hibernate/Panache) with a simple schema.

**Out (v1):**
- Real user registration, password reset, or production-grade authentication/authorization.
- Bank connections, CSV/API import, automatic transaction sync.
- Budgets, limits, alerts, or goal tracking.
- Multi-currency handling — [ASSUMPTION] single currency for v1.
- Native mobile apps (mobile-friendly web only).
- Shared/joint accounts or complex household roles beyond per-user logins.

## Vision

If it succeeds as a **demo**, it becomes the canonical BMad reference project — the thing people point to when they ask "show me what building with BMad actually looks like," and a template that can be forked to teach the method.

If it grows as a **product**, natural next steps are budgets and limits, recurring transactions (salary, rent, subscriptions), CSV import, real authentication, and richer reporting — each a clean candidate for a future BMad iteration, demonstrating how the method handles change over time.
