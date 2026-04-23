package application;

import java.sql.Date;

public class Transaction {
    private int id;
    private int userId;
    private String bookId;
    private String bookTitle; // Helper for UI
    private Date borrowDate;
    private Date returnDate; // Null if not returned
    private String status; // "BORROWED", "RETURNED"

    public Transaction(int id, int userId, String bookId, String bookTitle, Date borrowDate, Date returnDate,
            String status) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getBookId() {
        return bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public Date getBorrowDate() {
        return borrowDate;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public String getStatus() {
        return status;
    }
}
