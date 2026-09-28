import javax.swing.table.AbstractTableModel;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class TransactionTableModel extends AbstractTableModel {
    private static final String[] COLUMN_NAMES = {"Date", "Category", "Amount", "Id"};
    private List<TransactionData> transactions = new ArrayList<>();

    public void setTransactions(List<TransactionData> transactions) {
        this.transactions = new ArrayList<>(transactions);
        fireTableDataChanged();
    }

    public TransactionData getTransactionAt(int rowIndex) {
        return transactions.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return transactions.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        TransactionData transaction = getTransactionAt(rowIndex);
        return switch (columnIndex) {
            case 0 -> transaction.transactionDate();
            case 1 -> transaction.category();
            case 2 -> transaction.amount();
            case 3 -> transaction.id();
            default -> throw new IllegalArgumentException("Unknown column: " + columnIndex);
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> Timestamp.class;
            case 1 -> String.class;
            case 2 -> Double.class;
            case 3 -> Integer.class;
            default -> Object.class;
        };
    }
}
