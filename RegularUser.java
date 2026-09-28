import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

class RegularUser extends User implements TransactionHandler {
    public RegularUser(String name, double budget) {
        super(name, budget);
    }

    @Override
    public void addTransaction(Transaction transaction) {
        if (transaction == null) {
            System.out.println("Error: Null transaction cannot be added.");
            return;
        }

        transactions.add(transaction);
        DatabaseHelper.insertTransaction(name, transaction.getCategory(), transaction.getAmount());

        if (getTotalExpenses() > budget) {
            JOptionPane.showMessageDialog(null, "Warning: Budget exceeded!", "Alert", JOptionPane.WARNING_MESSAGE);
        }
    }

    // method overload
    public void addTransaction(String category, double amount) {
        addTransaction(new Transaction(category, amount));
    }

    public double getRemainingBudget() {
        return budget - getTotalExpenses();
    }

    public String getBudgetAdvice() {
        double dailyLimit = budget / 30;
        return "Your suggested daily spending limit: $" + String.format("%.2f", dailyLimit);
    }

}



