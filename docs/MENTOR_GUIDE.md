# Demonstration and viva guide

## A 2-3 minute demonstration script

Use a test database, or launch with `--demo`. Demo mode starts with synthetic data in a fresh temporary database on each launch. To demonstrate persistence, use the normal mode or a named test database, because a new demo launch intentionally starts fresh.

| Time | Screen/action | Suggested narration |
| --- | --- | --- |
| 0:00-0:20 | Dashboard | Good morning. My project is Student Expense Tracker, a Java desktop application for college students. It records income and expenses and helps users understand their monthly spending. |
| 0:20-0:40 | Dashboard totals | The dashboard shows total income, total expenses, remaining balance and expenses for the selected month. The balance is income minus expenses. Amounts are stored in paise to avoid decimal calculation errors. |
| 0:40-1:05 | + Income, then + Expense | I will add pocket money of INR 5000 and a Food expense of INR 120.50, using today's date. The form checks the amount, category and date before saving. The dashboard updates after each successful save. |
| 1:05-1:30 | Expenses | Here is the expense history. I can search records, sort a column, edit the selected record and delete it after confirmation. Income has a similar history screen. |
| 1:30-1:55 | Reports | I select a month using YYYY-MM. The reports show daily expenses, category totals and a category bar chart. Monthly history groups expenses by both month and year. |
| 1:55-2:15 | CSV export | I can export all transactions to a CSV file for spreadsheet analysis. The program handles commas and quotes in descriptions. It also protects formula-like descriptions when opened in a spreadsheet. |
| 2:15-2:40 | Close and reopen normal/test DB | SQLite stores the records locally. I close and reopen the program to show that the same data remains. The source has separate model, database, DAO, service, UI and utility packages. |
| 2:40-2:55 | Dashboard | The project demonstrates OOP, exception handling, collections, JDBC CRUD and file handling. The build passes 20 integration tests. Future improvements could include budgets and recurring transactions. Thank you. |

For a shorter presentation, skip one history action and shorten the architecture explanation. Practise with the actual recording duration. Do not claim the temporary demo database survives a new demo launch.

## Important Java classes

| Class/interface | Simple explanation |
| --- | --- |
| App | Main method. Chooses the database, initializes it, and starts Swing on the event thread. |
| Transaction | Immutable object containing ID, type, amount in paise, category, date and description. It validates its own rules. |
| TransactionType | Enum that permits only INCOME and EXPENSE. Its toString method provides readable UI text. |
| Category | Stores fixed lists of valid categories for each transaction type. |
| Database | Selects the database path, creates the table and index, and opens JDBC connections. |
| TransactionDao | Interface defining findAll, insert, update and delete. The service depends on this abstraction. |
| SqliteTransactionDao | Implements the DAO with prepared SQL statements and try-with-resources. |
| FinanceService | Coordinates CRUD and calculates all-time, monthly, daily and category totals. |
| FinanceSummary | Java record used to return related summary values together. |
| MainFrame | Main Swing window, sidebar, cards, history pages, reports and background operations. |
| TransactionDialog | Form for adding and editing a record, with validation feedback. |
| TransactionTableModel | Extends AbstractTableModel. Swing requests table values through overridden methods. |
| CategoryChartPanel | Extends JPanel and overrides paintComponent to draw a simple category chart. |
| Theme | Defines shared colours, fonts, table formatting and buttons. |
| Money | Parses decimal input with BigDecimal, converts to paise and formats amounts. |
| CsvExporter | Writes safe, quoted UTF-8 CSV through a temporary file. |
| DemoData | Creates synthetic records only in explicitly selected demo mode. |

## JDBC and SQLite

JDBC is Java's standard API for communicating with a database. This project uses `java.sql.Connection`, `PreparedStatement`, `Statement` and `ResultSet`. The Xerial SQLite JDBC driver translates JDBC calls into operations on a local SQLite file.

SQLite is an embedded relational database. It runs inside the application process and does not need a separate server. The connection URL starts with `jdbc:sqlite:` followed by the file path. No database username or password is required here.

