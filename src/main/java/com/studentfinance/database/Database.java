package com.studentfinance.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** One local SQLite file. No database server, username, password or token. */
public final class Database {
    private final Path path;

    public Database(Path path) {
        this.path = path.toAbsolutePath().normalize();
    }

    public static Path defaultPath() {
        String override = System.getProperty("expense.tracker.db");
        return override == null ? Path.of(System.getProperty("user.home"),
                ".student-expense-tracker", "expenses.db") : Path.of(override);
    }

    public Path getPath() { return path; }

    public Connection connect() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + path);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA busy_timeout = 5000");
            statement.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException error) {
            connection.close();
            throw error;
        }
        return connection;
    }

    public void initialize() throws SQLException, IOException {
        Files.createDirectories(path.getParent());
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS transactions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        type TEXT NOT NULL CHECK(type IN ('INCOME', 'EXPENSE')),
                        amount_paise INTEGER NOT NULL CHECK(typeof(amount_paise) = 'integer'
                            AND amount_paise BETWEEN 1 AND 100000000),
                        category TEXT NOT NULL CHECK(
                            (type = 'EXPENSE' AND category IN
                                ('Food','Transportation','Education','Shopping','Entertainment','Other'))
                            OR (type = 'INCOME' AND category IN
                                ('Pocket Money','Scholarship','Part-time Work','Gift','Other'))),
                        transaction_date TEXT NOT NULL,
                        description TEXT NOT NULL DEFAULT '' CHECK(length(description) <= 250)
                    )
                    """);
            statement.execute("CREATE INDEX IF NOT EXISTS idx_transactions_date ON transactions(transaction_date)");
        }
    }
}
