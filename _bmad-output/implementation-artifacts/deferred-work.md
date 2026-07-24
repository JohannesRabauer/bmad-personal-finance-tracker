- source_spec: none
  summary: Transaction history list with filtering by type, category, and date (CAP-3 full filtering).
  evidence: Split from the whole-app SPEC to build a foundational vertical slice first; basic list is in the slice but rich filtering is deferred.
- source_spec: none
  summary: Dashboard with by-category breakdown and over-time spending trend (CAP-4).
  evidence: Split from the whole-app SPEC; the dashboard is an independently shippable deliverable layered on top of the foundational slice.
- source_spec: `_bmad-output/implementation-artifacts/spec-foundation-scaffold-login-transaction.md`
  summary: Add CSRF protection to state-changing POST endpoints (transaction create) and harden logout to POST.
  evidence: Adversarial review flagged no CSRF token on authenticated POST and a public GET /logout. Out of scope for this slice (spec non-goal is production-grade authN/authZ); revisit if the app moves beyond demo-grade auth.
- source_spec: `_bmad-output/implementation-artifacts/spec-foundation-scaffold-login-transaction.md`
  summary: Add an automated integration test that verifies H2 file-backed persistence survives an application restart.
  evidence: Verification-gap review noted tests use in-memory H2; file persistence is only covered by the spec's manual check. An automated cross-restart test would harden the guarantee.
