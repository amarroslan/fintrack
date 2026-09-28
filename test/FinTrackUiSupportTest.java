import java.sql.Timestamp;

public class FinTrackUiSupportTest {
    public static void main(String[] args) {
        testParsesPositiveAmount();
        testRejectsInvalidAmounts();
        testFormatsCurrency();
        testDerivesBudgetStatus();
        testPreservesTransactionData();
        System.out.println("FinTrackUiSupportTest: 5 tests passed");
    }

    private static void testParsesPositiveAmount() {
        assertEquals(Double.valueOf(12.5), FinTrackUiSupport.parsePositiveAmount(" 12.50 "), "positive amount");
    }

    private static void testRejectsInvalidAmounts() {
        String[] invalid = {"", "   ", "-1", "0", "NaN", "Infinity", "not-a-number"};
        for (String value : invalid) {
            assertNull(FinTrackUiSupport.parsePositiveAmount(value), "invalid amount: " + value);
        }
    }

    private static void testFormatsCurrency() {
        assertEquals("RM 1,250.50", FinTrackUiSupport.formatCurrency(1250.5), "currency formatting");
    }

    private static void testDerivesBudgetStatus() {
        assertEquals("No budget set", FinTrackUiSupport.budgetStatus(0, 0), "missing budget status");
        assertEquals("On track", FinTrackUiSupport.budgetStatus(1000, 500), "on track status");
        assertEquals("Approaching limit", FinTrackUiSupport.budgetStatus(1000, 850), "approaching status");
        assertEquals("Over budget", FinTrackUiSupport.budgetStatus(1000, 1200), "over budget status");
    }

    private static void testPreservesTransactionData() {
        Timestamp timestamp = Timestamp.valueOf("2026-09-28 12:30:00");
        TransactionData transaction = new TransactionData(7, "Food", 12.5, timestamp);
        assertEquals(7, transaction.id(), "transaction id");
        assertEquals("Food", transaction.category(), "transaction category");
        assertEquals(Double.valueOf(12.5), transaction.amount(), "transaction amount");
        assertEquals(timestamp, transaction.transactionDate(), "transaction date");
    }

    private static void assertNull(Object actual, String message) {
        if (actual != null) {
            throw new AssertionError(message + ": expected null but got " + actual);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + ": expected " + expected + " but got " + actual);
        }
    }

}
