import java.sql.Timestamp;

public record TransactionData(int id, String category, double amount, Timestamp transactionDate) {
}
