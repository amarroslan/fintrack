import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

abstract class User {
    protected String name;
    protected double budget;
    protected List<Transaction> transactions = new ArrayList<>();

    public User(String name, double budget) {
        this.name = name;
        this.budget = budget;
    }

    public abstract void addTransaction(Transaction transaction);

    public void showTransactions() {
        List<String> transactionStrings = DatabaseHelper.getAllTransactions(name);
        transactions.clear();

        for (String t : transactionStrings) {
            String[] parts = t.split(" \\| ");
            if (parts.length >= 4) {
                String category = parts[1].trim();
                double amount = Double.parseDouble(parts[2].replace("$", "").trim());
                transactions.add(new Transaction(category, amount));
            }
        }

        //lambda expression
        transactions.sort((t1, t2) -> Double.compare(t1.getAmount(), t2.getAmount()));

        for (Transaction transaction : transactions) {
            System.out.println(transaction);
        }
    }

    public double getTotalExpenses() {
        double total = 0;
        for (Transaction t : transactions) {
            total += t.getAmount();
        }
        return total;
    }

    interface TransactionHandler {
        void addTransaction(Transaction transaction);
    }

}


