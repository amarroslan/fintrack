import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    private static final String DB_PATH = "C:\\Users\\AMAR\\FinTrack\\fintrack.accdb"; // Update path
    private static final String URL = "jdbc:ucanaccess://" + DB_PATH;

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // register a new user
    public static boolean registerUser(String username, String password) {
        String checkUserSql = "SELECT * FROM users WHERE username = ?";
        String insertUserSql = "INSERT INTO users (username, password) VALUES (?, ?)";

        try (Connection conn = connect(); PreparedStatement checkStmt = conn.prepareStatement(checkUserSql)) {
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                return false; // Username already exists
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertUserSql)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                insertStmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // get user ID by username
    public static int getUserId(String username) {
        String sql = "SELECT ID FROM users WHERE username = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("ID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // validate user login
    public static boolean validateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // Returns true if credentials are correct
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // insert transaction
    public static boolean insertTransaction(String username, String category, double amount) {
        Integer userId = getUserId(username);
        if (userId == -1) {
            System.out.println("User not found for: " + username);
            return false;
        }

        String sql = "INSERT INTO transactions (ID, category, amount, transactiondate) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);  // Store user_id
            pstmt.setString(2, category);
            pstmt.setDouble(3, amount);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    // delete transaction
    public static boolean deleteTransaction(String username, int transactionId) {
        int userId = getUserId(username);
        if (userId == -1) {
            return false;
        }

        String sql = "DELETE FROM transactions WHERE transaction_id = ? AND ID = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, transactionId);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    // set & get user budget
    public static boolean setBudget(String username, double budget) {
        int userId = getUserId(username);
        if (userId == -1) return false; // User not found

        String checkSql = "SELECT * FROM budgets WHERE ID = ?";
        String updateSql = "UPDATE budgets SET budget = ? WHERE ID = ?";
        String insertSql = "INSERT INTO budgets (ID, budget) VALUES (?, ?)";

        try (Connection conn = connect();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, userId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // If budget exists, update it
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setDouble(1, budget);
                    updateStmt.setInt(2, userId);
                    updateStmt.executeUpdate();
                    return true;
                }
            } else {
                // If no budget exists, insert a new one
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, userId);
                    insertStmt.setDouble(2, budget);
                    insertStmt.executeUpdate();
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static double getBudget(String username) {
        int userId = getUserId(username);
        if (userId == -1) return 0.0;

        String sql = "SELECT budget FROM budgets WHERE ID = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("budget");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // generate monthly report
    public static String generateMonthlyReport(String username) {
        int userId = getUserId(username);
        if (userId == -1) return "User not found";

        StringBuilder report = new StringBuilder("Monthly Report for " + username + "\n");
        String query = "SELECT category, SUM(amount) AS total FROM transactions WHERE ID = ? AND transactiondate >= DATEADD('m', -1, CURRENT_DATE) GROUP BY category";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                report.append(rs.getString("category")).append(": $").append(rs.getDouble("total")).append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error generating report.";
        }
        return report.toString();
    }

    // ✅ Get category spending
    public static List<CategoryData> getCategorySpending(String username) {
        int userId = getUserId(username);
        List<CategoryData> categoryDataList = new ArrayList<>();

        if (userId == -1) return categoryDataList;

        String sql = "SELECT category, SUM(amount) AS total FROM transactions WHERE ID = ? GROUP BY category";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                categoryDataList.add(new CategoryData(rs.getString("category"), rs.getDouble("total")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categoryDataList;
    }


    // ✅ Generate spending advice
    public static String generateSpendingAdvice(String username) {
        double budget = getBudget(username);
        double totalSpent = getTotalSpent(username);

        if (budget == 0) {
            return "No budget set!";
        }

        double spentPercentage = (totalSpent / budget) * 100;
        if (spentPercentage < 50) {
            return "Great job! You've spent only " + String.format("%.2f", spentPercentage) + "% of your budget.";
        } else if (spentPercentage < 100) {
            return "Be cautious! You've spent " + String.format("%.2f", spentPercentage) + "% of your budget.";
        } else {
            return "Warning! You've exceeded your budget.";
        }
    }

    public static List<String> getAllTransactions(String username) {
        List<String> transactions = new ArrayList<>();
        int userId = getUserId(username); // Get user ID
        if (userId == -1) return transactions; // If user ID not found, return empty list

        // query to use `transaction_id` and filter by `ID` (user ID)
        String sql = "SELECT transaction_id, category, amount, transactiondate FROM transactions WHERE ID = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);  // ✅ Use `ID` (user ID column)
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int transactionId = rs.getInt("transaction_id"); // ✅ Get `transaction_id`
                String category = rs.getString("category");
                double amount = rs.getDouble("amount");
                String date = rs.getString("transactiondate");

                // display transaction ID so users can delete transactions
                transactions.add("Transaction ID: " + transactionId + " | " + date + " | " + category + " | $" + amount);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    public static List<TransactionData> getTransactions(String username) {
        List<TransactionData> transactions = new ArrayList<>();
        int userId = getUserId(username);
        if (userId == -1) {
            return transactions;
        }

        String sql = "SELECT transaction_id, category, amount, transactiondate "
                + "FROM transactions WHERE ID = ? "
                + "ORDER BY transactiondate DESC, transaction_id DESC";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new TransactionData(
                            rs.getInt("transaction_id"),
                            rs.getString("category"),
                            rs.getDouble("amount"),
                            rs.getTimestamp("transactiondate")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    // Get user reward
    public static String getUserReward(String username, double budget) {
        double totalSpent = getTotalSpent(username);
        if (totalSpent < budget * 0.5) {
            return "Gold Saver - You're amazing!";
        } else if (totalSpent < budget * 0.8) {
            return "Silver Saver - Great job!";
        } else {
            return "Bronze Saver - Keep Saving!";
        }
    }

    // ✅ Get total spent
    public static double getTotalSpent(String username) {
        int userId = getUserId(username);
        if (userId == -1) return 0.0;

        String sql = "SELECT SUM(amount) AS total FROM transactions WHERE ID = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // Other methods...

    public static class CategoryData {
        private String category;
        private double totalAmount;

        public CategoryData(String category, double totalAmount) {
            this.category = category;
            this.totalAmount = totalAmount;
        }

        public String getCategory() {
            return category;
        }

        public double getTotalAmount() {
            return totalAmount;
        }
    }

}


