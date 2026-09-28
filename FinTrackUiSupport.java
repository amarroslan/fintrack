import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class FinTrackUiSupport {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.US);

    private FinTrackUiSupport() {
    }

    public static Double parsePositiveAmount(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        try {
            double amount = Double.parseDouble(input.trim());
            return Double.isFinite(amount) && amount > 0 ? amount : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static String formatCurrency(double amount) {
        return String.format(Locale.US, "RM %,.2f", amount);
    }

    public static String totalSpentLabel() {
        return "Total spent";
    }

    public static String budgetStatus(double budget, double spent) {
        if (!Double.isFinite(budget) || budget <= 0) {
            return "No budget set";
        }

        double spentPercentage = spent / budget;
        if (spentPercentage < 0.80) {
            return "On track";
        }
        if (spentPercentage <= 1.00) {
            return "Approaching limit";
        }
        return "Over budget";
    }

    public static String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return "—";
        }
        return timestamp.toLocalDateTime().format(TIMESTAMP_FORMATTER);
    }
}
