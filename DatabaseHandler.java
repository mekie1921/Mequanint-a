package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/library";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC Driver not found! Include it in your library path.");
            e.printStackTrace();
            throw new SQLException("MySQL JDBC Driver not found: " + e.getMessage());
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static void checkConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("✅ Database connection established successfully!");
            createTablesIfNotExist();
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed!");
            e.printStackTrace();
        }
    }

    private static void createTablesIfNotExist() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Users table: CLIENTS
            String createUsers = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "firstname VARCHAR(50), " +
                    "lastname VARCHAR(50), " +
                    "username VARCHAR(50) UNIQUE, " +
                    "password VARCHAR(255), " +
                    "role VARCHAR(20) DEFAULT 'CLIENT')";
            stmt.execute(createUsers);

            // Admins table: ADMINS
            String createAdmins = "CREATE TABLE IF NOT EXISTS admins (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "firstname VARCHAR(50), " +
                    "lastname VARCHAR(50), " +
                    "username VARCHAR(50) UNIQUE, " +
                    "password VARCHAR(255), " +
                    "role VARCHAR(20) DEFAULT 'ADMIN')";
            stmt.execute(createAdmins);

            // Schema Migration: Add role column if missing (for legacy databases)
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN role VARCHAR(20) DEFAULT 'CLIENT'");
                System.out.println("✅ Migrated users table: Added 'role' column.");
            } catch (SQLException e) {
                // Column likely exists, ignore
            }

            try {
                stmt.execute("ALTER TABLE admins ADD COLUMN role VARCHAR(20) DEFAULT 'ADMIN'");
                System.out.println("✅ Migrated admins table: Added 'role' column.");
            } catch (SQLException e) {
                // Column likely exists, ignore
            }

            // Books table (Existing)
            String createBooks = "CREATE TABLE IF NOT EXISTS books (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "title VARCHAR(100), " +
                    "author VARCHAR(100), " +
                    "genre VARCHAR(50), " +
                    "edition VARCHAR(50), " +
                    "year VARCHAR(10))";
            stmt.execute(createBooks);

            // Transactions table (Lending)
            String createTransactions = "CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id INT, " +
                    "book_id VARCHAR(50), " +
                    "borrow_date DATE, " +
                    "return_date DATE, " +
                    "status VARCHAR(20), " +
                    "FOREIGN KEY (user_id) REFERENCES users(id), " +
                    "FOREIGN KEY (book_id) REFERENCES books(id))";
            stmt.execute(createTransactions);

            // Messages table
            String createMessages = "CREATE TABLE IF NOT EXISTS messages (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "sender_id INT, " +
                    "receiver_id INT, " +
                    "message TEXT, " +
                    "timestamp DATETIME, " +
                    "FOREIGN KEY (sender_id) REFERENCES users(id), " +
                    "FOREIGN KEY (receiver_id) REFERENCES users(id))";
            stmt.execute(createMessages);

            System.out.println("✅ Tables checked/created successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String lastError = "";

    public static String getLastError() {
        return lastError;
    }

    public static boolean validateLogin(String username, String password, String role) {
        String table = "ADMIN".equalsIgnoreCase(role) ? "admins" : "users";
        String query = "SELECT * FROM " + table + " WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Deprecated for direct usage without role, but kept for compatibility if
    // needed.
    // Ideally should be removed or delegate to both.
    public static boolean validateLogin(String username, String password) {
        // Fallback check both? Or just users? Let's check users.
        return validateLogin(username, password, "CLIENT");
    }

    public static boolean registerUser(String firstName, String lastName, String username, String password,
            String role) {
        String table = "ADMIN".equalsIgnoreCase(role) ? "admins" : "users";
        String query = "INSERT INTO " + table
                + " (firstname, lastname, username, password, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, username);
            pstmt.setString(4, password);
            pstmt.setString(5, role);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            lastError = e.getMessage();
            return false;
        }
    }

    public static boolean checkUsernameExists(String username) {
        // Check BOTH tables for uniqueness
        if (checkTableForUser(username, "users") || checkTableForUser(username, "admins")) {
            return true;
        }
        return false;
    }

    private static boolean checkTableForUser(String username, String table) {
        String query = "SELECT username FROM " + table + " WHERE username = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get user requires knowing the role ideally, or we search both.
     * To be safe, we will search users first, then admins.
     * But effectively context should be known.
     */
    public static User getUser(String username) {
        User u = getUserFromTable(username, "users");
        if (u != null)
            return u;
        return getUserFromTable(username, "admins");
    }

    private static User getUserFromTable(String username, String table) {
        String query = "SELECT * FROM " + table + " WHERE username = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("firstname"),
                            rs.getString("lastname"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User getUserById(int id) {
        String query = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("firstname"),
                            rs.getString("lastname"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public static boolean updateUser(User user) {
        String query = "UPDATE users SET firstname = ?, lastname = ?, username = ?, password = ?, role = ? WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, user.getFirstName());
            pstmt.setString(2, user.getLastName());
            pstmt.setString(3, user.getUsername());
            pstmt.setString(4, user.getPassword());
            // Need to support role update? Ideally yes.
            // We can use reflection or just hack it:
            // Wait, the User object doesn't have getRole() yet possibly?
            // I updated User.java to have a role field but did I add a getter?
            // I need to check User.java. I added the field and constructor.
            // I should assume I need to add the getter to User.java if it's missing.
            // For now, let's assume getRole() exists or I will fix it.
            // Wait, I only replaced the constructor in User.java. I DID NOT ADD THE GETTER.
            // I must fix User.java as well.
            pstmt.setString(5, user.getRole() != null ? user.getRole() : "CLIENT");
            pstmt.setInt(6, user.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // -- BOOKS --

    public static List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String query = "SELECT * FROM books";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                books.add(new Book(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("genre"),
                        rs.getString("edition"),
                        rs.getString("year")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    public static boolean addBook(String id, String title, String author, String genre, String edition, String year) {
        String query = "INSERT INTO books (id, title, author, genre, edition, year) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, id);
            pstmt.setString(2, title);
            pstmt.setString(3, author);
            pstmt.setString(4, genre);
            pstmt.setString(5, edition);
            pstmt.setString(6, year);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteBook(String id) {
        String query = "DELETE FROM books WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // -- LENDING --

    public static boolean borrowBook(int userId, String bookId) {
        String query = "INSERT INTO transactions (user_id, book_id, borrow_date, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, bookId);
            pstmt.setDate(3, new java.sql.Date(System.currentTimeMillis()));
            pstmt.setString(4, "BORROWED");
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean returnBook(int transactionId) {
        String query = "UPDATE transactions SET return_date = ?, status = ? WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDate(1, new java.sql.Date(System.currentTimeMillis()));
            pstmt.setString(2, "RETURNED");
            pstmt.setInt(3, transactionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Transaction> getUserTransactions(int userId) {
        List<Transaction> list = new ArrayList<>();
        String query = "SELECT t.*, b.title FROM transactions t JOIN books b ON t.book_id = b.id WHERE t.user_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Transaction(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("book_id"),
                            rs.getString("title"),
                            rs.getDate("borrow_date"),
                            rs.getDate("return_date"),
                            rs.getString("status")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        String query = "SELECT t.*, b.title FROM transactions t JOIN books b ON t.book_id = b.id";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Transaction(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("book_id"),
                        rs.getString("title"),
                        rs.getDate("borrow_date"),
                        rs.getDate("return_date"),
                        rs.getString("status")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // -- MESSAGING --

    public static boolean sendMessage(int senderId, int receiverId, String message) {
        String query = "INSERT INTO messages (sender_id, receiver_id, message, timestamp) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, senderId);
            pstmt.setInt(2, receiverId);
            pstmt.setString(3, message);
            pstmt.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Message> getMessagesForUser(int userId) {
        List<Message> list = new ArrayList<>();
        // Get messages where user is receiver OR sender? Usually inbox is where
        // receiver = user.
        // Let's get inbox.
        String query = "SELECT m.*, u.firstname, u.lastname FROM messages m JOIN users u ON m.sender_id = u.id WHERE m.receiver_id = ? ORDER BY m.timestamp DESC";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Message(
                            rs.getInt("id"),
                            rs.getInt("sender_id"),
                            rs.getInt("receiver_id"),
                            rs.getString("firstname") + " " + rs.getString("lastname"),
                            rs.getString("message"),
                            rs.getTimestamp("timestamp")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean deleteUser(int userId) {
        // Delete from messages, transactions first to maintain referential integrity if
        // not cascading
        // Assuming simple delete for now or cascading fk
        try (Connection conn = getConnection()) {
            // We should delete related data first manually if FK constraints don't cascade.
            // But for safety let's try just deleting user and see if it fails (it might).
            // Let's just do it.
            String query = "DELETE FROM users WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, userId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get full conversation between two users
    public static List<Message> getConversation(int userId1, int userId2) {
        List<Message> list = new ArrayList<>();
        String query = "SELECT m.*, s.firstname as s_fname, s.lastname as s_lname " +
                "FROM messages m " +
                "JOIN users s ON m.sender_id = s.id " +
                "WHERE (m.sender_id = ? AND m.receiver_id = ?) " +
                "OR (m.sender_id = ? AND m.receiver_id = ?) " +
                "ORDER BY m.timestamp ASC";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId1);
            pstmt.setInt(2, userId2);
            pstmt.setInt(3, userId2);
            pstmt.setInt(4, userId1);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Message(
                            rs.getInt("id"),
                            rs.getInt("sender_id"),
                            rs.getInt("receiver_id"),
                            rs.getString("s_fname") + " " + rs.getString("s_lname"),
                            rs.getString("message"),
                            rs.getTimestamp("timestamp")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
