# FinTrack Dashboard Polish Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the current text-area Swing screen with a polished, typed finance dashboard while preserving the existing Access schema and JFreeChart integration.

**Architecture:** Keep the existing default-package Java structure, but separate presentation rules into a small testable helper, expose typed transaction rows from `DatabaseHelper`, and let `FinTrackGUI` render those rows through a `JTable`. All mutations return success/failure so the UI refreshes only after successful database operations.

**Tech Stack:** Java Swing, JDBC/UCanAccess, JFreeChart, plain Java assertion-based tests (no new test dependency).

**Spec:** `docs/superpowers/specs/2026-09-28-fintrack-dashboard-polish-design.md`

## Global Constraints

- Preserve the existing Access database and its tables.
- Keep `FinTrackGUI` as the main window.
- JFreeChart remains the charting implementation.
- Use RM currency formatting consistently in the dashboard.
- The UI must not parse display strings to recover ids or amounts.
- Database failures must show a concise user-facing error and retain the current visible data.
- Security hardening, password hashing, database replacement, cloud sync, mobile support, and a web rewrite are out of scope.

## Review Focus

- Blank, whitespace-only, non-numeric, zero, negative, NaN, and infinite amounts must be rejected without changing the table.
- A budget of zero or a missing budget must produce an explicit “No budget set” state instead of a divide-by-zero or misleading percentage.
- A selected transaction row must delete only the current user’s matching transaction id; a stale or foreign id must not be deleted.
- An empty transaction result must render a useful empty state and must not open a blank/invalid chart.
- Repeated insights actions must reuse or dispose the chart window instead of accumulating duplicate windows.

### Task 1: Presentation Rules and Typed Transaction Record

**Files:**
- Modify: `TransactionData.java`
- Create: `FinTrackUiSupport.java`
- Create: `test/FinTrackUiSupportTest.java`

**Interfaces:**
- Produces `TransactionData(int id, String category, double amount, java.sql.Timestamp transactionDate)` with accessors `id()`, `category()`, `amount()`, and `transactionDate()`.
- Produces `FinTrackUiSupport.parsePositiveAmount(String) -> Double`, returning `null` for invalid/non-positive/non-finite input.
- Produces `FinTrackUiSupport.formatCurrency(double) -> String`, using `RM` and exactly two decimals.
- Produces `FinTrackUiSupport.budgetStatus(double budget, double spent) -> String` with the states `No budget set`, `On track`, `Approaching limit`, and `Over budget`.
- Produces `FinTrackUiSupport.formatTimestamp(java.sql.Timestamp) -> String` for table display.

- [ ] **Step 1: Write failing assertion tests**

  Add a plain Java test class with a `main` method and assertion helpers covering:

  - `parsePositiveAmount(" 12.50 ") == 12.5`.
  - Blank, `-1`, `0`, `NaN`, `Infinity`, and non-numeric input return `null`.
  - `formatCurrency(1250.5)` returns `RM 1,250.50`.
  - `budgetStatus(0, 0)`, `budgetStatus(1000, 500)`, `budgetStatus(1000, 850)`, and `budgetStatus(1000, 1200)` return the four exact states.
  - A `TransactionData` instance preserves id, category, amount, and timestamp.

- [ ] **Step 2: Run the test to verify it fails**

  Run: `javac -d test-out TransactionData.java FinTrackUiSupport.java test/FinTrackUiSupportTest.java; if ($LASTEXITCODE -eq 0) { java -ea -cp test-out FinTrackUiSupportTest }`

  Expected: FAIL because `FinTrackUiSupport` and the expanded `TransactionData` constructor do not exist yet.

- [ ] **Step 3: Implement the presentation helpers**

  Add the exact methods above. Use `BigDecimal`-style formatting through `String.format(Locale.US, "RM %,.2f", amount)` and compare budget percentages without rounding. Make `budgetStatus` check missing/non-positive budget first, then `< 0.80`, `<= 1.00`, and `> 1.00`.

- [ ] **Step 4: Run the focused test to verify it passes**

  Run the same `javac` and `java -ea` command.

  Expected: all assertions pass and the process exits with code 0.

