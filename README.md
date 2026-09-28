# FinTrack

FinTrack is a Java Swing personal-finance tracker backed by an Access database. The dashboard provides:

- spending, budget, remaining-balance, and budget-status cards;
- guided transaction and budget entry;
- a sortable transaction history table with selected-row deletion;
- monthly reports, spending advice, and a reusable category chart.

## Requirements

- JDK 17 or newer;
- [JFreeChart](https://www.jfree.org/jfreechart/) 1.5.4;
- UCanAccess 5.0.1 and its bundled dependencies;
- IntelliJ IDEA (the project currently uses `FinTrack.iml`).

## Run locally

1. Open the project in IntelliJ IDEA.
2. Add `jfreechart-1.5.4.jar` and the UCanAccess 5.0.1 JAR/dependency directory to the module classpath.
3. Copy your local `fintrack.accdb` file into the project root. The current database helper uses the existing machine-specific path in `DatabaseHelper.java`, so update `DB_PATH` for your checkout before running.
4. Run `FinTrack.java`.

The local Access database is intentionally ignored by Git because it can contain user data. Create or copy your own database before launching the app.

## Tests and compile

The repository includes dependency-free assertion tests for presentation rules and the transaction table model:

```powershell
javac -d test-out TransactionData.java FinTrackUiSupport.java test/FinTrackUiSupportTest.java
java -ea -cp test-out FinTrackUiSupportTest

javac -d test-out TransactionData.java FinTrackUiSupport.java TransactionTableModel.java test/TransactionTableModelTest.java
java -ea -cp test-out TransactionTableModelTest
```

Compile the full application from IntelliJ with the JFreeChart and UCanAccess dependencies configured.

## Notes

The current application still uses plaintext credentials and a machine-specific Access path. Those are follow-up hardening tasks; do not use this build for sensitive production data without addressing them.
