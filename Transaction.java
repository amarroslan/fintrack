public class Transaction {
    private String category;
    private double amount;

    public Transaction(String category, double amount) {
        this.category = category;
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory(){
        return category;
    }

    @Override
    public boolean equals(Object obj) { //equals() implementation
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Transaction that = (Transaction) obj;
        return Double.compare(that.amount, amount) == 0 && category.equals(that.category);
    }

    @Override
    public String toString() { //toString() implementation
        return "Category: " + category + ", Amount: " + amount;
    }
}