- [ ] **Step 5: Commit**

  ```powershell
  git add TransactionData.java FinTrackUiSupport.java test/FinTrackUiSupportTest.java
  git commit -m "feat: add dashboard presentation rules"
  ```

### Task 2: Typed Transaction Queries and User-Scoped Deletion

**Files:**
- Modify: `DatabaseHelper.java`
- Modify: `RegularUser.java` (only to accept the boolean result from `insertTransaction` without changing its public behavior)

**Interfaces:**
- Consumes `TransactionData` and `FinTrackUiSupport.formatTimestamp` from Task 1.
- Produces `DatabaseHelper.insertTransaction(String username, String category, double amount) -> boolean`.
- Produces `DatabaseHelper.getTransactions(String username) -> List<TransactionData>`, ordered newest first.
- Produces `DatabaseHelper.deleteTransaction(String username, int transactionId) -> boolean`, deleting only rows matching both the current user id and transaction id.
- Existing `getAllTransactions(String)` remains available for compatibility but may delegate to typed results for its legacy string output.

- [ ] **Step 1: Add a query-contract test seam**

  Extend `test/FinTrackUiSupportTest.java` or create `test/TransactionDataTest.java` with a mapping test that constructs two `TransactionData` rows and verifies their ids and timestamps remain distinct. This pins the data object the database/UI boundary consumes without requiring an Access driver or real user data.

- [ ] **Step 2: Run the focused test and baseline compile**

  Run: `javac -d test-out TransactionData.java FinTrackUiSupport.java test/FinTrackUiSupportTest.java; if ($LASTEXITCODE -eq 0) { java -ea -cp test-out FinTrackUiSupportTest }`

  Expected: PASS before database edits; this is the typed-boundary baseline.

- [ ] **Step 3: Implement typed database operations**

  Change `insertTransaction` to return `true` only after `executeUpdate()` succeeds. Implement `getTransactions` with `SELECT transaction_id, category, amount, transactiondate FROM transactions WHERE ID = ? ORDER BY transactiondate DESC, transaction_id DESC`. Implement scoped deletion with `DELETE FROM transactions WHERE transaction_id = ? AND ID = ?`, returning `updatedRows == 1`. Keep all statements parameterized and keep existing user-id lookup behavior.

- [ ] **Step 4: Compile the production sources against the existing IntelliJ libraries**

  Run the project’s configured IntelliJ build if available. If the external JAR paths in `FinTrack.iml` are unavailable, run a source-only compile for the model/helper classes and record the missing dependency as an environment limitation rather than hiding it.

  Expected: no Java syntax/type errors in the changed database/model code.

- [ ] **Step 5: Commit**

  ```powershell
  git add DatabaseHelper.java RegularUser.java
  git commit -m "feat: expose typed user-scoped transactions"
  ```

### Task 3: Dashboard UI and Interaction Flow

**Files:**
- Replace implementation: `FinTrackGUI.java`
- Create: `TransactionTableModel.java`
- Modify: `FinTrack.java`

**Interfaces:**
- Consumes the Task 1 presentation helpers and Task 2 methods `insertTransaction`, `getTransactions`, and `deleteTransaction`.
- Produces `TransactionTableModel extends AbstractTableModel` with `setTransactions(List<TransactionData>)`, `getTransactionAt(int)`, four columns named `Date`, `Category`, `Amount`, and `Id`, and a `JTable` using that model. Also produces four summary cards, a category combo box, selected-row deletion, and a single reusable insights window.

- [ ] **Step 1: Write the failing table-model test**

  Add `test/TransactionTableModelTest.java` that constructs `TransactionTableModel`, asserts the four exact column names and zero initial rows, loads two `TransactionData` rows, asserts two rows, and asserts `getTransactionAt(0)` returns the same typed id/category/amount as the first loaded row. Keep database calls and `JFrame` construction out of this test.

- [ ] **Step 2: Run the table-model test to establish the failing contract**

  Run: `javac -d test-out TransactionData.java TransactionTableModel.java test/TransactionTableModelTest.java; if ($LASTEXITCODE -eq 0) { java -ea -cp test-out TransactionTableModelTest }`

  Expected: FAIL because `TransactionTableModel` does not exist yet.

