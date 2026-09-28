# FinTrack Web Platform Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a deployable Next.js + PostgreSQL web version of FinTrack to the repo while preserving the Java Swing client as a legacy reference.

**Architecture:** Monorepo: `legacy-java/` keeps the desktop app untouched; `web/` is a standalone Next.js (App Router, TypeScript) project with a Drizzle-managed Postgres schema, cookie-session auth, and server actions for all mutations. Vercel project root is `web/`.

**Tech Stack:** Next.js 15, TypeScript, Drizzle ORM + node-postgres, Neon Postgres (via Vercel Marketplace), node:crypto PBKDF2, Vitest.

**Spec:** `docs/superpowers/specs/2026-09-28-fintrack-web-platform-design.md`

## Global Constraints

- Never store or return plaintext passwords; PBKDF2-HMAC-SHA256, per-user salt, constant-time compare.
- Sessions: opaque random token, only its SHA-256 hash stored, HTTP-only + secure + same-site=lax cookie, 30-day expiry.
- Every query scoped by the authenticated user id; 401 when no session.
- Amounts stored numeric(12,2); formatted as RM only at the presentation boundary.
- The Access file and export JSON never enter Git.
- Secrets only via environment variables (`DATABASE_URL`, `AUTH_SECRET`); `.env*` git-ignored.

## Review Focus

- A missing/expired session must never expose dashboard data; unauthenticated navigation redirects to `/login`.
- Amount validation rejects blank, negative, zero, NaN, Infinity, and non-numeric input without a database call.
- Budget status handles no-budget without divide-by-zero and shows a negative remaining value when over budget.
- Deleting a transaction must use the row's id scoped to the session user; a foreign id fails without error leakage.
- Login failure and registration duplicate must return generic messages; SQL errors never reach the browser.

---

### Task 1: Repository restructure

**Files:** Move all `*.java`, `test/`, `FinTrack.iml` into `legacy-java/`; update `.gitignore`, `README.md`.

- [ ] Move desktop sources with `git mv`; keep docs/ as-is.
- [ ] `.gitignore` gains `web/node_modules/`, `web/.next/`, `web/.env*`, `*.accdb` stays.
- [ ] README documents both clients.
- [ ] Commit: `chore: split legacy Java client into legacy-java/`

### Task 2: Web scaffold + schema

**Files:** Create `web/` Next.js app, `web/db/schema.ts` (users, sessions, budgets, transactions per spec), `web/db/index.ts`, `drizzle.config.ts`, `.env.example`, `package.json` scripts (`dev`, `build`, `lint`, `test`, `db:generate`, `db:migrate`).

- [ ] Scaffold manually (avoid `create-next-app` network interactivity): package.json, tsconfig, next config, app/layout.tsx, app/page.tsx redirect to `/dashboard` or `/login`.
- [ ] Drizzle schema matches spec §PostgreSQL schema exactly (UUID PKs, numeric(12,2), indexes on `transactions(user_id, occurred_at desc)` and unique `sessions.token_hash`).
- [ ] `npx drizzle-kit generate` produces SQL migrations.
- [ ] Commit: `feat(web): scaffold Next.js app with Drizzle schema`

### Task 3: Core libraries + unit tests (Vitest)

**Files:** `web/lib/password.ts`, `web/lib/session-token.ts`, `web/lib/validation.ts`, `web/lib/format.ts`, `web/lib/budget.ts`, tests in `web/tests/unit/`.

**Interfaces:**
- `hashPassword(password) -> {hash, salt}`; `verifyPassword(password, hash, salt) -> boolean` (timing-safe).
- `hashToken(token) -> string` (sha256 hex); `generateSessionToken() -> string` (32 random bytes hex).
- `parseAmount(input) -> number | null` (positive finite only); `parseCategory(input) -> string | null` (trimmed, 1..60 chars).
- `formatRM(amount) -> string` ("RM 1,250.50").
- `budgetStatus(budget, spent) -> "No budget set" | "On track" | "Approaching limit" | "Over budget"` (thresholds <0.8, <=1.0, >1.0).

- [ ] Write failing tests for the exact interfaces above (including invalid inputs and thresholds).
- [ ] Implement until green; run `npm run test`.
- [ ] Commit: `feat(web): add auth/format/domain libraries with unit tests`

### Task 4: Authentication

**Files:** `web/lib/auth.ts` (session create/read/destroy on cookies), `web/app/(auth)/login/page.tsx`, `web/app/(auth)/register/page.tsx`, `web/app/actions/auth.ts`, `web/components/auth-form.tsx`.

- [ ] Server actions: `registerAction`, `loginAction` (validate, hash, create session, set cookie, redirect `/dashboard`), `logoutAction`.
- [ ] Generic error messages only; duplicate username reports conflict without leaking existence details beyond "already exists".
- [ ] Commit: `feat(web): add session auth with login and register`

### Task 5: Dashboard

**Files:** `web/app/dashboard/page.tsx` (+ `loading.tsx`, `error.tsx`), `web/lib/queries.ts` (typed user-scoped reads), `web/app/actions/transactions.ts` (add/delete), `web/app/actions/budget.ts` (upsert), `web/components/` (summary cards, transaction form, table with delete, report + insights).

- [ ] All reads via one `getDashboardData(userId)`; totals derived server-side.
- [ ] Delete confirms client-side, calls scoped action, refreshes via `revalidatePath`.
- [ ] Monthly report: last-30-day category totals; insights: category breakdown list.
- [ ] Empty/error/loading states per spec; mobile-stacked layout.
- [ ] Commit: `feat(web): build dashboard with forms, table, report, insights`

### Task 6: Access import tooling

**Files:** `web/scripts/import-access-export.ts`, `web/tests/unit/import-parse.test.ts`.

- [ ] `parseExport(json) -> {valid, invalid[]}` per spec's JSON contract; validates usernames, positive amounts, ISO dates.
- [ ] Import script: creates PBKDF2 users (idempotent upsert), upserts budgets, inserts transactions, prints a rejection report; document usage in README.
- [ ] Commit: `feat(web): add Access export import tooling`

### Task 7: Vercel + docs + verification

**Files:** `web/vercel.json`, `web/.env.example`, README deploy section, `web/tests/` green, `npm run lint && npm run test && npm run build` clean.

- [ ] Build passes with no type errors; lint clean.
- [ ] Document Vercel root = `web/`, env vars `DATABASE_URL`, `AUTH_SECRET`, Neon Marketplace setup, migration command.
- [ ] Commit: `chore(web): vercel config and deployment docs`, push branch.
