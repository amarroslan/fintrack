import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Timestamp;
import java.util.List;

public class FinTrackGUI {
    private static final Color BACKGROUND = new Color(247, 248, 252);
    private static final Color CARD = Color.WHITE;
    private static final Color INK = new Color(31, 35, 46);
    private static final Color MUTED = new Color(104, 111, 128);
    private static final Color PRIMARY = new Color(91, 67, 161);
    private static final Color PRIMARY_DARK = new Color(67, 46, 126);
    private static final Color BORDER = new Color(226, 228, 236);
    private static final Color SUCCESS = new Color(42, 122, 84);
    private static final Color WARNING = new Color(184, 116, 24);
    private static final Color DANGER = new Color(171, 61, 71);

    private final String loggedInUser;
    private JFrame frame;
    private JComboBox<String> categoryComboBox;
    private JTextField amountField;
    private JTextField budgetField;
    private JLabel totalSpentValue;
    private JLabel budgetValue;
    private JLabel remainingValue;
    private JLabel budgetStatusValue;
    private JLabel tableStatusLabel;
    private JLabel formStatusLabel;
    private JButton deleteButton;
    private JTable transactionTable;
    private TransactionTableModel transactionTableModel;
    private JFrame insightsFrame;

    public FinTrackGUI(String username) {
        this.loggedInUser = username;
        initializeGUI();
    }