- [ ] **Step 3: Implement `TransactionTableModel`**

  Extend `AbstractTableModel`, maintain a private `List<TransactionData>`, return the exact four columns, expose the typed row through `getTransactionAt`, and fire a single data-change event from `setTransactions`. Render the id column as a hidden support column in the view rather than parsing visible strings.

- [ ] **Step 4: Run the table-model test to verify it passes**

  Run the same `javac` and `java -ea` command.

  Expected: all assertions pass and the process exits with code 0.

- [ ] **Step 5: Implement the dashboard shell**

  Build the window with a header, four summary cards, an entry card, a history card, and action buttons. Use shared color/font/inset constants. Set a sensible minimum size and use `BorderLayout`, `GridBagLayout`, and `BoxLayout` only where each makes the layout easier to resize.

- [ ] **Step 6: Implement the typed transaction table**

  Store `List<TransactionData>` in a table model with columns `Date`, `Category`, `Amount`, and a hidden/id-backed row mapping. Render dates through `formatTimestamp`, amounts through `formatCurrency`, and default to newest-first ordering. Use the selected model row to retrieve the original `TransactionData`; never parse the rendered strings.

- [ ] **Step 7: Implement validated actions and refresh flow**

  Validate the amount with `parsePositiveAmount`, call the boolean database methods, and call one `refreshDashboard()` only after success. Update the empty state, summary cards, budget status, and delete-button enabled state together. Confirm deletion and pass both the logged-in username and selected transaction id to `deleteTransaction`.

- [ ] **Step 8: Implement summary, report, advice, and reusable insights behavior**

  Keep report/advice actions, format all visible money as RM, reuse a single chart frame, and dispose it on dashboard close. When category data is empty, show an explanatory message and do not open a chart.

- [ ] **Step 9: Update application startup**

  Launch the login window on the Swing event-dispatch thread in `FinTrack.main` and leave database connectivity errors to the UI flow rather than opening an extra connection at startup.

- [ ] **Step 10: Run the focused tests and compile**

  Run the Task 1 assertion test, `TransactionTableModelTest`, and the full available IntelliJ/source compile.

  Expected: all runnable assertions pass; compilation succeeds or the only reported limitation is a missing external JAR/Access driver.

- [ ] **Step 11: Commit**

  ```powershell
  git add FinTrackGUI.java TransactionTableModel.java FinTrack.java test/
  git commit -m "feat: polish FinTrack dashboard"
  ```

### Task 4: Run Documentation and Repository Handoff

**Files:**
- Create: `README.md`
- Modify: `.gitignore` if verification finds generated files not covered

**Interfaces:**
- Consumes the final source layout and existing IntelliJ module configuration.
- Produces concise setup/run instructions, dependency notes, and a clear statement that the local Access database is not committed.

- [ ] **Step 1: Write README verification checklist**

  Include prerequisites, opening/building in IntelliJ, required UCanAccess/JFreeChart libraries, how to place/configure the local database, and the supported dashboard actions.

- [ ] **Step 2: Verify the documentation against the repository**

  Run: `rg --files -g '!out' -g '!*.accdb'` and `git status --short`.

  Expected: README paths and commands match files present, generated output/database files are ignored, and source/docs are the only intended upload content.

- [ ] **Step 3: Run final verification**

  Run: `git diff --check`, the assertion tests, and the strongest available Java compile command.

  Expected: whitespace check is clean, tests pass, and compile results are recorded.

- [ ] **Step 4: Commit**

  ```powershell
  git add README.md .gitignore
  git commit -m "docs: add FinTrack setup guide"
  ```

- [ ] **Step 5: Prepare GitHub upload**

  Inspect `git remote -v` and current branch. If no remote exists, ask for the GitHub repository URL or use a configured GitHub CLI account only when available; do not invent a repository name or upload the local `.accdb` database.

## Final verification

After all tasks, run the assertion tests, the strongest available compile, `git diff --check`, `git status --short`, and `git log --oneline`. Confirm the working tree contains no ignored build output in the commit and no private database file is staged before pushing.
