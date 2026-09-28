import java.sql.Timestamp;
import java.util.Arrays;

public class TransactionTableModelTest {
    public static void main(String[] args) {
        TransactionTableModel model = new TransactionTableModel();

        assertEquals(4, model.getColumnCount(), "column count");
        assertEquals("Date", model.getColumnName(0), "date column");
        assertEquals("Category", model.getColumnName(1), "category column");
        assertEquals("Amount", model.getColumnName(2), "amount column");
        assertEquals("Id", model.getColumnName(3), "id column");
        assertEquals(0, model.getRowCount(), "empty row count");

        TransactionData first = new TransactionData(7, "Food", 12.5,
                Timestamp.valueOf("2026-09-28 12:30:00"));
        TransactionData second = new TransactionData(8, "Transport", 8.0,
                Timestamp.valueOf("2026-09-27 08:15:00"));
        model.setTransactions(Arrays.asList(first, second));

        assertEquals(2, model.getRowCount(), "loaded row count");
        assertEquals(first, model.getTransactionAt(0), "first typed row");
        assertEquals(second, model.getTransactionAt(1), "second typed row");
        System.out.println("TransactionTableModelTest: 8 assertions passed");
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + ": expected " + expected + " but got " + actual);
        }
    }
}