    private void initializeGUI() {
        frame = new JFrame("FinTrack Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(980, 650));
        frame.setSize(1120, 760);
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBorder(new EmptyBorder(24, 28, 24, 28));
        root.setBackground(BACKGROUND);

        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createDashboardContent(), BorderLayout.CENTER);
        root.add(createActionBar(), BorderLayout.SOUTH);

        frame.setContentPane(root);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (insightsFrame != null) {
                    insightsFrame.dispose();
                }
            }
        });
        frame.setVisible(true);
        refreshDashboard();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("FinTrack");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(INK);

        JLabel subtitle = new JLabel("A clearer view of your everyday spending");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(MUTED);

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new javax.swing.BoxLayout(brand, javax.swing.BoxLayout.Y_AXIS));
        brand.add(title);
        brand.add(subtitle);

        JLabel userLabel = new JLabel("Signed in as " + loggedInUser, SwingConstants.RIGHT);
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        userLabel.setForeground(PRIMARY_DARK);
        header.add(brand, BorderLayout.WEST);
        header.add(userLabel, BorderLayout.EAST);
        return header;
    }

    private JPanel createDashboardContent() {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);
        content.add(createSummaryCards(), BorderLayout.NORTH);

        JPanel lower = new JPanel(new BorderLayout(16, 0));
        lower.setOpaque(false);
        lower.add(createTransactionForm(), BorderLayout.WEST);
        lower.add(createTransactionHistory(), BorderLayout.CENTER);
        content.add(lower, BorderLayout.CENTER);
        return content;
    }

    private JPanel createSummaryCards() {
        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        cards.setOpaque(false);
        totalSpentValue = new JLabel(FinTrackUiSupport.formatCurrency(0));
        budgetValue = new JLabel("Not set");
        remainingValue = new JLabel("—");
        budgetStatusValue = new JLabel("No budget set");

        cards.add(createSummaryCard("Spent this period", totalSpentValue, PRIMARY));
        cards.add(createSummaryCard("Monthly budget", budgetValue, new Color(55, 124, 142)));
        cards.add(createSummaryCard("Remaining", remainingValue, SUCCESS));
        cards.add(createSummaryCard("Budget status", budgetStatusValue, WARNING));
        return cards;
    }

    private JPanel createSummaryCard(String label, JLabel value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                new EmptyBorder(14, 16, 14, 16)));

        JLabel title = new JLabel(label.toUpperCase());
        title.setFont(new Font("SansSerif", Font.BOLD, 11));
        title.setForeground(MUTED);
        value.setFont(new Font("SansSerif", Font.BOLD, 20));
        value.setForeground(INK);
        card.add(title, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private JPanel createTransactionForm() {
        JPanel card = createCard();
        card.setPreferredSize(new Dimension(285, 0));
        card.setLayout(new BorderLayout(0, 14));

        JLabel title = new JLabel("Add transaction");
        title.setFont(new Font("SansSerif", Font.BOLD, 19));
        title.setForeground(INK);
        card.add(title, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 8, 0);

        fields.add(createFieldLabel("Category"), constraints);
        constraints.gridy++;
        categoryComboBox = new JComboBox<>(new String[]{"Food", "Transport", "Bills", "Shopping", "Health", "Entertainment", "Other"});
        categoryComboBox.setEditable(true);
        categoryComboBox.setSelectedItem("");
        styleInput(categoryComboBox);
        fields.add(categoryComboBox, constraints);

        constraints.gridy++;
        constraints.insets = new Insets(8, 0, 8, 0);
        fields.add(createFieldLabel("Amount"), constraints);
        constraints.gridy++;
        constraints.insets = new Insets(0, 0, 8, 0);
        amountField = new JTextField();
        styleInput(amountField);
        fields.add(amountField, constraints);

        constraints.gridy++;
        constraints.insets = new Insets(8, 0, 8, 0);
        fields.add(createFieldLabel("Monthly budget"), constraints);
        constraints.gridy++;
        constraints.insets = new Insets(0, 0, 8, 0);
        budgetField = new JTextField();
        styleInput(budgetField);
        fields.add(budgetField, constraints);

        JButton addButton = createPrimaryButton("Add transaction");
        addButton.addActionListener(e -> addTransaction());
        JButton budgetButton = createSecondaryButton("Save budget");
        budgetButton.addActionListener(e -> setBudget());
        JPanel formButtons = new JPanel(new GridLayout(1, 2, 8, 0));
        formButtons.setOpaque(false);
        formButtons.add(addButton);
        formButtons.add(budgetButton);
        constraints.gridy++;
        constraints.insets = new Insets(12, 0, 0, 0);
        fields.add(formButtons, constraints);
        card.add(fields, BorderLayout.CENTER);

        formStatusLabel = new JLabel(" ");
        formStatusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        formStatusLabel.setForeground(MUTED);
        card.add(formStatusLabel, BorderLayout.SOUTH);
        return card;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(MUTED);
        return label;
    }

    private JPanel createTransactionHistory() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(0, 12));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JLabel title = new JLabel("Transaction history");
        title.setFont(new Font("SansSerif", Font.BOLD, 19));
        title.setForeground(INK);
        heading.add(title, BorderLayout.WEST);

        deleteButton = createSecondaryButton("Delete selected");
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(e -> deleteSelectedTransaction());
        heading.add(deleteButton, BorderLayout.EAST);
        card.add(heading, BorderLayout.NORTH);

        transactionTableModel = new TransactionTableModel();
        transactionTable = new JTable(transactionTableModel);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionTable.setRowHeight(32);
        transactionTable.setShowGrid(false);
        transactionTable.setIntercellSpacing(new Dimension(0, 0));
        transactionTable.setFillsViewportHeight(true);
        transactionTable.setAutoCreateRowSorter(true);
        transactionTable.getTableHeader().setReorderingAllowed(false);
        transactionTable.getSelectionModel().addListSelectionListener(e ->
                deleteButton.setEnabled(transactionTable.getSelectedRow() >= 0));

        DefaultTableCellRenderer dateRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                setText(FinTrackUiSupport.formatTimestamp((Timestamp) value));
            }
        };
        DefaultTableCellRenderer amountRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                setText(value == null ? "" : FinTrackUiSupport.formatCurrency((Double) value));
            }
        };
        amountRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        transactionTable.getColumnModel().getColumn(0).setCellRenderer(dateRenderer);
        transactionTable.getColumnModel().getColumn(2).setCellRenderer(amountRenderer);
        TableColumn idColumn = transactionTable.getColumnModel().getColumn(3);
        idColumn.setMinWidth(0);
        idColumn.setMaxWidth(0);
        idColumn.setPreferredWidth(0);

        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        card.add(scrollPane, BorderLayout.CENTER);

        tableStatusLabel = new JLabel(" ");
        tableStatusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tableStatusLabel.setForeground(MUTED);
        card.add(tableStatusLabel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createActionBar() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);

        JButton refreshButton = createSecondaryButton("Refresh");
        refreshButton.addActionListener(e -> refreshDashboard());
        JButton reportButton = createSecondaryButton("Monthly report");
        reportButton.addActionListener(e -> showMonthlyReport());
        JButton insightsButton = createSecondaryButton("Spending insights");
        insightsButton.addActionListener(e -> showSpendingInsights());
        JButton adviceButton = createSecondaryButton("Spending advice");
        adviceButton.addActionListener(e -> showSpendingAdvice());

        actions.add(refreshButton);
        actions.add(reportButton);
        actions.add(insightsButton);
        actions.add(adviceButton);
        return actions;
    }

    private JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(18, 18, 18, 18)));
        return card;
    }

    private void addTransaction() {
        String category = String.valueOf(categoryComboBox.getEditor().getItem()).trim();
        Double amount = FinTrackUiSupport.parsePositiveAmount(amountField.getText());
        if (category.isEmpty()) {
            showFormError("Choose or enter a category.");
            return;
        }
        if (amount == null) {
            showFormError("Enter a positive amount, such as 25.50.");
            return;
        }

        if (DatabaseHelper.insertTransaction(loggedInUser, category, amount)) {
            amountField.setText("");
            categoryComboBox.setSelectedItem("");
            setFormStatus("Transaction added.", SUCCESS);
            refreshDashboard();
        } else {
            showError("The transaction could not be saved. Your current data was kept.");
        }
    }

    private void setBudget() {
        Double budget = FinTrackUiSupport.parsePositiveAmount(budgetField.getText());
        if (budget == null) {
            showFormError("Enter a positive monthly budget.");
            return;
        }
        if (DatabaseHelper.setBudget(loggedInUser, budget)) {
            budgetField.setText("");
            setFormStatus("Budget updated.", SUCCESS);
            refreshDashboard();
        } else {
            showError("The budget could not be saved. Your current data was kept.");
        }
    }

    private void deleteSelectedTransaction() {
        int selectedViewRow = transactionTable.getSelectedRow();
        if (selectedViewRow < 0) {
            return;
        }

        int selectedModelRow = transactionTable.convertRowIndexToModel(selectedViewRow);
        TransactionData transaction = transactionTableModel.getTransactionAt(selectedModelRow);
        int confirmation = JOptionPane.showConfirmDialog(frame,
                "Delete the " + transaction.category() + " transaction for "
                        + FinTrackUiSupport.formatCurrency(transaction.amount()) + "?",
                "Delete transaction", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        if (DatabaseHelper.deleteTransaction(loggedInUser, transaction.id())) {
            setFormStatus("Transaction deleted.", SUCCESS);
            refreshDashboard();
        } else {
            showError("That transaction could not be deleted. Your current data was kept.");
        }
    }

    private void refreshDashboard() {
        try {
            List<TransactionData> transactions = DatabaseHelper.getTransactions(loggedInUser);
            transactionTableModel.setTransactions(transactions);
            tableStatusLabel.setText(transactions.isEmpty()
                    ? "No transactions yet — add your first one above."
                    : transactions.size() + " transaction" + (transactions.size() == 1 ? "" : "s"));
            deleteButton.setEnabled(false);

            double budget = DatabaseHelper.getBudget(loggedInUser);
            double totalSpent = DatabaseHelper.getTotalSpent(loggedInUser);
            updateSummary(budget, totalSpent);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            showError("FinTrack could not refresh your data. Please try again.");
        }
    }

    private void updateSummary(double budget, double totalSpent) {
        totalSpentValue.setText(FinTrackUiSupport.formatCurrency(totalSpent));
        String status = FinTrackUiSupport.budgetStatus(budget, totalSpent);
        budgetValue.setText(budget > 0 ? FinTrackUiSupport.formatCurrency(budget) : "Not set");
        remainingValue.setText(budget > 0 ? FinTrackUiSupport.formatCurrency(budget - totalSpent) : "—");
        budgetStatusValue.setText(status);
        budgetStatusValue.setForeground(status.equals("Over budget") ? DANGER
                : status.equals("Approaching limit") ? WARNING : SUCCESS);
    }

    private void showMonthlyReport() {
        String report = DatabaseHelper.generateMonthlyReport(loggedInUser);
        double totalSpent = DatabaseHelper.getTotalSpent(loggedInUser);
        String message = report + "\nTotal spent: " + FinTrackUiSupport.formatCurrency(totalSpent);
        JOptionPane.showMessageDialog(frame, message, "Monthly report", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSpendingAdvice() {
        JOptionPane.showMessageDialog(frame,
                DatabaseHelper.generateSpendingAdvice(loggedInUser),
                "Spending advice", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSpendingInsights() {
        List<DatabaseHelper.CategoryData> categoryData = DatabaseHelper.getCategorySpending(loggedInUser);
        if (categoryData.isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                    "Add a transaction first to see your spending breakdown.",
                    "Spending insights", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        for (DatabaseHelper.CategoryData data : categoryData) {
            dataset.setValue(data.getCategory(), data.getTotalAmount());
        }
        JFreeChart chart = ChartFactory.createPieChart(
                "Spending by category", dataset, true, true, false);

        if (insightsFrame == null || !insightsFrame.isDisplayable()) {
            insightsFrame = new JFrame("Spending insights");
            insightsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }
        insightsFrame.getContentPane().removeAll();
        insightsFrame.setLayout(new BorderLayout(0, 10));
        insightsFrame.add(new ChartPanel(chart), BorderLayout.CENTER);
        JLabel summary = new JLabel("Total spent: "
                + FinTrackUiSupport.formatCurrency(DatabaseHelper.getTotalSpent(loggedInUser)),
                SwingConstants.CENTER);
        summary.setBorder(new EmptyBorder(0, 0, 10, 0));
        summary.setFont(new Font("SansSerif", Font.BOLD, 13));
        insightsFrame.add(summary, BorderLayout.SOUTH);
        insightsFrame.setSize(560, 540);
        insightsFrame.setLocationRelativeTo(frame);
        insightsFrame.setVisible(true);
    }

    private void styleInput(javax.swing.JComponent component) {
        component.setFont(new Font("SansSerif", Font.PLAIN, 14));
        component.setPreferredSize(new Dimension(0, 34));
    }

    private JButton createPrimaryButton(String text) {
        JButton button = createButton(text);
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = createButton(text);
        button.setBackground(Color.WHITE);
        button.setForeground(PRIMARY_DARK);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 183, 218)),
                new EmptyBorder(8, 13, 8, 13)));
        return button;
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(9, 14, 9, 14));
        return button;
    }

    private void setFormStatus(String message, Color color) {
        formStatusLabel.setText(message);
        formStatusLabel.setForeground(color);
    }

    private void showFormError(String message) {
        setFormStatus(message, DANGER);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "FinTrack", JOptionPane.ERROR_MESSAGE);
    }
}
