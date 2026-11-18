package com.studentfinance.ui;

import com.studentfinance.model.*;
import com.studentfinance.service.*;
import com.studentfinance.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public final class MainFrame extends JFrame {
    private final FinanceService service;
    private final CardLayout pages = new CardLayout();
    private final JPanel pageHost = new JPanel(pages);
    private final JLabel title = Theme.label("Dashboard", 28, Theme.GREEN, true);
    private final JLabel status = Theme.label("Loading records...", 12, Theme.MUTED, false);
    private final JLabel income = Theme.label("INR 0.00", 26, Theme.GREEN, true);
    private final JLabel expenses = Theme.label("INR 0.00", 26, Theme.TEXT, true);
    private final JLabel balance = Theme.label("INR 0.00", 26, Theme.GREEN, true);
    private final JLabel monthly = Theme.label("INR 0.00", 26, Theme.TEXT, true);
    private final JLabel monthDetail = Theme.label(" ", 13, Theme.MUTED, false);
    private final JTextField month = new JTextField(YearMonth.now().toString(), 7);
    private YearMonth selectedMonth = YearMonth.now();
    private final TransactionTableModel transactions = new TransactionTableModel();
    private final JTable table = new JTable(transactions);
    private final CategoryChartPanel dashboardChart = new CategoryChartPanel();
    private final CategoryChartPanel reportChart = new CategoryChartPanel();
    private final DefaultTableModel daily = reportModel("Date", "Expense (INR)");
    private final DefaultTableModel category = reportModel("Category", "Expense (INR)");
    private final DefaultTableModel history = reportModel("Month", "Expense (INR)");
    private final JTextField search = new JTextField(18);
    private List<Transaction> snapshot = List.of();
    private String currentPage = "Dashboard";
    private boolean busy;

    public MainFrame(FinanceService service, Path databasePath, boolean demo) {
        super("Student Expense Tracker" + (demo ? " - Demo data" : ""));
        this.service = service;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 900);
        setMinimumSize(new Dimension(1120, 740));
        setLocationRelativeTo(null);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BACKGROUND);
        root.add(sidebar(demo), BorderLayout.WEST);
        JPanel main = new JPanel(new BorderLayout(0, 20));
        main.setBackground(Theme.BACKGROUND);
        main.setBorder(new EmptyBorder(28, 28, 16, 28));
        main.add(header(), BorderLayout.NORTH);
        pageHost.setOpaque(false);
        pageHost.add(dashboard(), "Dashboard");
        pageHost.add(transactionPage(), "Transactions");
        pageHost.add(reports(), "Reports");
        main.add(pageHost, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(status, BorderLayout.WEST);
        JLabel local = Theme.label(demo ? "DEMO DATA  /  Separate temporary database" : "Saved on this device", 11, Theme.MUTED, false);
        local.setToolTipText(databasePath.toString());
        footer.add(local, BorderLayout.EAST);
        main.add(footer, BorderLayout.SOUTH);
        root.add(main, BorderLayout.CENTER);
        setContentPane(root);
        refresh(null);
    }

    private JPanel sidebar(boolean demo) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.GREEN);
        panel.setBorder(new EmptyBorder(34, 22, 28, 22));
        panel.setPreferredSize(new Dimension(214, 0));
        JLabel brand = Theme.label("STUDENT", 23, Color.WHITE, true);
        JLabel sub = Theme.label("Expense Tracker", 16, new Color(202, 229, 211), false);
        panel.add(brand);
        panel.add(Box.createVerticalStrut(5));
        panel.add(sub);
        panel.add(Box.createVerticalStrut(44));
        for (String name : List.of("Dashboard", "Expenses", "Income", "Reports")) {
            JButton button = Theme.button(name, true);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setMaximumSize(new Dimension(175, 48));
            button.setBorder(new EmptyBorder(13, 10, 13, 8));
            button.addActionListener(event -> showPage(name));
            panel.add(button);
            panel.add(Box.createVerticalStrut(10));
        }
        panel.add(Box.createVerticalGlue());
        panel.add(Theme.label(demo ? "DEMONSTRATION" : "YOUR MONEY, ORGANIZED", 10, new Color(202, 229, 211), true));
        panel.add(Box.createVerticalStrut(10));
        panel.add(Theme.label("Java 17  /  SQLite", 11, new Color(202, 229, 211), false));
        return panel;
    }

    private JPanel header() {
        JPanel outer = new JPanel(new BorderLayout(0, 18));
        outer.setOpaque(false);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JPanel names = new JPanel(new GridLayout(2, 1, 0, 5));
        names.setOpaque(false);
        names.add(title);
        names.add(Theme.label("A clearer view of your student finances", 14, Theme.MUTED, false));
        top.add(names, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actions.setOpaque(false);
        JButton addIncome = Theme.button("+ Income", false);
        JButton addExpense = Theme.button("+ Expense", true);
        addIncome.addActionListener(event -> edit(null, TransactionType.INCOME));
        addExpense.addActionListener(event -> edit(null, TransactionType.EXPENSE));
        actions.add(addIncome);
        actions.add(addExpense);
        top.add(actions, BorderLayout.EAST);
        outer.add(top, BorderLayout.NORTH);
        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filter.setOpaque(false);
        JLabel label = Theme.label("Report month", 13, Theme.TEXT, true);
        label.setLabelFor(month);
        filter.add(label);
        month.setToolTipText("Enter a month as YYYY-MM, then Apply");
        filter.add(month);
        JButton apply = Theme.button("Apply", false);
        apply.addActionListener(event -> applyMonth());
        month.addActionListener(event -> applyMonth());
        filter.add(apply);
        JButton reload = Theme.button("Refresh", false);
        reload.addActionListener(event -> { if (!busy) refresh(null); });
        filter.add(reload);
        outer.add(filter, BorderLayout.SOUTH);
        return outer;
    }

    private void applyMonth() {
        try {
            if (!month.getText().strip().matches("\\d{4}-\\d{2}")) throw new IllegalArgumentException();
            selectedMonth = YearMonth.parse(month.getText().strip());
            render();
        } catch (Exception invalid) { error("Enter a valid month as YYYY-MM, for example 2026-11."); }
    }

    private JPanel dashboard() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);
        JPanel cards = new JPanel(new GridLayout(1, 4, 14, 0));
        cards.setOpaque(false);
        cards.add(card("TOTAL INCOME", income, "All recorded income"));
        cards.add(card("TOTAL EXPENSES", expenses, "All recorded expenses"));
        cards.add(card("REMAINING BALANCE", balance, "Income minus expenses"));
        cards.add(card("MONTHLY SPENDING", monthly, "Selected report month"));
        panel.add(cards, BorderLayout.NORTH);
        JPanel chart = section("Spending by category", dashboardChart);
        chart.add(monthDetail, BorderLayout.SOUTH);
        panel.add(chart, BorderLayout.CENTER);
        return panel;
    }

    private JPanel card(String caption, JLabel value, String hint) {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.LINE), new EmptyBorder(18, 17, 18, 10)));
        card.add(Theme.label(caption, 10, Theme.MUTED, true), BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        card.add(Theme.label(hint, 11, Theme.MUTED, false), BorderLayout.SOUTH);
        return card;
    }

    private JPanel transactionPage() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        Theme.configureTable(table);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(4).setPreferredWidth(260);
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        JPanel find = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        find.setOpaque(false);
        JLabel label = Theme.label("Search", 13, Theme.TEXT, true);
        label.setLabelFor(search);
        find.add(label);
        find.add(search);
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent event) { filterTransactions(); }
            public void removeUpdate(javax.swing.event.DocumentEvent event) { filterTransactions(); }
            public void changedUpdate(javax.swing.event.DocumentEvent event) { filterTransactions(); }
        });
        toolbar.add(find, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton edit = Theme.button("Edit selected", false);
        edit.addActionListener(event -> { Transaction transaction = selected(); if (transaction != null) edit(transaction, transaction.getType()); });
        JButton delete = Theme.button("Delete selected", false);
        delete.addActionListener(event -> deleteSelected());
        JButton export = Theme.button("Export all to CSV", true);
        export.addActionListener(event -> export());
        actions.add(edit); actions.add(delete); actions.add(export);
        toolbar.add(actions, BorderLayout.EAST);
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(Theme.label("History shows all dates. Use Reports for month and day totals. Click a column to sort.", 12, Theme.MUTED, false), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel reports() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setOpaque(false);
        reportChart.setPreferredSize(new Dimension(500, 220));
        panel.add(section("Category totals for the selected month", reportChart), BorderLayout.NORTH);
        JPanel tables = new JPanel(new GridLayout(1, 3, 14, 0));
        tables.setOpaque(false);
        tables.add(section("Daily expenses", scrollTable(daily)));
        tables.add(section("By category", scrollTable(category)));
        tables.add(section("Monthly history (all dates)", scrollTable(history)));
        panel.add(tables, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane scrollTable(DefaultTableModel model) {
        JTable report = new JTable(model);
        Theme.configureTable(report);
        return new JScrollPane(report);
    }

    private static DefaultTableModel reportModel(String first, String second) {
        return new DefaultTableModel(new Object[]{first, second}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private JPanel section(String title, JComponent content) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));
        panel.add(Theme.label(title, 16, Theme.GREEN, true), BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    public void showPage(String name) {
        currentPage = name;
        title.setText(name);
        pages.show(pageHost, name.equals("Expenses") || name.equals("Income") ? "Transactions" : name);
        filterTransactions();
    }

    private void filterTransactions() {
        String query = search.getText().strip().toLowerCase(java.util.Locale.ROOT);
        transactions.setTransactions(snapshot.stream().filter(transaction ->
                (!currentPage.equals("Expenses") || transaction.getType() == TransactionType.EXPENSE)
                && (!currentPage.equals("Income") || transaction.getType() == TransactionType.INCOME)
                && (transaction.getCategory() + " " + transaction.getDescription() + " " + transaction.getDate())
                    .toLowerCase(java.util.Locale.ROOT).contains(query)).toList());
    }

    private Transaction selected() {
        if (busy) return null;
        int row = table.getSelectedRow();
        if (row < 0) { error("Select a transaction in the table first."); return null; }
        return transactions.getTransaction(table.convertRowIndexToModel(row));
    }

    private void edit(Transaction existing, TransactionType initial) {
        if (busy) return;
        final TransactionDialog[] dialog = new TransactionDialog[1];
        dialog[0] = new TransactionDialog(this, initial, existing, transaction ->
                background(() -> { service.save(transaction); return service.all(); }, records -> {
                    snapshot = records; render(); dialog[0].dispose(); status.setText("Transaction saved.");
                }, failure -> dialog[0].showError(failure.getMessage())));
        dialog[0].setVisible(true);
    }

    private void deleteSelected() {
        Transaction transaction = selected();
        if (transaction == null) return;
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Delete " + transaction.getType().toString().toLowerCase() + " of " + Money.format(transaction.getAmountPaise())
                        + " dated " + transaction.getDate() + "? This cannot be undone.",
                "Delete transaction", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmation == JOptionPane.YES_OPTION) background(() -> {
            service.delete(transaction.getId()); return service.all();
        }, records -> { snapshot = records; render(); status.setText("Transaction deleted."); }, failure -> error(failure.getMessage()));
    }

    private void export() {
        if (busy) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export all income and expenses");
        chooser.setSelectedFile(new java.io.File("student-transactions.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path destination = chooser.getSelectedFile().toPath();
        if (!destination.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(".csv"))
            destination = destination.resolveSibling(destination.getFileName() + ".csv");
        if (java.nio.file.Files.exists(destination) && JOptionPane.showConfirmDialog(this,
                "Replace the existing file?", "Export CSV", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        Path target = destination;
        List<Transaction> data = snapshot;
        background(() -> { CsvExporter.export(target, data); return target; }, saved -> {
            status.setText("Exported " + data.size() + " transactions.");
            JOptionPane.showMessageDialog(this, "CSV saved to:\n" + saved.toAbsolutePath());
        }, failure -> error("Could not export CSV: " + failure.getMessage()));
    }

    public void refresh(Runnable after) {
        background(service::all, records -> { snapshot = records; render(); if (after != null) after.run(); },
                failure -> error("Could not read the database: " + failure.getMessage()));
    }

    private void render() {
        FinanceSummary summary = service.summarize(snapshot, selectedMonth);
        income.setText(Money.format(summary.totalIncome()));
        expenses.setText(Money.format(summary.totalExpenses()));
        balance.setText(Money.format(summary.balance()));
        balance.setForeground(summary.balance() < 0 ? new Color(164, 58, 46) : Theme.GREEN);
        monthly.setText(Money.format(summary.monthlyExpenses()));
        monthDetail.setText(selectedMonth + "   Income: " + Money.format(summary.monthlyIncome())
                + "   Net change: " + Money.format(summary.monthlyIncome() - summary.monthlyExpenses()));
        dashboardChart.setValues(summary.categoryExpenses());
        reportChart.setValues(summary.categoryExpenses());
        fill(daily, summary.dailyExpenses());
        fill(category, summary.categoryExpenses());
        fill(history, summary.monthlyHistory());
        filterTransactions();
        status.setText(snapshot.isEmpty() ? "No transactions yet. Start with + Income or + Expense." : snapshot.size() + " transactions saved locally.");
    }

    private void fill(DefaultTableModel model, Map<?, Long> values) {
        model.setRowCount(0);
        values.forEach((key, value) -> model.addRow(new Object[]{key.toString(), Money.decimal(value)}));
    }

    private <T> void background(Callable<T> task, Consumer<T> success, Consumer<Exception> failure) {
        if (busy) return;
        busy = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        status.setText("Working...");
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return task.call(); }
            @Override protected void done() {
                busy = false;
                setCursor(Cursor.getDefaultCursor());
                try { success.accept(get()); }
                catch (Exception error) {
                    Throwable cause = error.getCause() == null ? error : error.getCause();
                    status.setText("Operation failed. Your existing records remain available.");
                    failure.accept(cause instanceof Exception exception ? exception : error);
                }
            }
        }.execute();
    }

    private void error(String message) {
        JOptionPane.showMessageDialog(this, message, "Student Expense Tracker", JOptionPane.ERROR_MESSAGE);
    }

    public boolean isLoading() { return busy; }
}
