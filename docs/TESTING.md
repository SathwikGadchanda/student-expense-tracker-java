# Testing and measured results

Verified on Windows with OpenJDK 17 and Maven 3.9.9 on 18 November 2026.

## Automated integration suite

`mvn verify` completed successfully. JUnit reported **20 tests, 0 failures, 0 errors, 0 skipped**. Tests use a fresh temporary SQLite file per test, not the personal application database.

| Area | Evidence in FinanceIntegrationTest | Result |
| --- | --- | --- |
| Connectivity and initialization | Creates nested data directory, connects, SELECT 1, repeat initialization | Passed |
| Create | Inserts income and expense with unique generated IDs | Passed |
| Read and ordering | Maps saved values, sorts by date and ID, returns immutable list | Passed |
| Update | Changes type, amount, category, date and description | Passed |
| Delete | Deletes one selected ID while preserving another | Passed |
| Missing records | Update/delete missing IDs report an error | Passed |
| Calculations | Exact paise income, expense and balance totals | Passed |
| Monthly summaries | Month-end, year boundary and leap-day grouping | Passed |
| Daily/category summaries | Aggregates multiple records in the same group | Passed |
| Empty data and overspending | Zero totals and negative balances | Passed |
| Validation | Invalid amount formats, category mismatch and long description | Passed |
| Database constraints | Rejects invalid direct SQL writes | Passed |
| SQL input handling | Stores quotes and SQL-looking text as description data | Passed |
| CSV | UTF-8, comma/quote/newline escaping, formulas, replacement and failed path | Passed |
| Persistence | New database and service instances read saved records | Passed |

There are 20 JUnit methods. Several related methods are grouped in the table above. The test reports in `target/surefire-reports` give the exact method names, results and timings.

## Separate-process restart test

PersistenceProbe ran in two separate JVMs against the same temporary file. The first process inserted a known income record and exited with `WRITE_OK`. The second process reopened that file, verified the amount and row count, and exited with `RESTART_READ_OK`. Both exit codes were 0. This verifies survival across process exit, beyond simply replacing DAO objects in one JVM.

To reproduce after building, choose a new nonexistent database path:

```powershell
java -cp "target/test-classes;target/student-expense-tracker.jar" com.studentfinance.PersistenceProbe write "C:\MyData\restart-probe.db"
java -cp "target/test-classes;target/student-expense-tracker.jar" com.studentfinance.PersistenceProbe read "C:\MyData\restart-probe.db"
```

The read probe expects exactly one record. Use a fresh path if running the write probe again.

## Packaged JAR check

The Maven Shade plugin created an executable JAR with the App main class and JDBC dependencies. Running the packaged JAR with `--check-db` against a separate test path printed `DATABASE_OK records=0` and returned exit code 0. This verifies the manifest entry point and native SQLite driver load from the packaged file.

## UI inspection

UiCapture launched the actual MainFrame from the packaged application's classes, with a separate synthetic demo database. It rendered Dashboard, Expenses, Income and Reports to PNGs. The four images were visually inspected. The review led to resizing the report chart so tables remain usable at the available window height. These are actual Swing renders, not design mockups or generated illustrations.

This is a visual smoke check. It does not constitute a complete automated click-through test of every dialog, button or operating-system file chooser. The CRUD and CSV outcomes above were verified through service/DAO integration tests.

## Manual demonstration checklist

Before presenting, check these steps on the presentation computer:

1. Launch normal mode against an empty named test database.
2. Add income 5000 and expense 120.50. Confirm balance 4879.50.
3. Enter an invalid date and amount. Confirm a validation message.
4. Edit the expense to 150. Confirm balance 4850.
5. Search and sort the history. Select a row after sorting and confirm Edit opens the correct record.
6. Delete the expense after confirmation. Confirm the income remains.
7. Add records in two months and apply each report month.
8. Export CSV, open it in a spreadsheet, and check headers and amounts.
9. Close and reopen the same named database. Confirm records persist.
10. Test on the projector display and use the report tables' scrollbars as needed.

These manual steps are provided for rehearsal and are not reported as completed tests.
