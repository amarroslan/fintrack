# FinTrack Dashboard Polish Design

## Goal

Turn the current Swing screen into a coherent personal-finance dashboard while preserving the existing Access database and its tables. The primary user should be able to understand their current spending, add a transaction, set a budget, inspect category breakdowns, and remove a selected transaction without navigating through ambiguous dialogs.

This pass is intentionally product-focused. Security hardening, portable database configuration, and a build/dependency migration are follow-up work unless they are required to make the dashboard safe to operate.

## Current context

- The application is a Java Swing desktop app.
- `LoginGUI` opens `FinTrackGUI` after authentication.
- `DatabaseHelper` owns Access JDBC queries for users, budgets, and transactions.
- `FinTrackGUI` currently uses a text area, free-text category and amount fields, several independent buttons, and modal dialogs for reports and insights.
- `TransactionData` does not currently contain the transaction id or date needed by a table row.
- JFreeChart is already configured in the IntelliJ module file and should remain the charting implementation.

## Design

### 1. Dashboard shell

Keep `FinTrackGUI` as the main window, but replace the current BorderLayout composition with a structured shell:

- A header containing the product name, logged-in username, and a short period label.
- A row of four summary cards for total spent, budget, remaining budget, and budget status.
- A content area split into a transaction-entry card and a transaction-history card.
- A compact action row for refresh, monthly report, spending insights, and spending advice.

The window should use consistent insets, a restrained purple/indigo palette, readable contrast, and a minimum size so the table remains usable when resized. Styling should be centralized in small helper methods/constants rather than repeated inline colors.

### 2. Transaction entry

Replace the free-text category field with a category combo box containing common categories and an `Other` option. Keep an editable path for `Other` so existing user behavior is not lost. The amount field should accept only positive, finite values and display a direct validation message beside the form or in a dialog when submission fails.

On success, clear the entry fields, refresh the table and summary cards, and show a brief success status in the window rather than requiring a blocking success dialog for every transaction.

### 3. Transaction history

Replace the text area with a `JTable` backed by a small typed record/model containing transaction id, date, category, and amount. The table should:

- Sort by date with newest transactions first by default.
- Format amount values consistently as RM currency.
- Select a whole row when clicked.
- Enable delete only when a row is selected.
- Ask for confirmation before deletion.

Deletion must use the selected row's transaction id and the current logged-in user. The data-access layer should scope the delete query to the user id so the polished UI does not introduce cross-user deletion risk.

### 4. Summary and insights

Create one refresh path that reads the current budget, total spent, and transaction data, then updates all dashboard widgets together. Remaining budget should be calculated as budget minus total spent and should be visibly negative when over budget. Status text should distinguish no budget, on track, approaching limit, and over budget.

Keep the existing JFreeChart pie chart for category insights, but source it from the same refreshed category data and use RM-formatted labels in the surrounding summary. Avoid opening duplicate chart windows when the user invokes insights repeatedly; dispose the prior chart window or reuse a single insights window.

### 5. Data boundary

Add a typed transaction result (or extend the existing record) to carry the database row id and timestamp. Add a user-scoped delete operation in `DatabaseHelper`. Preserve existing public helper behavior where practical so the model classes and current database remain compatible.

The UI should not parse display strings to recover ids or amounts. Display formatting belongs in the Swing layer; query results should remain typed.

## Data flow

```text
user action
  -> FinTrackGUI validates input
  -> DatabaseHelper performs parameterized query
  -> typed result objects return to the GUI
  -> one refreshDashboard() updates cards, table, and derived status
```

Database failures should be converted into a concise user-facing error message and logged with the exception cause. A failed add, budget update, or delete must not clear or mutate the visible table as if the operation succeeded.

## Testing and verification

- Add focused tests for amount validation, budget-status derivation, currency formatting, and table-row-to-transaction mapping where the repository's available Java test tooling permits.
- Compile all source files against the existing configured libraries.
- Exercise the application paths that can be verified without a GUI automation framework: login window construction, transaction model mapping, validation, and database helper query compilation.
- Run `git diff --check` and record any environment limitation, especially missing external JARs or Access drivers.

## Out of scope

- Password hashing and authentication redesign.
- Replacing Access with SQLite or another database.
- Cloud sync, mobile support, or a web rewrite.
- New financial-accounting features beyond the current budget, transaction, report, advice, and insight concepts.
