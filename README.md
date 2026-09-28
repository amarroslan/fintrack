# FinTrack

FinTrack is a personal-finance tracker. The repository contains two clients:

- **`web/`** — the primary Next.js + PostgreSQL web application (deployable to Vercel), with secure password hashing, cookie sessions, and a responsive dashboard.
- **`legacy-java/`** — the original Java Swing desktop client backed by an Access database, kept as a working reference.

## Web app

The web app provides login/registration, spending/budget/remaining/status summary cards, guided transaction and budget entry, a transaction history table with deletion, monthly reports, and category insights. Amounts are stored as numeric values and formatted as RM at the presentation boundary.

### Requirements

- Node.js 20 or newer;
- A PostgreSQL database (Neon works well with Vercel).

### Run locally

1. `cd web && npm install`
2. Copy `.env.example` to `.env.local` and set `DATABASE_URL` (and `AUTH_SECRET`).
3. `npm run db:migrate` to create the schema.
4. `npm run dev` and open http://localhost:3000.

### Scripts

`npm run dev`, `npm run build`, `npm run lint`, `npm run test`, `npm run db:generate`, `npm run db:migrate`.

### Deploy to Vercel

1. Import the repository into Vercel with **Root Directory = `web`**.
2. Install a PostgreSQL provider (e.g. Neon) via the Vercel Marketplace; Vercel injects `DATABASE_URL`.
3. Set `AUTH_SECRET` in Vercel environment variables.
4. Run the migration command (`npm run db:migrate`) against the production database once.

### Import legacy Access data (optional)

Export your Access data to a JSON file matching the contract in `docs/superpowers/specs/2026-09-28-fintrack-web-platform-design.md`, then run `npm run import-access -- path/to/export.json`. The export file and the Access database are never committed.

## Legacy Java desktop app

The desktop client lives in `legacy-java/` and provides:

- spending, budget, remaining-balance, and budget-status cards;
- guided transaction and budget entry;
- a sortable transaction history table with selected-row deletion;
- monthly reports, spending advice, and a reusable category chart.

### Requirements

- JDK 17 or newer;
- [JFreeChart](https://www.jfree.org/jfreechart/) 1.5.4;
- UCanAccess 5.0.1 and its bundled dependencies;
- IntelliJ IDEA (the project uses `legacy-java/FinTrack.iml`).

### Run locally

1. Open the project in IntelliJ IDEA (the module root is `legacy-java/`).
2. Add `jfreechart-1.5.4.jar` and the UCanAccess 5.0.1 JAR/dependency directory to the module classpath.
3. Copy your local `fintrack.accdb` file into `legacy-java/`. Update `DB_PATH` in `legacy-java/DatabaseHelper.java` for your checkout before running.
4. Run `legacy-java/FinTrack.java`.

The local Access database is intentionally ignored by Git because it can contain user data. Create or copy your own database before launching the app.

### Tests and compile

Dependency-free assertion tests for the desktop presentation rules and table model:

```powershell
cd legacy-java
javac -d test-out TransactionData.java FinTrackUiSupport.java test/FinTrackUiSupportTest.java
java -ea -cp test-out FinTrackUiSupportTest

javac -d test-out TransactionData.java FinTrackUiSupport.java TransactionTableModel.java test/TransactionTableModelTest.java
java -ea -cp test-out TransactionTableModelTest
```

Compile the full application from IntelliJ with the JFreeChart and UCanAccess dependencies configured.

### Notes

The legacy client still uses plaintext credentials and a machine-specific Access path. Use the web app for anything sensitive; keep the desktop build as a reference only.