For an insert, the DAO opens a connection, prepares an INSERT statement, binds type, amount, category, date and description, then calls executeUpdate. It reads `last_insert_rowid()` on the same connection to get the new ID. Try-with-resources closes the connection, statement and result set automatically. A SELECT returns a ResultSet, which the DAO maps into Transaction objects. The UI receives a list rather than SQL rows.

Each write is one automatically committed SQL statement. The current project does not need a multi-statement bank transfer transaction. If such a feature is added, related writes should use an explicit database transaction with commit and rollback.

## OOP concepts used

| Concept | Actual project example | Explanation |
| --- | --- | --- |
| Classes and objects | A new Transaction for each record | A class describes the fields and behaviour. Each object represents one entry. |
| Encapsulation | Transaction's private final fields and getters | Other classes cannot directly change its state. Validation happens at construction. |
| Abstraction | TransactionDao interface | The service knows which operations are available without knowing the SQL details. |
| Inheritance | MainFrame extends JFrame; chart extends JPanel | Custom UI classes reuse standard Swing behaviour. |
| Polymorphism | SqliteTransactionDao used through TransactionDao | The implementation supplies the interface's behaviour at runtime. Swing also calls overridden getValueAt and paintComponent methods. |
| Composition | FinanceService holds a TransactionDao | Small collaborating objects keep responsibilities separate. |

Inheritance is used where Swing requires it. TransactionType distinguishes income from expense because those records have the same fields; artificial Income/Expense subclasses would add complexity without useful different behaviour.

## Twenty possible viva questions

1. **What problem does your project solve?**
   It helps students record income and expenses and understand daily and monthly spending.

2. **Why did you choose Java Swing?**
   Swing is part of Java's desktop libraries. It provides windows, forms, buttons and tables without a web stack.

3. **What is SQLite?**
   SQLite is an embedded relational database that stores this project's records in one local file.

4. **What is JDBC?**
   JDBC is Java's standard database API. A JDBC driver allows Java code to connect to a particular database.

5. **What are CRUD operations in your application?**
   Create adds a transaction, Read lists it, Update edits it, and Delete removes it.

6. **Why use prepared statements?**
   They bind values separately from SQL syntax, so descriptions with quotes or SQL-looking text remain data.

7. **Why store money as paise instead of double?**
   Integer paise represent two decimal places exactly. Binary floating point can introduce rounding errors.

8. **How do you calculate the balance?**
   I sum all recorded income and subtract all recorded expenses. A negative result means expenses exceed income.

9. **How do monthly reports work?**
   The service compares each record's YearMonth with the selected report month, then groups matching expenses.

10. **How does data remain after restart?**
    Records are committed to the SQLite file in the user's home directory. The next normal launch opens that same file.

11. **Where is encapsulation used?**
    Transaction uses private final fields and public getters, and checks its data when an object is created.

12. **Where is inheritance used?**
    MainFrame extends JFrame, TransactionDialog extends JDialog, and CategoryChartPanel extends JPanel.

13. **Where is polymorphism used?**
    FinanceService calls the DAO through its interface. Swing also calls the overridden methods of the table model and chart panel.

14. **What is the purpose of the DAO layer?**
    It keeps database queries separate from business calculations and UI code.

15. **How do you handle exceptions?**
    Database and file methods report failures through exceptions. The UI catches them and shows a useful message. Try-with-resources closes JDBC resources.

16. **Which collections do you use?**
    List stores transaction objects. Maps group expense totals by category, date and month. TreeMap keeps report keys sorted.

17. **Why use SwingWorker?**
    JDBC and CSV operations can take time. SwingWorker performs them away from the event thread and updates the UI when they finish.

18. **How does CSV export work?**
    CsvExporter writes a UTF-8 text file with column headers and records. It quotes special characters and neutralizes formula-leading text.

19. **Does your project have authentication?**
    No. It is a single-user local application. It uses no passwords or tokens. OS file permissions protect access, and the database is not encrypted.

20. **What tests and future improvements can you explain?**
    Twenty integration tests cover CRUD, calculations, month boundaries, validation and CSV export. Separate JVMs verified persistence. Future work could add budgets, encrypted backups and recurring entries.
