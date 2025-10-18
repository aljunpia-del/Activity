import java.time.LocalDate;

public class Transaction {
    private String transactionId;
    private String userId;
    private String bookId;
    private LocalDate dateBorrowed;
    private LocalDate dateReturned;

    public Transaction(String transactionId, String userId, String bookId, LocalDate dateBorrowed, LocalDate dateReturned) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.bookId = bookId;
        this.dateBorrowed = dateBorrowed;
        this.dateReturned = dateReturned;
    }

    public void displayTransaction() {
        System.out.println("TID: " + transactionId + ", UID: " + userId + ", BID: " + bookId + 
                           ", Borrowed: " + dateBorrowed + ", Returned: " + (dateReturned != null ? dateReturned : "Not returned"));
    }

    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public String getUserId() { return userId; }
    public String getBookId() { return bookId; }
    public LocalDate getDateBorrowed() { return dateBorrowed; }
    public LocalDate getDateReturned() { return dateReturned; }
    public void setDateReturned(LocalDate dateReturned) { this.dateReturned = dateReturned; }
}
