# FinTrack Web Platform Design

## Goal

Convert FinTrack from a Java Swing/Access-only desktop application into a deployable web application while preserving the existing desktop client as a legacy reference. The web application must provide secure authentication, PostgreSQL persistence, responsive dashboard workflows, and a Vercel deployment path.

## Decisions

- The web client lives in `web/` and uses Next.js with TypeScript and the App Router.
- PostgreSQL is the system of record for the web app. Neon is the initial provider, provisioned through Vercel Marketplace.
- Drizzle ORM owns the schema and migrations.
- Passwords are stored as salted PBKDF2-HMAC-SHA256 hashes. The plaintext password is never stored or returned.
- Sessions use random opaque tokens stored as hashes in PostgreSQL and sent to the browser through an HTTP-only, secure, same-site cookie.
- The current Java Swing application remains under `legacy-java/` so the migration does not destroy the working desktop reference.
- The Access database is an import source only. It is never committed to GitHub and is never accessed by Vercel runtime code.

## Repository layout

```text
legacy-java/        existing Swing application and desktop-only dependencies
web/
  app/              pages and route handlers
  components/       reusable dashboard/auth UI
  db/               Drizzle schema, client, and migrations
  lib/              auth, validation, formatting, and domain services
  scripts/          Access-export import tooling
  tests/            unit and integration tests
  package.json
  vercel.json
```

The Vercel project root is `web/`. The root README documents both the desktop legacy client and the web deployment.

## PostgreSQL schema

### `users`

- `id` UUID primary key
- `username` text, unique, normalized to lowercase
- `password_hash` text
- `password_salt` text
- `created_at` timestamptz

### `sessions`

- `id` UUID primary key
- `user_id` UUID foreign key to `users`
- `token_hash` text, unique
- `expires_at` timestamptz
- `created_at` timestamptz

### `budgets`

- `user_id` UUID primary/foreign key to `users`
- `monthly_amount` numeric(12,2), positive
- `updated_at` timestamptz

### `transactions`

- `id` UUID primary key
- `user_id` UUID foreign key to `users`
- `category` text, non-empty
- `amount` numeric(12,2), positive
- `occurred_at` timestamptz
- `created_at` timestamptz

Indexes cover `transactions(user_id, occurred_at desc)` and `sessions(token_hash)`.

## Application behavior

### Authentication

- Registration trims and normalizes usernames, rejects blank/invalid input, and rejects duplicates.
- Login verifies the PBKDF2 hash with constant-time comparison.
- Successful login creates a 30-day session and sets an HTTP-only cookie.
- Logout deletes the current session and clears the cookie.
- Protected server actions and route handlers resolve the current user from the session before reading or mutating data.
- Every transaction, budget, and report query is scoped by the authenticated user id.

### Dashboard

The responsive web dashboard carries forward the desktop polish work:

- summary cards for total spent, budget, remaining amount, and status;
- guided transaction form with common categories and positive finite amount validation;
- accessible transaction table sorted newest first;
- selected-row deletion with confirmation;
- monthly report and category insights;
- empty, loading, and error states;
- mobile layout that stacks cards and controls without horizontal scrolling.

All amounts are stored as numeric database values and formatted as RM at the presentation boundary. No client code parses display strings to recover ids or amounts.

## Access migration

The import contract is a JSON export generated from the local Access file and intentionally kept outside Git:

```json
{
  "users": [{"username": "alex", "password": "legacy-password"}],
  "budgets": [{"username": "alex", "monthlyAmount": "1200.00"}],
  "transactions": [{"username": "alex", "category": "Food", "amount": "12.50", "occurredAt": "2026-09-28T12:30:00Z"}]
}
```

The migration script validates the shape, creates users with new PBKDF2 hashes, upserts budgets, inserts transactions, reports rejected rows, and is idempotent for users and transactions with stable source ids when available. The raw Access file and export JSON are ignored by Git.

## Vercel deployment

- Connect the GitHub repository to Vercel with `web/` as the project root.
- Install a Neon PostgreSQL integration through Vercel Marketplace.
- Configure `DATABASE_URL`, `AUTH_SECRET`, and `NEXT_PUBLIC_APP_URL` through Vercel environment variables; secrets never appear in source code or client bundles.
- Run Drizzle migrations as an explicit deployment/setup step, not during every request.
- Use Vercel Preview deployments for pull requests and Production for the main deployment.
- Provide `npm run dev`, `npm run test`, `npm run lint`, `npm run build`, and migration commands in `web/package.json`.

## Error handling and observability

- Validation errors return structured 400 responses and inline form messages.
- Unauthenticated requests return 401 and redirect browser navigation to `/login`.
- Missing records return 404 without leaking another user's data.
- Database failures return a generic user-facing error and log an operation id server-side; raw SQL errors and credentials never reach the browser.
- Mutations are transaction-safe and the UI refreshes only after a successful response.

## Testing and verification

- Unit tests cover password hashing/verification, session token hashing, amount/category validation, budget status, and migration parsing.
- Database integration tests run against a disposable PostgreSQL-compatible test database or a documented local Postgres setup.
- Route tests verify authentication boundaries and user-scoped transaction/budget access.
- Playwright smoke tests cover registration, login, adding a transaction, setting a budget, deleting a transaction, logout, and responsive dashboard rendering.
- Vercel build verification runs `npm run lint`, `npm run test`, and `npm run build` from `web/`.

## Out of scope

- Real-time multi-user collaboration.
- Bank account synchronization or payment processing.
- Password reset email delivery until an email provider is configured.
- Deleting the legacy Java client from the repository.
