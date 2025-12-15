// DashboardPage.java
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.Date;
import java.util.Vector;

/**
 * DashboardPage single-file:
 * - Dashboard UI
 * - DBHelper (books + users + librarians + issues)
 * - Add/Edit/Delete/Search book pages
 * - ManageUsersPage (admin) with immediate-refresh capability
 * - CreateAccountPage & LoginPage
 * - ManageLibrariansPanel (with photo)
 * - IssueBooksPage & ReturnBooksPage (librarian only)
 *
 * Requires sqlite-jdbc on classpath. Put 00.jpg in same folder for background (optional).
 */
public class DashboardPage extends JFrame {

    public DashboardPage(String role) {
        this(role, false);
    }

    public DashboardPage(String role, boolean maximizeOnOpen) {
        System.out.println("DashboardPage constructor called for role: " + role);
        setTitle("Library Dashboard - " + role.toUpperCase());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        if (maximizeOnOpen) setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);

        // Optional background image named "00.jpg" in same folder
        JLabel background = createBackgroundLabel();
        add(background, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTH;

        JLabel header = new JLabel("Library Management System");
        header.setFont(new Font("Segoe UI", Font.BOLD, 60));
        header.setForeground(Color.GREEN.darker());
        background.add(header, gbc);

        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel card = new JPanel(new GridBagLayout());
        card.setPreferredSize(new Dimension(760, 420));
        card.setBackground(new Color(255, 255, 255, 230));
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(12, 12, 12, 12);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;

        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        card.add(title, c);

        c.gridy++;
        JLabel info = new JLabel("Logged in as: " + role.toUpperCase());
        info.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        card.add(info, c);

        Font buttonFont = new Font("Segoe UI", Font.BOLD, 18);
        c.gridwidth = 1;

        // --- buttons ---
        JButton searchBooksBtn = new JButton("Search books"); searchBooksBtn.setFont(buttonFont);
        JButton readDetailsBtn = new JButton("Read book details"); readDetailsBtn.setFont(buttonFont);
        JButton issueReturnBtn = new JButton("Issue & Return books"); issueReturnBtn.setFont(buttonFont);
        JButton myHistoryBtn = new JButton("View my issue history"); myHistoryBtn.setFont(buttonFont);

        JButton addBooksBtn = new JButton("Add books"); addBooksBtn.setFont(buttonFont);
        JButton editBooksBtn = new JButton("Edit books"); editBooksBtn.setFont(buttonFont);
        JButton deleteBooksBtn = new JButton("Delete books"); deleteBooksBtn.setFont(buttonFont);

        JButton manageUsersBtn = new JButton("Manage users"); manageUsersBtn.setFont(buttonFont);
        JButton manageLibrariansBtn = new JButton("Manage librarian accounts"); manageLibrariansBtn.setFont(buttonFont);

        JButton issueBtn = new JButton("Issue Books"); issueBtn.setFont(buttonFont);
        JButton returnBtn = new JButton("Return Books"); returnBtn.setFont(buttonFont);

        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 2;
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));

        // Layout per role
        if ("admin".equalsIgnoreCase(role)) {
            c.gridwidth = 1;
            c.gridy = 3; c.gridx = 0; card.add(addBooksBtn, c);
            c.gridx = 1; card.add(editBooksBtn, c);

            c.gridy++; c.gridx = 0; card.add(deleteBooksBtn, c);
            c.gridx = 1; card.add(Box.createHorizontalStrut(240), c);

            c.gridy++; c.gridx = 0; card.add(manageUsersBtn, c);
            c.gridx = 1; card.add(manageLibrariansBtn, c);

            c.gridy++; c.gridx = 0; c.gridwidth = 2; card.add(logoutBtn, c);

        } else if ("librarian".equalsIgnoreCase(role)) {
            c.gridwidth = 1;
            c.gridy = 3; c.gridx = 0; card.add(addBooksBtn, c);
            c.gridx = 1; card.add(editBooksBtn, c);

            c.gridy++; c.gridx = 0; card.add(deleteBooksBtn, c);
            c.gridx = 1; card.add(Box.createHorizontalStrut(240), c);

            c.gridy++; c.gridx = 0; card.add(issueBtn, c);
            c.gridx = 1; card.add(returnBtn, c);

            c.gridy++; c.gridx = 0; c.gridwidth = 2; card.add(logoutBtn, c);

        } else {
            c.gridwidth = 1;
            c.gridy = 3; c.gridx = 0; card.add(searchBooksBtn, c);
            c.gridx = 1; card.add(readDetailsBtn, c);

            c.gridy++; c.gridx = 0; card.add(issueReturnBtn, c);
            c.gridx = 1; card.add(myHistoryBtn, c);

            c.gridy++; c.gridx = 0; c.gridwidth = 2; card.add(logoutBtn, c);
        }

        background.add(card, gbc);

        // Actions
        logoutBtn.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginPage());
        });

        addBooksBtn.addActionListener(e -> SwingUtilities.invokeLater(() -> new AddBooksPage(role)));
        editBooksBtn.addActionListener(e -> SwingUtilities.invokeLater(() -> new EditBooksPage(role)));
        deleteBooksBtn.addActionListener(e -> SwingUtilities.invokeLater(() -> new DeleteBooksPage(role)));
        manageUsersBtn.addActionListener(e -> SwingUtilities.invokeLater(() -> new ManageUsersPage()));
        // open integrated Manage Librarians window
        manageLibrariansBtn.addActionListener(e -> SwingUtilities.invokeLater(() -> new ManageLibrariansWindow()));

        issueBtn.addActionListener(e -> SwingUtilities.invokeLater(() -> new IssueBooksPage()));
        returnBtn.addActionListener(e -> SwingUtilities.invokeLater(() -> new ReturnBooksPage()));

        // pass role into SearchBooksPage so Edit opens with correct role
        searchBooksBtn.addActionListener(e -> SwingUtilities.invokeLater(() -> new SearchBooksPage(role)));
        readDetailsBtn.addActionListener(e -> openOrMessage("ReadDetailsPage", "ReadDetailsPage not found."));
        issueReturnBtn.addActionListener(e -> openOrMessage("IssueReturnPage", "IssueReturnPage not found."));
        myHistoryBtn.addActionListener(e -> openOrMessage("MyIssueHistoryPage", "MyIssueHistoryPage not found."));

        if (maximizeOnOpen) {
            setVisible(true);
            setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
            toFront();
            requestFocus();
        } else setVisible(true);
    }

    private JLabel createBackgroundLabel() {
        ImageIcon raw = new ImageIcon("00.jpg");
        Image scaled = raw.getImage().getScaledInstance(getWidth() <= 0 ? 1000 : getWidth(), getHeight() <= 0 ? 700 : getHeight(), Image.SCALE_SMOOTH);
        JLabel background = new JLabel(new ImageIcon(scaled));
        background.setLayout(new GridBagLayout());

        // Rescale on resize
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                ImageIcon ri = new ImageIcon("00.jpg");
                Image sc = ri.getImage().getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
                background.setIcon(new ImageIcon(sc));
            }
        });
        return background;
    }

    private void openOrMessage(String className, String message) {
        try {
            Class.forName(className).getDeclaredConstructor().newInstance();
        } catch (Throwable ex) {
            JOptionPane.showMessageDialog(this, message + " Add the class if you want full page.", "Missing", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // quick tester
    public static void main(String[] args) {
        System.out.println("DashboardPage main called");
        SwingUtilities.invokeLater(() -> new DashboardPage("librarian", false)); // show as librarian by default
    }
}

/* ---------------- DBHelper (books + users + librarians + issues) ---------------- */
class DBHelper {
    private static final String DB_URL = "jdbc:sqlite:library.db";

    static void initialize() {
        try { Class.forName("org.sqlite.JDBC"); } catch (Exception ignored) {}
        createBooksTableIfNotExists();
        createUsersTableIfNotExists();
        createLibrariansTableIfNotExists(); // make sure librarians table exists
        createIssuesTableIfNotExists(); // create issues table
        insertSampleBooksIfEmpty();
    }

    // Insert sample books if table is empty
    private static void insertSampleBooksIfEmpty() {
        String countSql = "SELECT COUNT(*) FROM books;";
        try (Connection c = getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(countSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Insert 8 sample books first
                insertBook("978-0-123456-78-9", "Java Programming", "John Doe", "Programming", "Tech Books Inc", "2020", "1st Edition", 5, 5);
                insertBook("978-0-987654-32-1", "Data Structures", "Jane Smith", "Computer Science", "Academic Press", "2019", "2nd Edition", 3, 3);
                insertBook("978-0-111111-11-1", "Database Management", "Bob Johnson", "Databases", "DB Publishers", "2021", "1st Edition", 4, 4);
                insertBook("978-0-222222-22-2", "Algorithms", "Alice Brown", "Computer Science", "Algo Press", "2018", "3rd Edition", 2, 2);
                insertBook("978-0-333333-33-3", "Web Development", "Charlie Wilson", "Web", "Web Books Ltd", "2022", "1st Edition", 6, 6);
                insertBook("978-0-444444-44-4", "Machine Learning", "David Lee", "AI", "AI Books Co", "2023", "1st Edition", 3, 3);
                insertBook("978-0-555555-55-5", "History of Computing", "Eva Green", "History", "History Press", "2017", "2nd Edition", 4, 4);
                insertBook("978-0-666666-66-6", "Python Basics", "Frank White", "Programming", "Code Publishers", "2021", "Revised Edition", 5, 5);

                // Insert 100 more books programmatically
                String[] categories = {"Fiction", "Non-Fiction", "Science", "Mathematics", "History", "Technology", "Biography", "Children", "Other"};
                String[] authors = {"Author A", "Author B", "Author C", "Author D", "Author E", "Author F", "Author G", "Author H", "Author I", "Author J"};
                String[] publishers = {"Publisher X", "Publisher Y", "Publisher Z", "Publisher W", "Publisher V"};
                for (int i = 9; i <= 108; i++) {
                    String isbn = "978-0-" + String.format("%06d", i) + "-" + String.format("%02d", i % 100) + "-" + String.format("%01d", i % 10);
                    String title = "Book " + i;
                    String author = authors[i % authors.length];
                    String category = categories[i % categories.length];
                    String publisher = publishers[i % publishers.length];
                    String year = String.valueOf(2000 + (i % 25));
                    String edition = (i % 3 + 1) + "st Edition";
                    int quantity = 2 + (i % 5);
                    int available = quantity;
                    insertBook(isbn, title, author, category, publisher, year, edition, quantity, available);
                }
                System.out.println("Sample books inserted into database.");
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // Books table
    public static void createBooksTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS books (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "isbn TEXT NOT NULL," +
                "title TEXT NOT NULL," +
                "author TEXT NOT NULL," +
                "category TEXT," +
                "publisher TEXT," +
                "publish_year TEXT," +
                "edition TEXT," +
                "quantity INTEGER DEFAULT 0," +
                "available INTEGER DEFAULT 0" +
                ");";
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.execute(sql);
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    public static boolean insertBook(String isbn, String title, String author, String category,
                                     String publisher, String publishYear, String edition,
                                     int quantity, int available) {
        String sql = "INSERT INTO books (isbn, title, author, category, publisher, publish_year, edition, quantity, available) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, isbn);
            ps.setString(2, title);
            ps.setString(3, author);
            ps.setString(4, category);
            ps.setString(5, publisher);
            ps.setString(6, publishYear);
            ps.setString(7, edition);
            ps.setInt(8, quantity);
            ps.setInt(9, available);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }

    // Update book by ID
    public static boolean updateBook(int id, String isbn, String title, String author, String category,
                                     String publisher, String publishYear, String edition,
                                     int quantity, int available) {
        String sql = "UPDATE books SET isbn=?, title=?, author=?, category=?, publisher=?, publish_year=?, edition=?, quantity=?, available=? WHERE id=?;";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, isbn);
            ps.setString(2, title);
            ps.setString(3, author);
            ps.setString(4, category);
            ps.setString(5, publisher);
            ps.setString(6, publishYear);
            ps.setString(7, edition);
            ps.setInt(8, quantity);
            ps.setInt(9, available);
            ps.setInt(10, id);
            int updated = ps.executeUpdate();
            return updated > 0;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }

    // Delete book by id
    public static boolean deleteBookById(int id) {
        String sql = "DELETE FROM books WHERE id = ?;";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }

    // get book by id (returns Object array) or null
    public static Object[] getBookById(int id) {
        String sql = "SELECT id, isbn, title, author, category, publisher, publish_year, edition, quantity, available FROM books WHERE id = ? LIMIT 1;";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                            rs.getInt("id"),
                            rs.getString("isbn"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("category"),
                            rs.getString("publisher"),
                            rs.getString("publish_year"),
                            rs.getString("edition"),
                            rs.getInt("quantity"),
                            rs.getInt("available")
                    };
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return null;
    }

    // get book by ISBN
    public static Object[] getBookByISBN(String isbn) {
        String sql = "SELECT id, isbn, title, author, category, publisher, publish_year, edition, quantity, available FROM books WHERE isbn = ? LIMIT 1;";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                            rs.getInt("id"),
                            rs.getString("isbn"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("category"),
                            rs.getString("publisher"),
                            rs.getString("publish_year"),
                            rs.getString("edition"),
                            rs.getInt("quantity"),
                            rs.getInt("available")
                    };
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return null;
    }

    public static DefaultTableModel getAllBooksTableModel() {
        String[] cols = {"Book ID", "ISBN", "Title", "Author", "Category", "Publisher", "Year", "Edition", "Quantity", "Available"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int row, int column) { return false; } };
        String sql = "SELECT id, isbn, title, author, category, publisher, publish_year, edition, quantity, available FROM books ORDER BY id DESC";
        try (Connection c = getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("isbn"));
                row.add(rs.getString("title"));
                row.add(rs.getString("author"));
                row.add(rs.getString("category"));
                row.add(rs.getString("publisher"));
                row.add(rs.getString("publish_year"));
                row.add(rs.getString("edition"));
                row.add(rs.getInt("quantity"));
                row.add(rs.getInt("available"));
                model.addRow(row);
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return model;
    }

    public static DefaultTableModel searchBooksTableModel(String keyword) {
        String[] cols = {"Book ID", "ISBN", "Title", "Author", "Category", "Publisher", "Year", "Edition", "Quantity", "Available"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int row, int column) { return false; } };
        String sql = "SELECT id, isbn, title, author, category, publisher, publish_year, edition, quantity, available FROM books " +
                "WHERE isbn LIKE ? OR title LIKE ? OR author LIKE ? OR category LIKE ? ORDER BY id DESC";
        String kw = "%" + keyword + "%";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 1; i <= 4; i++) ps.setString(i, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("id"));
                    row.add(rs.getString("isbn"));
                    row.add(rs.getString("title"));
                    row.add(rs.getString("author"));
                    row.add(rs.getString("category"));
                    row.add(rs.getString("publisher"));
                    row.add(rs.getString("publish_year"));
                    row.add(rs.getString("edition"));
                    row.add(rs.getInt("quantity"));
                    row.add(rs.getInt("available"));
                    model.addRow(row);
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return model;
    }

    // ---------- Users table helpers ----------
    public static void createUsersTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "full_name TEXT NOT NULL," +
                "username TEXT NOT NULL UNIQUE," +
                "password TEXT NOT NULL," +     // store hashed in production
                "email TEXT," +
                "phone TEXT," +
                "role TEXT NOT NULL," +         // Admin / Librarian / User
                "address TEXT," +
                "gender TEXT," +
                "status TEXT DEFAULT 'Active'," + // Active / Inactive
                "date_created TEXT," +
                "last_login TEXT" +
                ");";
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.execute(sql);
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    public static boolean insertUser(String fullName, String username, String password,
                                     String email, String phone, String role,
                                     String address, String gender, String status) {
        String sql = "INSERT INTO users (full_name, username, password, email, phone, role, address, gender, status, date_created, last_login) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NULL);";
        String now = java.time.LocalDateTime.now().toString();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, username);
            ps.setString(3, password);
            ps.setString(4, email);
            ps.setString(5, phone);
            ps.setString(6, role);
            ps.setString(7, address);
            ps.setString(8, gender);
            ps.setString(9, status);
            ps.setString(10, now);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }

    public static boolean updateUser(int id, String fullName, String username, String password,
                                     String email, String phone, String role,
                                     String address, String gender, String status) {
        String sql = "UPDATE users SET full_name=?, username=?, password=?, email=?, phone=?, role=?, address=?, gender=?, status=? WHERE id=?;";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, username);
            ps.setString(3, password);
            ps.setString(4, email);
            ps.setString(5, phone);
            ps.setString(6, role);
            ps.setString(7, address);
            ps.setString(8, gender);
            ps.setString(9, status);
            ps.setInt(10, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }

    public static boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id=?;";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }

    public static DefaultTableModel getAllUsersTableModel() {
        String[] cols = {"User ID", "Full Name", "Username", "Email", "Phone", "Role", "Status", "Date Created", "Last Login"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int row, int column) { return false; } };
        String sql = "SELECT id, full_name, username, email, phone, role, status, date_created, last_login FROM users ORDER BY id DESC";
        try (Connection c = getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("full_name"));
                row.add(rs.getString("username"));
                row.add(rs.getString("email"));
                row.add(rs.getString("phone"));
                row.add(rs.getString("role"));
                row.add(rs.getString("status"));
                row.add(rs.getString("date_created"));
                row.add(rs.getString("last_login"));
                model.addRow(row);
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return model;
    }

    // ----------------- Librarians table (for Manage Librarians) -----------------
    public static void createLibrariansTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS librarians (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "full_name TEXT NOT NULL," +
                "username TEXT NOT NULL UNIQUE," +
                "password TEXT NOT NULL," +
                "email TEXT," +
                "phone TEXT," +
                "address TEXT," +
                "gender TEXT," +
                "date_joining TEXT," +
                "shift TEXT," +
                "status TEXT," +
                "photo BLOB" +
                ");";
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.execute(sql);
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    public static DefaultTableModel getAllLibrariansTableModel() {
        String[] cols = {"ID","Full Name","Username","Email","Phone","Joining Date","Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int row, int column) { return false; } };
        String q = "SELECT id, full_name, username, email, phone, date_joining, status FROM librarians ORDER BY id DESC";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(q); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Vector<Object> r = new Vector<>();
                r.add(rs.getInt("id"));
                r.add(rs.getString("full_name"));
                r.add(rs.getString("username"));
                r.add(rs.getString("email"));
                r.add(rs.getString("phone"));
                r.add(rs.getString("date_joining"));
                r.add(rs.getString("status"));
                model.addRow(r);
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return model;
    }

    // search librarians
    public static DefaultTableModel searchLibrariansTableModel(String keyword) {
        String[] cols = {"ID","Full Name","Username","Email","Phone","Joining Date","Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int row, int column) { return false; } };
        String sql = "SELECT id, full_name, username, email, phone, date_joining, status FROM librarians WHERE full_name LIKE ? OR username LIKE ? OR email LIKE ? OR phone LIKE ? ORDER BY id DESC";
        String s = "%" + keyword + "%";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i=1;i<=4;i++) ps.setString(i, s);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> r = new Vector<>();
                    r.add(rs.getInt("id"));
                    r.add(rs.getString("full_name"));
                    r.add(rs.getString("username"));
                    r.add(rs.getString("email"));
                    r.add(rs.getString("phone"));
                    r.add(rs.getString("date_joining"));
                    r.add(rs.getString("status"));
                    model.addRow(r);
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return model;
    }

    public static Object[] getLibrarianById(int id) {
        String q = "SELECT * FROM librarians WHERE id = ? LIMIT 1";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(q)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                            rs.getInt("id"),
                            rs.getString("full_name"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("address"),
                            rs.getString("gender"),
                            rs.getString("date_joining"),
                            rs.getString("shift"),
                            rs.getString("status"),
                            rs.getBytes("photo")
                    };
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return null;
    }

    public static boolean insertLibrarian(String fullName, String username, String password, String email, String phone,
                                          String address, String gender, String dateJoining, String shift, String status, byte[] photo) {
        String sql = "INSERT INTO librarians(full_name, username, password, email, phone, address, gender, date_joining, shift, status, photo) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            int idx=1;
            ps.setString(idx++, fullName);
            ps.setString(idx++, username);
            ps.setString(idx++, password);
            ps.setString(idx++, email);
            ps.setString(idx++, phone);
            ps.setString(idx++, address);
            ps.setString(idx++, gender);
            ps.setString(idx++, dateJoining);
            ps.setString(idx++, shift);
            ps.setString(idx++, status);
            if (photo != null) ps.setBytes(idx++, photo); else ps.setNull(idx++, Types.BLOB);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }

    public static boolean updateLibrarian(int id, String fullName, String username, String password, String email, String phone,
                                          String address, String gender, String dateJoining, String shift, String status, byte[] photo) {
        String sql = "UPDATE librarians SET full_name=?, username=?, password=?, email=?, phone=?, address=?, gender=?, date_joining=?, shift=?, status=?, photo=? WHERE id=?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            int idx=1;
            ps.setString(idx++, fullName);
            ps.setString(idx++, username);
            ps.setString(idx++, password);
            ps.setString(idx++, email);
            ps.setString(idx++, phone);
            ps.setString(idx++, address);
            ps.setString(idx++, gender);
            ps.setString(idx++, dateJoining);
            ps.setString(idx++, shift);
            ps.setString(idx++, status);
            if (photo != null) ps.setBytes(idx++, photo); else ps.setNull(idx++, Types.BLOB);
            ps.setInt(idx, id);
            int u = ps.executeUpdate();
            return u>0;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }

    public static boolean deleteLibrarian(int id) {
        String sql = "DELETE FROM librarians WHERE id=?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            int d = ps.executeUpdate();
            return d>0;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }

    // ----------------- ISSUES table & helpers -----------------
    public static void createIssuesTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS issues (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "book_id INTEGER," +
                "book_title TEXT," +
                "user_id INTEGER," +
                "user_name TEXT," +
                "issue_date TEXT," +
                "due_date TEXT," +
                "return_date TEXT," +
                "late_days INTEGER DEFAULT 0," +
                "issued_by TEXT," +
                "remarks TEXT" +
                ");";
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.execute(sql);
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    // Insert new issue (transactional: decrement book.available then insert issue)
    public static boolean insertIssue(int bookId, String bookTitle, int userId, String userName,
                                      String issueDate, String dueDate, String issuedBy, String remarks) {
        String insertIssue = "INSERT INTO issues (book_id, book_title, user_id, user_name, issue_date, due_date, issued_by, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
        String decAvailable = "UPDATE books SET available = available - 1 WHERE id = ? AND available > 0;";
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps2 = c.prepareStatement(decAvailable)) {
                ps2.setInt(1, bookId);
                int changed = ps2.executeUpdate();
                if (changed == 0) { c.rollback(); return false; } // no available copies
            }

            try (PreparedStatement ps1 = c.prepareStatement(insertIssue)) {
                ps1.setInt(1, bookId);
                ps1.setString(2, bookTitle);
                ps1.setInt(3, userId);
                ps1.setString(4, userName);
                ps1.setString(5, issueDate);
                ps1.setString(6, dueDate);
                ps1.setString(7, issuedBy);
                ps1.setString(8, remarks);
                ps1.executeUpdate();
            }

            c.commit();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Mark return (update issues, set return_date, late_days, append remarks) and increment book.available
    public static boolean returnIssue(int issueId, String returnDate, int lateDays, String remarks) {
        String updateIssue = "UPDATE issues SET return_date = ?, late_days = ?, remarks = COALESCE(remarks, '') || ? WHERE id = ? AND return_date IS NULL;";
        String getBookId = "SELECT book_id FROM issues WHERE id = ? LIMIT 1;";
        String incAvailable = "UPDATE books SET available = available + 1 WHERE id = ?;";
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement psGet = c.prepareStatement(getBookId)) {
                psGet.setInt(1, issueId);
                try (ResultSet rs = psGet.executeQuery()) {
                    if (!rs.next()) { c.rollback(); return false; }
                    int bookId = rs.getInt("book_id");

                    try (PreparedStatement psUpd = c.prepareStatement(updateIssue)) {
                        String appended = (remarks == null || remarks.trim().isEmpty()) ? "" : ("\nReturn remarks: " + remarks);
                        psUpd.setString(1, returnDate);
                        psUpd.setInt(2, lateDays);
                        psUpd.setString(3, appended);
                        psUpd.setInt(4, issueId);
                        int updated = psUpd.executeUpdate();
                        if (updated == 0) { c.rollback(); return false; } // already returned or not found
                    }

                    try (PreparedStatement psInc = c.prepareStatement(incAvailable)) {
                        psInc.setInt(1, bookId);
                        psInc.executeUpdate();
                    }

                    c.commit();
                    return true;
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }

    public static DefaultTableModel getAllIssuesTableModel() {
        String[] cols = {"Issue ID", "Book ID", "Book Title", "User ID", "User Name", "Issue Date", "Due Date", "Return Date", "Late Days", "Issued By", "Remarks"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int row, int column) { return false; } };
        String sql = "SELECT id, book_id, book_title, user_id, user_name, issue_date, due_date, return_date, late_days, issued_by, remarks FROM issues ORDER BY id DESC";
        try (Connection c = getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getInt("book_id"));
                row.add(rs.getString("book_title"));
                row.add(rs.getInt("user_id"));
                row.add(rs.getString("user_name"));
                row.add(rs.getString("issue_date"));
                row.add(rs.getString("due_date"));
                row.add(rs.getString("return_date"));
                row.add(rs.getInt("late_days"));
                row.add(rs.getString("issued_by"));
                row.add(rs.getString("remarks"));
                model.addRow(row);
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return model;
    }

    public static Object[] getIssueById(int issueId) {
        String sql = "SELECT id, book_id, book_title, user_id, user_name, issue_date, due_date, return_date, late_days, issued_by, remarks FROM issues WHERE id = ? LIMIT 1;";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, issueId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[] {
                            rs.getInt("id"),
                            rs.getInt("book_id"),
                            rs.getString("book_title"),
                            rs.getInt("user_id"),
                            rs.getString("user_name"),
                            rs.getString("issue_date"),
                            rs.getString("due_date"),
                            rs.getString("return_date"),
                            rs.getInt("late_days"),
                            rs.getString("issued_by"),
                            rs.getString("remarks")
                    };
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return null;
    }

    // Optional user helpers (used by Issue page)
    public static Object[] getUserById(int userId) {
        String q = "SELECT id, full_name, username FROM users WHERE id = ? LIMIT 1";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(q)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{ rs.getInt("id"), rs.getString("full_name"), rs.getString("username") };
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return null;
    }

    public static Object[] getUserByUsername(String username) {
        String q = "SELECT id, full_name, username FROM users WHERE username = ? LIMIT 1";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(q)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{ rs.getInt("id"), rs.getString("full_name"), rs.getString("username") };
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return null;
    }
}

/* ---------------- AddBooksPage ---------------- */
class AddBooksPage extends JFrame {
    private JTextField titleField, authorField, isbnField, publisherField, yearField, quantityField, editionField;
    private JComboBox<String> categoryBox;
    private String role;

    public AddBooksPage(String role) {
        this.role = role;
        setTitle("Add Book - " + role.toUpperCase());
        setSize(600, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        ImageIcon bgRaw = new ImageIcon("00.jpg");
        Image bgScaled = bgRaw.getImage().getScaledInstance(600, 520, Image.SCALE_SMOOTH);
        JLabel bgLabel = new JLabel(new ImageIcon(bgScaled));
        bgLabel.setLayout(new GridBagLayout());
        add(bgLabel);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                ImageIcon ri = new ImageIcon("00.jpg");
                Image sc = ri.getImage().getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
                bgLabel.setIcon(new ImageIcon(sc));
            }
        });

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        p.setBackground(new Color(255,255,255,210));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        c.gridx = 0; c.gridy = row; p.add(new JLabel("Book Title:"), c);
        titleField = new JTextField(); c.gridx = 1; p.add(titleField, c); row++;

        c.gridx = 0; c.gridy = row; p.add(new JLabel("Author:"), c);
        authorField = new JTextField(); c.gridx = 1; p.add(authorField, c); row++;

        c.gridx = 0; c.gridy = row; p.add(new JLabel("ISBN:"), c);
        isbnField = new JTextField(); c.gridx = 1; p.add(isbnField, c); row++;

        c.gridx = 0; c.gridy = row; p.add(new JLabel("Category / Genre:"), c);
        categoryBox = new JComboBox<>(new String[]{"Select...", "Fiction", "Non-Fiction", "Science", "Mathematics", "History", "Technology", "Biography", "Children", "Other"});
        c.gridx = 1; p.add(categoryBox, c); row++;

        c.gridx = 0; c.gridy = row; p.add(new JLabel("Publisher:"), c);
        publisherField = new JTextField(); c.gridx = 1; p.add(publisherField, c); row++;

        c.gridx = 0; c.gridy = row; p.add(new JLabel("Publish Year:"), c);
        yearField = new JTextField(); c.gridx = 1; p.add(yearField, c); row++;

        c.gridx = 0; c.gridy = row; p.add(new JLabel("Quantity / Total Copies:"), c);
        quantityField = new JTextField(); c.gridx = 1; p.add(quantityField, c); row++;

        c.gridx = 0; c.gridy = row; p.add(new JLabel("Edition (optional):"), c);
        editionField = new JTextField(); c.gridx = 1; p.add(editionField, c); row++;

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.setOpaque(false);
        JButton save = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        btns.add(cancel); btns.add(save);

        c.gridx = 0; c.gridy = row; c.gridwidth = 2; p.add(btns, c);
        bgLabel.add(p, new GridBagConstraints());

        cancel.addActionListener(e -> dispose());

        save.addActionListener(e -> {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String isbn = isbnField.getText().trim();
            String category = (String) categoryBox.getSelectedItem();
            String publisher = publisherField.getText().trim();
            String year = yearField.getText().trim();
            String qty = quantityField.getText().trim();
            String edition = editionField.getText().trim();

            if (title.isEmpty() || author.isEmpty() || isbn.isEmpty() || qty.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill required fields: Title, Author, ISBN, Quantity.");
                return;
            }

            int qtyInt = 0;
            try { qtyInt = Integer.parseInt(qty); if (qtyInt < 0) throw new NumberFormatException(); }
            catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Quantity must be a non-negative integer."); return; }

            boolean ok = DBHelper.insertBook(isbn, title, author, category, publisher, year, edition, qtyInt, qtyInt);
            if (ok) { JOptionPane.showMessageDialog(this, "Book Saved Successfully!"); dispose(); }
            else { JOptionPane.showMessageDialog(this, "Failed to save book. See console for details.", "Error", JOptionPane.ERROR_MESSAGE); }
        });

        setVisible(true);
    }
}

/* ---------------- EditBooksPage ---------------- */
class EditBooksPage extends JFrame {
    JTextField idField, isbnField, titleField, authorField, publisherField, yearField, quantityField, availableField, editionField;
    JComboBox<String> categoryBox;
    private String role;

    public EditBooksPage(String role) {
        this.role = role;
        setTitle("Edit Book - " + role.toUpperCase());
        setSize(700, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        ImageIcon bgRaw = new ImageIcon("00.jpg");
        Image bgScaled = bgRaw.getImage().getScaledInstance(700, 560, Image.SCALE_SMOOTH);
        JLabel bgLabel = new JLabel(new ImageIcon(bgScaled));
        bgLabel.setLayout(new GridBagLayout());
        add(bgLabel);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                ImageIcon ri = new ImageIcon("00.jpg");
                Image sc = ri.getImage().getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
                bgLabel.setIcon(new ImageIcon(sc));
            }
        });

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        p.setBackground(new Color(255,255,255,210));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        c.gridx = 0; c.gridy = row; p.add(new JLabel("Book ID:"), c);
        idField = new JTextField(); idField.setEditable(false); c.gridx = 1; c.gridy = row++; p.add(idField, c);

        c.gridx = 0; c.gridy = row; p.add(new JLabel("ISBN:"), c);
        isbnField = new JTextField(); c.gridx = 1; c.gridy = row++; p.add(isbnField, c);

        c.gridx = 0; c.gridy = row; p.add(new JLabel("Book Title:"), c);
        titleField = new JTextField(); c.gridx = 1; c.gridy = row++; p.add(titleField, c);

        c.gridx = 0; c.gridy = row; p.add(new JLabel("Author:"), c);
        authorField = new JTextField(); c.gridx = 1; c.gridy = row++; p.add(authorField, c);

        c.gridx = 0; c.gridy = row; p.add(new JLabel("Category / Genre:"), c);
        categoryBox = new JComboBox<>(new String[]{"Select...", "Fiction", "Non-Fiction", "Science", "Mathematics", "History", "Technology", "Biography", "Children", "Other"});
        c.gridx = 1; c.gridy = row++; p.add(categoryBox, c);

        c.gridx = 0; c.gridy = row; p.add(new JLabel("Publisher:"), c);
        publisherField = new JTextField(); c.gridx = 1; c.gridy = row++; p.add(publisherField, c);

        c.gridx = 0; c.gridy = row; p.add(new JLabel("Publish Year:"), c);
        yearField = new JTextField(); c.gridx = 1; c.gridy = row++; p.add(yearField, c);

        c.gridx = 0; c.gridy = row; p.add(new JLabel("Edition:"), c);
        editionField = new JTextField(); c.gridx = 1; c.gridy = row++; p.add(editionField, c);

        c.gridx = 0; c.gridy = row; p.add(new JLabel("Quantity / Total Copies:"), c);
        quantityField = new JTextField(); c.gridx = 1; c.gridy = row++; p.add(quantityField, c);

        c.gridx = 0; c.gridy = row; p.add(new JLabel("Available Copies:"), c);
        availableField = new JTextField(); c.gridx = 1; c.gridy = row++; p.add(availableField, c);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.setOpaque(false);
        JButton findBtn = new JButton("Find");
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        btns.add(findBtn); btns.add(saveBtn); btns.add(cancelBtn);

        c.gridx = 0; c.gridy = row; c.gridwidth = 2; p.add(btns, c);
        bgLabel.add(p, new GridBagConstraints());

        cancelBtn.addActionListener(e -> dispose());

        // Real DB-backed find: by Book ID or ISBN
        findBtn.addActionListener(e -> {
            String idText = idField.getText().trim();
            String isbnText = isbnField.getText().trim();
            Object[] book = null;

            if (!idText.isEmpty()) {
                try {
                    int id = Integer.parseInt(idText);
                    book = DBHelper.getBookById(id);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Book ID must be numeric.", "Invalid ID", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } else if (!isbnText.isEmpty()) {
                book = DBHelper.getBookByISBN(isbnText);
            } else {
                JOptionPane.showMessageDialog(this, "Enter Book ID or ISBN to find the book.", "Find", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (book == null) {
                JOptionPane.showMessageDialog(this, "Book not found in database.", "Not found", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // populate fields
            idField.setText(String.valueOf(book[0]));
            isbnField.setText(String.valueOf(book[1]));
            titleField.setText(String.valueOf(book[2]));
            authorField.setText(String.valueOf(book[3]));
            categoryBox.setSelectedItem(book[4] == null ? "Select..." : String.valueOf(book[4]));
            publisherField.setText(String.valueOf(book[5]));
            yearField.setText(String.valueOf(book[6]));
            editionField.setText(String.valueOf(book[7]));
            quantityField.setText(String.valueOf(book[8]));
            availableField.setText(String.valueOf(book[9]));
        });

        saveBtn.addActionListener(e -> {
            String idText = idField.getText().trim();
            String isbn = isbnField.getText().trim();
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String category = (String) categoryBox.getSelectedItem();
            String publisher = publisherField.getText().trim();
            String year = yearField.getText().trim();
            String edition = editionField.getText().trim();
            String qtyS = quantityField.getText().trim();
            String availS = availableField.getText().trim();

            if (isbn.isEmpty() || title.isEmpty() || author.isEmpty() || qtyS.isEmpty() || availS.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill required fields: ISBN, Title, Author, Quantity, Available Copies.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int qty, avail;
            try { qty = Integer.parseInt(qtyS); avail = Integer.parseInt(availS); if (qty < 0 || avail < 0) throw new NumberFormatException(); }
            catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Quantity and Available Copies must be non-negative integers.", "Validation", JOptionPane.WARNING_MESSAGE); return; }
            if (avail > qty) { JOptionPane.showMessageDialog(this, "Available Copies cannot exceed Quantity.", "Validation", JOptionPane.WARNING_MESSAGE); return; }

            if (idText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No Book ID present. Use Find first to load the book before saving.", "Missing ID", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int id = Integer.parseInt(idText);
            boolean ok = DBHelper.updateBook(id, isbn, title, author, category, publisher, year, edition, qty, avail);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Book updated successfully.");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update book. See console for details.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        setVisible(true);
    }
}

/* ---------------- DeleteBooksPage ---------------- */
class DeleteBooksPage extends JFrame {
    private JTextField idField, isbnField;
    private JTextField titleField, authorField, publisherField, yearField, quantityField, availableField, editionField;
    private JComboBox<String> categoryBox;
    private String role;

    public DeleteBooksPage(String role) {
        this.role = role == null ? "user" : role.toLowerCase();
        setTitle("Delete Book - " + this.role.toUpperCase());
        setSize(720, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Background (optional)
        ImageIcon bgRaw = new ImageIcon("00.jpg");
        Image bgScaled = bgRaw.getImage().getScaledInstance(720, 560, Image.SCALE_SMOOTH);
        JLabel bgLabel = new JLabel(new ImageIcon(bgScaled));
        bgLabel.setLayout(new GridBagLayout());
        add(bgLabel);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                ImageIcon ri = new ImageIcon("00.jpg");
                Image sc = ri.getImage().getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
                bgLabel.setIcon(new ImageIcon(sc));
            }
        });

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(255,255,255,220));
        p.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8,8,8,8);
        c.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // 1) Book ID (enter to find)
        c.gridx = 0; c.gridy = row; p.add(new JLabel("Book ID (enter to find):"), c);
        idField = new JTextField(); c.gridx = 1; p.add(idField, c);
        row++;

        // 2) ISBN
        c.gridx = 0; c.gridy = row; p.add(new JLabel("ISBN:"), c);
        isbnField = new JTextField(); c.gridx = 1; p.add(isbnField, c);
        row++;

        // 3) Book Title
        c.gridx = 0; c.gridy = row; p.add(new JLabel("Book Title:"), c);
        titleField = new JTextField(); titleField.setEditable(false); c.gridx = 1; p.add(titleField, c);
        row++;

        // 4) Author
        c.gridx = 0; c.gridy = row; p.add(new JLabel("Author:"), c);
        authorField = new JTextField(); authorField.setEditable(false); c.gridx = 1; p.add(authorField, c);
        row++;

        // 5) Category / Genre
        c.gridx = 0; c.gridy = row; p.add(new JLabel("Category / Genre:"), c);
        categoryBox = new JComboBox<>(new String[]{"", "Fiction", "Non-Fiction", "Science", "Mathematics", "History", "Technology", "Biography", "Children", "Other"});
        categoryBox.setEnabled(false); c.gridx = 1; p.add(categoryBox, c);
        row++;

        // 6) Publisher
        c.gridx = 0; c.gridy = row; p.add(new JLabel("Publisher:"), c);
        publisherField = new JTextField(); publisherField.setEditable(false); c.gridx = 1; p.add(publisherField, c);
        row++;

        // 7) Publish Year
        c.gridx = 0; c.gridy = row; p.add(new JLabel("Publish Year:"), c);
        yearField = new JTextField(); yearField.setEditable(false); c.gridx = 1; p.add(yearField, c);
        row++;

        // 8) Quantity / Total Copies
        c.gridx = 0; c.gridy = row; p.add(new JLabel("Quantity / Total Copies:"), c);
        quantityField = new JTextField(); quantityField.setEditable(false); c.gridx = 1; p.add(quantityField, c);
        row++;

        // 9) Available Copies
        c.gridx = 0; c.gridy = row; p.add(new JLabel("Available Copies:"), c);
        availableField = new JTextField(); availableField.setEditable(false); c.gridx = 1; p.add(availableField, c);
        row++;

        // 10) Edition
        c.gridx = 0; c.gridy = row; p.add(new JLabel("Edition:"), c);
        editionField = new JTextField(); editionField.setEditable(false); c.gridx = 1; p.add(editionField, c);
        row++;

        // Buttons: Find, Delete, Cancel
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.setOpaque(false);
        JButton findBtn = new JButton("Find");
        JButton deleteBtn = new JButton("Delete");
        JButton cancelBtn = new JButton("Cancel");
        btns.add(findBtn); btns.add(deleteBtn); btns.add(cancelBtn);

        c.gridx = 0; c.gridy = row; c.gridwidth = 2; p.add(btns, c);

        bgLabel.add(p, new GridBagConstraints());

        // Actions
        cancelBtn.addActionListener(e -> dispose());

        // Find by ID or ISBN and populate all fields in requested order
        findBtn.addActionListener(e -> {
            String idText = idField.getText().trim();
            String isbn = isbnField.getText().trim();

            Object[] book = null;
            if (!idText.isEmpty()) {
                try {
                    int id = Integer.parseInt(idText);
                    book = DBHelper.getBookById(id);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Book ID must be numeric.", "Invalid ID", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } else if (!isbn.isEmpty()) {
                book = DBHelper.getBookByISBN(isbn);
            } else {
                JOptionPane.showMessageDialog(this, "Enter Book ID or ISBN to find the book.", "Find", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (book == null) {
                JOptionPane.showMessageDialog(this, "Book not found.", "Find", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // populate fields in the order you asked
            idField.setText(String.valueOf(book[0]));
            isbnField.setText(String.valueOf(book[1]));
            titleField.setText(String.valueOf(book[2]));
            authorField.setText(String.valueOf(book[3]));
            categoryBox.setSelectedItem(book[4] == null ? "" : String.valueOf(book[4]));
            publisherField.setText(String.valueOf(book[5]));
            yearField.setText(String.valueOf(book[6]));
            quantityField.setText(String.valueOf(book[8]));
            availableField.setText(String.valueOf(book[9]));
            editionField.setText(String.valueOf(book[7]));
        });

        // Delete action (confirmed)
        deleteBtn.addActionListener(e -> {
            String idText = idField.getText().trim();
            if (idText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Find the book first (by ID or ISBN).", "Delete", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id;
            try { id = Integer.parseInt(idText); } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Book ID must be numeric.", "Invalid ID", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String title = titleField.getText().trim();
            String roleLabel = this.role.equals("librarian") ? "Librarian" : "Admin";
            int confirm = JOptionPane.showConfirmDialog(this,
                    roleLabel + " - Are you sure you want to delete the book:\n" + title + " (ID: " + id + ")?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) return;

            boolean ok = DBHelper.deleteBookById(id);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Book deleted successfully.");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete book. Check console for errors.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        setVisible(true);
    }
}

/* ---------------- SearchBooksPage ---------------- */
class SearchBooksPage extends JFrame {
    private JTable table;
    private JTextField searchField;
    private DefaultTableModel model;
    private String callerRole = "user";

    public SearchBooksPage(String role) {
        this.callerRole = role == null ? "user" : role;
        initUI();
    }

    private void initUI() {
        setTitle("Search Books");
        setSize(900, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel top = new JPanel(new BorderLayout(8,8));
        searchField = new JTextField();
        JButton searchBtn = new JButton("Search");
        JButton refreshBtn = new JButton("Refresh");
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controls.add(searchBtn);
        controls.add(refreshBtn);

        top.add(new JLabel("Search (title / author / ISBN / category): "), BorderLayout.WEST);
        top.add(searchField, BorderLayout.CENTER);
        top.add(controls, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        model = DBHelper.getAllBooksTableModel();
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // double-click -> open details (read-only for user, edit for admin/librarian)
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = table.getSelectedRow();
                    if (r == -1) return;
                    int modelRow = table.convertRowIndexToModel(r);
                    int bookId = (int) model.getValueAt(modelRow, 0);
                    Object[] book = DBHelper.getBookById(bookId);
                    if (book == null) return;
                    if ("user".equalsIgnoreCase(callerRole)) {
                        // Open read-only details for user
                        ReadDetailsPage details = new ReadDetailsPage();
                        SwingUtilities.invokeLater(() -> details.populateBook(book));
                    } else {
                        // Open edit page for admin/librarian
                        EditBooksPage edit = new EditBooksPage(callerRole);
                        SwingUtilities.invokeLater(() -> {
                            try {
                                edit.idField.setText(String.valueOf(book[0]));
                                edit.isbnField.setText(String.valueOf(book[1]));
                                edit.titleField.setText(String.valueOf(book[2]));
                                edit.authorField.setText(String.valueOf(book[3]));
                                edit.categoryBox.setSelectedItem(String.valueOf(book[4]));
                                edit.publisherField.setText(String.valueOf(book[5]));
                                edit.yearField.setText(String.valueOf(book[6]));
                                edit.editionField.setText(String.valueOf(book[7]));
                                edit.quantityField.setText(String.valueOf(book[8]));
                                edit.availableField.setText(String.valueOf(book[9]));
                            } catch (Exception ex) { ex.printStackTrace(); }
                        });
                    }
                }
            }
        });

        searchBtn.addActionListener(e -> {
            String kw = searchField.getText().trim();
            if (kw.isEmpty()) model = DBHelper.getAllBooksTableModel();
            else model = DBHelper.searchBooksTableModel(kw);
            table.setModel(model);
            table.setAutoCreateRowSorter(true);
        });

        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            model = DBHelper.getAllBooksTableModel();
            table.setModel(model);
            table.setAutoCreateRowSorter(true);
        });

        setVisible(true);
    }
}

/* ---------------- Manage Librarians: Panel + Window ---------------- */
class ManageLibrariansWindow extends JFrame {
    public ManageLibrariansWindow() {
        setTitle("Manage Librarians");
        setSize(1000, 640);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        add(new ManageLibrariansPanel());
        setVisible(true);
    }
}

class ManageLibrariansPanel extends JPanel {

    // UI components
    private JTextField txtId, txtFullName, txtUsername, txtEmail, txtPhone, txtShift;
    private JPasswordField txtPassword;
    private JTextArea txtAddress;
    private JComboBox<String> cbGender, cbStatus;
    private JFormattedTextField txtDateJoining;
    private JLabel lblPhotoPreview;
    private byte[] currentPhotoBytes = null; // holds photo bytes for current record
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JLabel lblCount;

    private static final String DB_URL = "jdbc:sqlite:library.db";

    public ManageLibrariansPanel() {
        setLayout(new BorderLayout(10,10));
        setBorder(new EmptyBorder(10,10,10,10));
        buildUI();
        DBHelper.createLibrariansTableIfNotExists();
        loadTableData("");
    }

    private void buildUI() {
        // LEFT: form card
        JPanel left = new JPanel();
        left.setLayout(new BorderLayout(8,8));
        left.setPreferredSize(new Dimension(420, 0));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Librarian Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,6,6,6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        // Hidden ID field (read-only)
        txtId = new JTextField();
        txtId.setEditable(false);
        addLabeled(form, "ID (auto):", txtId, gbc, y++);

        txtFullName = new JTextField();
        addLabeled(form, "Full Name:", txtFullName, gbc, y++);

        txtUsername = new JTextField();
        addLabeled(form, "Username:", txtUsername, gbc, y++);

        txtPassword = new JPasswordField();
        addLabeled(form, "Password:", txtPassword, gbc, y++);

        txtEmail = new JTextField();
        addLabeled(form, "Email:", txtEmail, gbc, y++);

        txtPhone = new JTextField();
        addLabeled(form, "Phone Number:", txtPhone, gbc, y++);

        // Address
        txtAddress = new JTextArea(3, 20);
        JScrollPane addrScroll = new JScrollPane(txtAddress);
        addLabeled(form, "Address (optional):", addrScroll, gbc, y++);

        // Gender & Shift on one row
        JPanel smallRow = new JPanel(new GridLayout(1,2,8,8));
        cbGender = new JComboBox<>(new String[]{"", "Male", "Female", "Other"});
        cbStatus = new JComboBox<>(new String[]{"Active", "Inactive"});
        txtShift = new JTextField();
        JPanel genderPanel = new JPanel(new BorderLayout(4,4));
        genderPanel.add(cbGender, BorderLayout.NORTH);
        genderPanel.setBorder(BorderFactory.createTitledBorder("Gender (optional)"));

        JPanel shiftPanel = new JPanel(new BorderLayout(4,4));
        shiftPanel.add(txtShift, BorderLayout.NORTH);
        shiftPanel.setBorder(BorderFactory.createTitledBorder("Shift / Working Hours"));

        smallRow.add(genderPanel);
        smallRow.add(shiftPanel);

        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.gridy = y++;
        form.add(smallRow, gbc);
        gbc.gridwidth = 1;
        gbc.weightx = 0;

        // Date of joining (simple formatted text)
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        txtDateJoining = new JFormattedTextField(df);
        txtDateJoining.setValue(new Date());
        addLabeled(form, "Date of Joining (YYYY-MM-DD):", txtDateJoining, gbc, y++);

        // Account status
        addLabeled(form, "Account Status:", cbStatus, gbc, y++);

        // Photo upload + preview
        JPanel photoPanel = new JPanel(new BorderLayout(6,6));
        lblPhotoPreview = new JLabel();
        lblPhotoPreview.setPreferredSize(new Dimension(160,140));
        lblPhotoPreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        lblPhotoPreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblPhotoPreview.setText("No Photo");
        photoPanel.add(lblPhotoPreview, BorderLayout.CENTER);

        JButton btnLoadPhoto = new JButton("Load Photo...");
        JButton btnClearPhoto = new JButton("Clear Photo");
        JPanel photoBtns = new JPanel(new GridLayout(1,2,6,6));
        photoBtns.add(btnLoadPhoto);
        photoBtns.add(btnClearPhoto);
        photoPanel.add(photoBtns, BorderLayout.SOUTH);

        gbc.gridy = y++;
        gbc.gridwidth = 2;
        form.add(photoPanel, gbc);
        gbc.gridwidth = 1;

        // Buttons: Add, Update, Delete, Clear
        JPanel buttons = new JPanel(new GridLayout(1,4,8,8));
        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");
        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);
        buttons.add(btnClear);

        gbc.gridy = y++;
        gbc.gridwidth = 2;
        form.add(buttons, gbc);
        gbc.gridwidth = 1;

        left.add(form, BorderLayout.CENTER);

        add(left, BorderLayout.WEST);

        // RIGHT: table + search
        JPanel right = new JPanel(new BorderLayout(8,8));
        right.setBorder(BorderFactory.createTitledBorder("Librarians"));

        JPanel searchRow = new JPanel(new BorderLayout(6,6));
        txtSearch = new JTextField();
        JButton btnSearch = new JButton("Search");
        searchRow.add(txtSearch, BorderLayout.CENTER);
        searchRow.add(btnSearch, BorderLayout.EAST);

        right.add(searchRow, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{
                "ID","Full Name","Username","Email","Phone","Joining Date","Status"
        }, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScroll = new JScrollPane(table);
        right.add(tableScroll, BorderLayout.CENTER);

        // bottom: record count
        lblCount = new JLabel("0 records");
        right.add(lblCount, BorderLayout.SOUTH);

        add(right, BorderLayout.CENTER);

        // Wire actions
        btnLoadPhoto.addActionListener(e -> loadPhoto());
        btnClearPhoto.addActionListener(e -> { currentPhotoBytes = null; lblPhotoPreview.setIcon(null); lblPhotoPreview.setText("No Photo"); });

        btnAdd.addActionListener(e -> {
            if (validateForAdd()) {
                addLibrarian();
                loadTableData(txtSearch.getText().trim());
            }
        });

        btnUpdate.addActionListener(e -> {
            if (validateForUpdate()) {
                updateLibrarian();
                loadTableData(txtSearch.getText().trim());
            }
        });

        btnDelete.addActionListener(e -> {
            deleteSelectedLibrarian();
            loadTableData(txtSearch.getText().trim());
        });

        btnClear.addActionListener(e -> clearForm());

        btnSearch.addActionListener(e -> loadTableData(txtSearch.getText().trim()));
        txtSearch.addActionListener(e -> loadTableData(txtSearch.getText().trim()));

        // table click -> fill form
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) fillFormFromSelected();
            }
        });
    }

    private void addLabeled(JPanel panel, String labelText, Component comp, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(comp, gbc);
    }

    private void loadTableData(String search) {
        tableModel.setRowCount(0);
        DefaultTableModel model;
        if (search == null || search.trim().isEmpty()) model = DBHelper.getAllLibrariansTableModel();
        else model = DBHelper.searchLibrariansTableModel(search);
        // copy rows from model to our tableModel (to keep same columns)
        for (int r = 0; r < model.getRowCount(); r++) {
            Vector<?> v = (Vector<?>) model.getDataVector().get(r);
            tableModel.addRow(new Object[]{
                    v.get(0), v.get(1), v.get(2), v.get(3), v.get(4), v.get(5), v.get(6)
            });
        }
        lblCount.setText(tableModel.getRowCount() + " records");
    }

    private void addLibrarian() {
        String fullName = txtFullName.getText().trim();
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String address = txtAddress.getText().trim();
        String gender = (String) cbGender.getSelectedItem();
        String dateJoining = txtDateJoining.getText().trim();
        String shift = txtShift.getText().trim();
        String status = (String) cbStatus.getSelectedItem();

        boolean ok = DBHelper.insertLibrarian(fullName, username, password, email, phone, address, gender, dateJoining, shift, status, currentPhotoBytes);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Librarian added.");
            clearForm();
        } else showError("Add failed. Check console.");
    }

    private void updateLibrarian() {
        if (txtId.getText().trim().isEmpty()) { showError("Select a record to update."); return; }
        int id = Integer.parseInt(txtId.getText().trim());
        String fullName = txtFullName.getText().trim();
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String address = txtAddress.getText().trim();
        String gender = (String) cbGender.getSelectedItem();
        String dateJoining = txtDateJoining.getText().trim();
        String shift = txtShift.getText().trim();
        String status = (String) cbStatus.getSelectedItem();

        boolean ok = DBHelper.updateLibrarian(id, fullName, username, password, email, phone, address, gender, dateJoining, shift, status, currentPhotoBytes);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Updated.");
            clearForm();
        } else showError("Update failed. Check console.");
    }

    private void deleteSelectedLibrarian() {
        int row = table.getSelectedRow();
        if (row < 0) { showError("Select a row to delete."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete librarian ID " + id + " ?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        boolean ok = DBHelper.deleteLibrarian(id);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Deleted.");
            clearForm();
        } else showError("Delete failed.");
    }

    private void fillFormFromSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int id = (int) tableModel.getValueAt(row, 0);
        Object[] rec = DBHelper.getLibrarianById(id);
        if (rec == null) return;
        txtId.setText(String.valueOf(rec[0]));
        txtFullName.setText((String) rec[1]);
        txtUsername.setText((String) rec[2]);
        txtPassword.setText((String) rec[3]);
        txtEmail.setText((String) rec[4]);
        txtPhone.setText((String) rec[5]);
        txtAddress.setText((String) rec[6]);
        cbGender.setSelectedItem(rec[7] == null ? "" : (String) rec[7]);
        txtDateJoining.setText(rec[8] == null ? "" : (String) rec[8]);
        txtShift.setText(rec[9] == null ? "" : (String) rec[9]);
        cbStatus.setSelectedItem(rec[10] == null ? "Active" : (String) rec[10]);
        currentPhotoBytes = (byte[]) rec[11];
        if (currentPhotoBytes != null && currentPhotoBytes.length > 0) {
            ImageIcon icon = new ImageIcon(currentPhotoBytes);
            Image img = icon.getImage().getScaledInstance(lblPhotoPreview.getWidth(), lblPhotoPreview.getHeight(), Image.SCALE_SMOOTH);
            lblPhotoPreview.setIcon(new ImageIcon(img));
            lblPhotoPreview.setText("");
        } else {
            lblPhotoPreview.setIcon(null);
            lblPhotoPreview.setText("No Photo");
        }
    }

    // Photo loader
    private void loadPhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg","jpeg","png","gif","bmp"));
        int ret = chooser.showOpenDialog(this);
        if (ret != JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile();
        try {
            currentPhotoBytes = readFileToBytes(f);
            ImageIcon icon = new ImageIcon(currentPhotoBytes);
            Image img = icon.getImage().getScaledInstance(lblPhotoPreview.getWidth(), lblPhotoPreview.getHeight(), Image.SCALE_SMOOTH);
            lblPhotoPreview.setIcon(new ImageIcon(img));
            lblPhotoPreview.setText("");
        } catch (IOException ex) {
            showError("Image load error: " + ex.getMessage());
        }
    }

    private static byte[] readFileToBytes(File f) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             InputStream in = new FileInputStream(f)) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) baos.write(buf, 0, n);
            return baos.toByteArray();
        }
    }

    private void clearForm() {
        txtId.setText("");
        txtFullName.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        txtAddress.setText("");
        cbGender.setSelectedIndex(0);
        txtDateJoining.setValue(new Date());
        txtShift.setText("");
        cbStatus.setSelectedIndex(0);
        currentPhotoBytes = null;
        lblPhotoPreview.setIcon(null);
        lblPhotoPreview.setText("No Photo");
        table.clearSelection();
    }

    private boolean validateForAdd() {
        if (txtFullName.getText().trim().isEmpty()) { showError("Full name required."); txtFullName.requestFocus(); return false;}
        if (txtUsername.getText().trim().isEmpty()) { showError("Username required."); txtUsername.requestFocus(); return false;}
        if (txtPassword.getPassword().length == 0) { showError("Password required."); txtPassword.requestFocus(); return false;}
        return true;
    }

    private boolean validateForUpdate() {
        if (txtId.getText().trim().isEmpty()) { showError("Select a record to update."); return false;}
        return validateForAdd();
    }

    private void showError(String msg) { JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); }
}

/* ---------------- IssueBooksPage (Librarian) & ReturnBooksPage (Librarian) ---------------- */

/* IssueBooksPage - used by librarian to issue a book */
class IssueBooksPage extends JFrame {
    private JTextField issueIdField; // optional show after insertion
    private JTextField bookIdField, isbnField, bookTitleField;
    private JTextField userIdField, usernameField, userNameField;
    private JFormattedTextField issueDateField, dueDateField;
    private JTextField issuedByField;
    private JTextArea remarksArea;

    public IssueBooksPage() {
        setTitle("Issue Book (Librarian)");
        setSize(680, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8,8,8,8);
        c.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;

        // Issue ID (read-only) - will be filled after saving if desired
        issueIdField = new JTextField();
        issueIdField.setEditable(false);
        c.gridx = 0; c.gridy = row++; c.gridwidth = 1;
        p.add(new JLabel("Issue ID (auto):"), c);
        c.gridx = 1; c.weightx = 1.0;
        p.add(issueIdField, c);
        c.weightx = 0;

        // Book: allow find by Book ID or ISBN
        bookIdField = new JTextField();
        isbnField = new JTextField();
        bookTitleField = new JTextField();
        bookTitleField.setEditable(false);
        JButton findBookByIdBtn = new JButton("Find by ID");
        JButton findBookByIsbnBtn = new JButton("Find by ISBN");

        JPanel bookRow = new JPanel(new GridBagLayout());
        GridBagConstraints br = new GridBagConstraints();
        br.insets = new Insets(4,4,4,4);
        br.fill = GridBagConstraints.HORIZONTAL;
        br.gridx = 0; br.gridy = 0;
        bookRow.add(new JLabel("Book ID:"), br);
        br.gridx = 1; bookRow.add(bookIdField, br);
        br.gridx = 2; bookRow.add(findBookByIdBtn, br);

        br.gridx = 0; br.gridy = 1;
        bookRow.add(new JLabel("ISBN:"), br);
        br.gridx = 1; bookRow.add(isbnField, br);
        br.gridx = 2; bookRow.add(findBookByIsbnBtn, br);

        br.gridx = 0; br.gridy = 2;
        bookRow.add(new JLabel("Book Title:"), br);
        br.gridx = 1; br.gridwidth = 2; bookRow.add(bookTitleField, br);
        br.gridwidth = 1;

        // add bookRow to main panel
        c.gridx = 0; c.gridy = row++; c.gridwidth = 2;
        p.add(bookRow, c);
        c.gridwidth = 1;

        // User: find by ID or username
        userIdField = new JTextField();
        usernameField = new JTextField();
        userNameField = new JTextField();
        userNameField.setEditable(false);
        JButton findUserByIdBtn = new JButton("Find by User ID");
        JButton findUserByUsernameBtn = new JButton("Find by Username");

        JPanel userRow = new JPanel(new GridBagLayout());
        GridBagConstraints ur = new GridBagConstraints();
        ur.insets = new Insets(4,4,4,4);
        ur.fill = GridBagConstraints.HORIZONTAL;

        ur.gridx = 0; ur.gridy = 0;
        userRow.add(new JLabel("User ID:"), ur);
        ur.gridx = 1; userRow.add(userIdField, ur);
        ur.gridx = 2; userRow.add(findUserByIdBtn, ur);

        ur.gridx = 0; ur.gridy = 1;
        userRow.add(new JLabel("Username:"), ur);
        ur.gridx = 1; userRow.add(usernameField, ur);
        ur.gridx = 2; userRow.add(findUserByUsernameBtn, ur);

        ur.gridx = 0; ur.gridy = 2;
        userRow.add(new JLabel("User Name:"), ur);
        ur.gridx = 1; ur.gridwidth = 2; userRow.add(userNameField, ur);
        ur.gridwidth = 1;

        c.gridx = 0; c.gridy = row++; c.gridwidth = 2;
        p.add(userRow, c);
        c.gridwidth = 1;

        // Issue / Due dates
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        issueDateField = new JFormattedTextField(df);
        issueDateField.setValue(new Date());
        dueDateField = new JFormattedTextField(df);
        // default due date = +14 days
        {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.DAY_OF_MONTH, 14);
            dueDateField.setValue(cal.getTime());
        }

        c.gridx = 0; c.gridy = row++; c.gridwidth = 1;
        p.add(new JLabel("Issue Date (YYYY-MM-DD):"), c);
        c.gridx = 1; c.weightx = 1.0;
        p.add(issueDateField, c);
        c.weightx = 0;
        c.gridx = 0; c.gridy = row++; c.gridwidth = 1;
        p.add(new JLabel("Due Date (YYYY-MM-DD):"), c);
        c.gridx = 1; c.weightx = 1.0;
        p.add(dueDateField, c);
        c.weightx = 0;

        // Issued by + remarks
        issuedByField = new JTextField();
        remarksArea = new JTextArea(4, 30);
        JScrollPane remarksScroll = new JScrollPane(remarksArea);

        c.gridx = 0; c.gridy = row++; c.gridwidth = 1;
        p.add(new JLabel("Issued By (Librarian):"), c);
        c.gridx = 1; c.weightx = 1.0;
        p.add(issuedByField, c);
        c.weightx = 0;
        c.gridx = 0; c.gridy = row++; c.gridwidth = 2;
        p.add(new JLabel("Remarks (optional):"), c);
        c.gridy = row++; p.add(remarksScroll, c);
        c.gridwidth = 1;

        // Buttons: Issue and Close
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnIssue = new JButton("Issue Book");
        JButton btnClose = new JButton("Close");
        btnRow.add(btnClose);
        btnRow.add(btnIssue);

        c.gridx = 0; c.gridy = row++; c.gridwidth = 2;
        p.add(btnRow, c);

        add(p);
        setVisible(true);

        // --- Action wiring ---

        findBookByIdBtn.addActionListener(ae -> {
            String idText = bookIdField.getText().trim();
            if (idText.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter Book ID first."); return; }
            try {
                int id = Integer.parseInt(idText);
                Object[] book = DBHelper.getBookById(id);
                if (book == null) { JOptionPane.showMessageDialog(this, "Book not found."); return; }
                bookTitleField.setText(String.valueOf(book[2]));
                int avail = (int) book[9];
                if (avail <= 0) JOptionPane.showMessageDialog(this, "Warning: No available copies currently.");
            } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Book ID must be numeric."); }
        });

        findBookByIsbnBtn.addActionListener(ae -> {
            String isbn = isbnField.getText().trim();
            if (isbn.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter ISBN first."); return; }
            Object[] book = DBHelper.getBookByISBN(isbn);
            if (book == null) { JOptionPane.showMessageDialog(this, "Book not found by ISBN."); return; }
            bookIdField.setText(String.valueOf(book[0]));
            bookTitleField.setText(String.valueOf(book[2]));
            int avail = (int) book[9];
            if (avail <= 0) JOptionPane.showMessageDialog(this, "Warning: No available copies currently.");
        });

        findUserByIdBtn.addActionListener(ae -> {
            String utext = userIdField.getText().trim();
            if (utext.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter User ID first."); return; }
            try {
                int uid = Integer.parseInt(utext);
                Object[] user = DBHelper.getUserById(uid);
                if (user == null) { JOptionPane.showMessageDialog(this, "User not found."); return; }
                usernameField.setText(String.valueOf(user[2]));
                userNameField.setText(String.valueOf(user[1]));
            } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "User ID must be numeric."); }
        });

        findUserByUsernameBtn.addActionListener(ae -> {
            String un = usernameField.getText().trim();
            if (un.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter Username first."); return; }
            Object[] user = DBHelper.getUserByUsername(un);
            if (user == null) { JOptionPane.showMessageDialog(this, "User not found."); return; }
            userIdField.setText(String.valueOf(user[0]));
            userNameField.setText(String.valueOf(user[1]));
        });

        btnIssue.addActionListener(ae -> {
            // Validate required fields
            String bIdt = bookIdField.getText().trim();
            String bTitle = bookTitleField.getText().trim();
            String uIdt = userIdField.getText().trim();
            String uName = userNameField.getText().trim();
            String issuedBy = issuedByField.getText().trim();
            String issueDate = issueDateField.getText().trim();
            String dueDate = dueDateField.getText().trim();
            String remarks = remarksArea.getText().trim();

            if (bIdt.isEmpty() || bTitle.isEmpty() || uIdt.isEmpty() || uName.isEmpty() || issuedBy.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill Book ID (or ISBN), Book Title, User ID (or Username), User Name, and Issued By.");
                return;
            }
            int bookId, userId;
            try {
                bookId = Integer.parseInt(bIdt);
                userId = Integer.parseInt(uIdt);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Book ID and User ID must be numeric.");
                return;
            }
            // attempt insert
            boolean ok = DBHelper.insertIssue(bookId, bTitle, userId, uName, issueDate, dueDate, issuedBy, remarks);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Book issued successfully.");
                // optional: refresh form, set issue id by fetching latest (simplest: fetch all issues and get first row id)
                DefaultTableModel issuesModel = DBHelper.getAllIssuesTableModel();
                if (issuesModel.getRowCount() > 0) {
                    Object latestId = issuesModel.getValueAt(0, 0);
                    issueIdField.setText(String.valueOf(latestId));
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to issue book. Maybe no available copies.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnClose.addActionListener(ae -> dispose());
    } // end IssueBooksPage constructor

} // end class IssueBooksPage


/* ---------------- ReturnBooksPage ---------------- */
class ReturnBooksPage extends JFrame {
    private JTextField issueIdField;
    private JTextField bookIdField, bookTitleField;
    private JTextField userIdField, userNameField;
    private JFormattedTextField issueDateField, dueDateField, returnDateField;
    private JTextField lateDaysField;
    private JTextArea remarksArea;
    private JButton btnFind, btnReturn, btnClose;

    public ReturnBooksPage() {
        setTitle("Return Book (Librarian)");
        setSize(680, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8,8,8,8);
        c.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;

        issueIdField = new JTextField();
        c.gridx = 0; c.gridy = row++; c.gridwidth = 1;
        p.add(new JLabel("Issue ID (enter to find):"), c);
        c.gridx = 1; c.weightx = 1.0;
        p.add(issueIdField, c);
        c.weightx = 0;

        // Book/User/Date fields (read-only)
        bookIdField = new JTextField(); bookIdField.setEditable(false);
        bookTitleField = new JTextField(); bookTitleField.setEditable(false);
        userIdField = new JTextField(); userIdField.setEditable(false);
        userNameField = new JTextField(); userNameField.setEditable(false);

        issueDateField = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
        issueDateField.setEditable(false);
        dueDateField = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
        dueDateField.setEditable(false);
        returnDateField = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
        returnDateField.setValue(new Date()); // default to today

        c.gridx = 0; c.gridy = row++; c.gridwidth = 1;
        p.add(new JLabel("Book ID:"), c);
        c.gridx = 1; c.weightx = 1.0;
        p.add(bookIdField, c);
        c.weightx = 0;
        c.gridx = 0; c.gridy = row++; c.gridwidth = 1;
        p.add(new JLabel("Book Title:"), c);
        c.gridx = 1; c.weightx = 1.0;
        p.add(bookTitleField, c);
        c.weightx = 0;
        c.gridx = 0; c.gridy = row++; c.gridwidth = 1;
        p.add(new JLabel("User ID:"), c);
        c.gridx = 1; c.weightx = 1.0;
        p.add(userIdField, c);
        c.weightx = 0;
        c.gridx = 0; c.gridy = row++; c.gridwidth = 1;
        p.add(new JLabel("User Name:"), c);
        c.gridx = 1; c.weightx = 1.0;
        p.add(userNameField, c);
        c.weightx = 0;
        c.gridx = 0; c.gridy = row++; c.gridwidth = 1;
        p.add(new JLabel("Issue Date:"), c);
        c.gridx = 1; c.weightx = 1.0;
        p.add(issueDateField, c);
        c.weightx = 0;
        c.gridx = 0; c.gridy = row++; c.gridwidth = 1;
        p.add(new JLabel("Due Date:"), c);
        c.gridx = 1; c.weightx = 1.0;
        p.add(dueDateField, c);
        c.weightx = 0;
        c.gridx = 0; c.gridy = row++; c.gridwidth = 1;
        p.add(new JLabel("Return Date (YYYY-MM-DD):"), c);
        c.gridx = 1; c.weightx = 1.0;
        p.add(returnDateField, c);
        c.weightx = 0;

        lateDaysField = new JTextField(); lateDaysField.setEditable(false);
        c.gridx = 0; c.gridy = row++; c.gridwidth = 1;
        p.add(new JLabel("Late Days:"), c);
        c.gridx = 1; c.weightx = 1.0;
        p.add(lateDaysField, c);
        c.weightx = 0;

        remarksArea = new JTextArea(4, 30);
        JScrollPane remarksScroll = new JScrollPane(remarksArea);
        c.gridx = 0; c.gridy = row++; c.gridwidth = 2;
        p.add(new JLabel("Return Remarks (optional):"), c);
        p.add(remarksScroll, c);
        c.gridwidth = 1;

        // Buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnFind = new JButton("Find Issue");
        btnReturn = new JButton("Mark Return");
        btnClose = new JButton("Close");
        btns.add(btnFind); btns.add(btnReturn); btns.add(btnClose);

        c.gridx = 0; c.gridy = row++; c.gridwidth = 2;
        p.add(btns, c);
        c.gridwidth = 1;

        add(p);
        setVisible(true);

        // Actions
        btnFind.addActionListener(ae -> {
            String idt = issueIdField.getText().trim();
            if (idt.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter Issue ID to find."); return; }
            int iid;
            try { iid = Integer.parseInt(idt); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Issue ID must be numeric."); return; }
            Object[] issue = DBHelper.getIssueById(iid);
            if (issue == null) { JOptionPane.showMessageDialog(this, "Issue not found."); return; }
            // issue columns: id, book_id, book_title, user_id, user_name, issue_date, due_date, return_date, late_days, issued_by, remarks
            bookIdField.setText(String.valueOf(issue[1]));
            bookTitleField.setText(String.valueOf(issue[2]));
            userIdField.setText(String.valueOf(issue[3]));
            userNameField.setText(String.valueOf(issue[4]));
            issueDateField.setText(String.valueOf(issue[5]));
            dueDateField.setText(String.valueOf(issue[6]));
            // show existing return_date if already returned
            String existingReturn = issue[7] == null ? "" : String.valueOf(issue[7]);
            if (!existingReturn.isEmpty()) {
                JOptionPane.showMessageDialog(this, "This issue already has a return date: " + existingReturn);
            }
            lateDaysField.setText(String.valueOf(issue[8]));
            remarksArea.setText(issue[10] == null ? "" : String.valueOf(issue[10]));
        });

        btnReturn.addActionListener(ae -> {
            String idt = issueIdField.getText().trim();
            if (idt.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter Issue ID first."); return; }
            int iid;
            try { iid = Integer.parseInt(idt); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Issue ID must be numeric."); return; }

            String returnDateStr = returnDateField.getText().trim();
            String dueDateStr = dueDateField.getText().trim();
            if (returnDateStr.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter return date."); return; }

            // parse dates and compute late days
            DateFormat dfmt = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date rDate = dfmt.parse(returnDateStr);
                Date dDate = dueDateStr == null || dueDateStr.trim().isEmpty() ? null : dfmt.parse(dueDateStr);
                int lateDays = 0;
                if (dDate != null) {
                    long diff = rDate.getTime() - dDate.getTime();
                    long days = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diff);
                    lateDays = (int) Math.max(0, days);
                }
                String rem = remarksArea.getText().trim();
                boolean ok = DBHelper.returnIssue(iid, returnDateStr, lateDays, rem);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Return recorded. Late days: " + lateDays);
                    // update lateDaysField and refresh
                    lateDaysField.setText(String.valueOf(lateDays));
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to record return. It might already be returned.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.");
            }
        });

        btnClose.addActionListener(ae -> dispose());
    } // end constructor

} // end ReturnBooksPage class


/* ---------------- ReadDetailsPage (User) ----------------
 * Shows full read-only book details (the subtitles you requested).
 * Find by Book ID or ISBN.
 */
class ReadDetailsPage extends JFrame {
    private JTextField idField, isbnField;
    private JTextField titleField, authorField, categoryField, publisherField, yearField, editionField, totalCopiesField, availableField;
    private JTextArea descriptionArea;

    public ReadDetailsPage() {
        setTitle("Read Book Details");
        setSize(760, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8,8,8,8);
        c.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;

        idField = new JTextField();
        isbnField = new JTextField();

        c.gridx = 0; c.gridy = row; p.add(new JLabel("Book ID (enter to find):"), c);
        c.gridx = 1; c.weightx = 1.0; p.add(idField, c);
        JButton btnFindById = new JButton("Find");
        c.gridx = 2; c.weightx = 0; p.add(btnFindById, c);
        row++;

        c.gridx = 0; c.gridy = row; p.add(new JLabel("ISBN (enter to find):"), c);
        c.gridx = 1; c.weightx = 1.0; p.add(isbnField, c);
        JButton btnFindByIsbn = new JButton("Find");
        c.gridx = 2; c.weightx = 0; p.add(btnFindByIsbn, c);
        row++;

        titleField = new JTextField(); titleField.setEditable(false);
        c.gridx = 0; c.gridy = row; p.add(new JLabel("Book Title:"), c);
        c.gridx = 1; c.gridwidth = 2; p.add(titleField, c); c.gridwidth = 1; row++;

        authorField = new JTextField(); authorField.setEditable(false);
        c.gridx = 0; c.gridy = row; p.add(new JLabel("Author:"), c);
        c.gridx = 1; c.gridwidth = 2; p.add(authorField, c); c.gridwidth = 1; row++;

        categoryField = new JTextField(); categoryField.setEditable(false);
        c.gridx = 0; c.gridy = row; p.add(new JLabel("Category / Genre:"), c);
        c.gridx = 1; c.gridwidth = 2; p.add(categoryField, c); c.gridwidth = 1; row++;

        publisherField = new JTextField(); publisherField.setEditable(false);
        c.gridx = 0; c.gridy = row; p.add(new JLabel("Publisher:"), c);
        c.gridx = 1; c.gridwidth = 2; p.add(publisherField, c); c.gridwidth = 1; row++;

        yearField = new JTextField(); yearField.setEditable(false);
        c.gridx = 0; c.gridy = row; p.add(new JLabel("Publish Year:"), c);
        c.gridx = 1; c.gridwidth = 2; p.add(yearField, c); c.gridwidth = 1; row++;

        editionField = new JTextField(); editionField.setEditable(false);
        c.gridx = 0; c.gridy = row; p.add(new JLabel("Edition:"), c);
        c.gridx = 1; c.gridwidth = 2; p.add(editionField, c); c.gridwidth = 1; row++;

        totalCopiesField = new JTextField(); totalCopiesField.setEditable(false);
        c.gridx = 0; c.gridy = row; p.add(new JLabel("Total Copies:"), c);
        c.gridx = 1; p.add(totalCopiesField, c);
        availableField = new JTextField(); availableField.setEditable(false);
        c.gridx = 2; p.add(new JLabel("Available Copies:"), c);
        c.gridx = 3; p.add(availableField, c);
        row++;

        descriptionArea = new JTextArea(6, 40);
        descriptionArea.setEditable(false);
        JScrollPane sp = new JScrollPane(descriptionArea);
        c.gridx = 0; c.gridy = row; c.gridwidth = 4; p.add(new JLabel("Description (optional):"), c); row++;
        c.gridx = 0; c.gridy = row; p.add(sp, c);
        c.gridwidth = 1;

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton close = new JButton("Close");
        btnRow.add(close);
        c.gridx = 0; c.gridy = row+1; c.gridwidth = 4; p.add(btnRow, c);

        add(p);
        setVisible(true);

        // Actions
        close.addActionListener(ae -> dispose());

        btnFindById.addActionListener(ae -> {
            String t = idField.getText().trim();
            if (t.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter Book ID first."); return; }
            try {
                int id = Integer.parseInt(t);
                Object[] book = DBHelper.getBookById(id);
                populateBook(book);
            } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Book ID must be numeric."); }
        });

        btnFindByIsbn.addActionListener(ae -> {
            String t = isbnField.getText().trim();
            if (t.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter ISBN first."); return; }
            Object[] book = DBHelper.getBookByISBN(t);
            populateBook(book);
        });
    }

    public void populateBook(Object[] book) {
        if (book == null) {
            JOptionPane.showMessageDialog(this, "Book not found.");
            titleField.setText(""); authorField.setText(""); categoryField.setText(""); publisherField.setText("");
            yearField.setText(""); editionField.setText(""); totalCopiesField.setText(""); availableField.setText("");
            descriptionArea.setText("");
            return;
        }
        // mapping based on DBHelper getBookById/getBookByISBN
        // book[0]=id, [1]=isbn, [2]=title, [3]=author, [4]=category, [5]=publisher, [6]=publish_year, [7]=edition, [8]=quantity, [9]=available
        idField.setText(String.valueOf(book[0]));
        isbnField.setText(String.valueOf(book[1]));
        titleField.setText(String.valueOf(book[2]));
        authorField.setText(String.valueOf(book[3]));
        categoryField.setText(book[4] == null ? "" : String.valueOf(book[4]));
        publisherField.setText(book[5] == null ? "" : String.valueOf(book[5]));
        yearField.setText(book[6] == null ? "" : String.valueOf(book[6]));
        editionField.setText(book[7] == null ? "" : String.valueOf(book[7]));
        totalCopiesField.setText(String.valueOf(book[8]));
        availableField.setText(String.valueOf(book[9]));
        // Note: your books table doesn't store a description column - if you add one, populate it here.
        descriptionArea.setText("");
    }
}

/* ---------------- IssueReturnPage (User) ----------------
 * Combined user page where a user can request an issue or request a return.
 * This is a lightweight user-facing flow (doesn't require librarian).
 */
class IssueReturnPage extends JFrame {
    public IssueReturnPage() {
        setTitle("Issue & Return (User)");
        setSize(820, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();

        // ----- Issue Request Panel -----
        JPanel issuePanel = new JPanel(new GridBagLayout());
        GridBagConstraints ic = new GridBagConstraints();
        ic.insets = new Insets(8,8,8,8);
        ic.fill = GridBagConstraints.HORIZONTAL;
        int r = 0;

        JTextField bIdField = new JTextField();
        JTextField isbnField = new JTextField();
        JTextField bTitleField = new JTextField(); bTitleField.setEditable(false);
        JButton findById = new JButton("Find by ID");
        JButton findByIsbn = new JButton("Find by ISBN");

        ic.gridx = 0; ic.gridy = r; issuePanel.add(new JLabel("Book ID (or find):"), ic);
        ic.gridx = 1; ic.weightx = 1.0; issuePanel.add(bIdField, ic);
        ic.gridx = 2; ic.weightx = 0; issuePanel.add(findById, ic); r++;

        ic.gridx = 0; ic.gridy = r; issuePanel.add(new JLabel("ISBN (or find):"), ic);
        ic.gridx = 1; ic.weightx = 1.0; issuePanel.add(isbnField, ic);
        ic.gridx = 2; ic.weightx = 0; issuePanel.add(findByIsbn, ic); r++;

        ic.gridx = 0; ic.gridy = r; issuePanel.add(new JLabel("Book Title:"), ic);
        ic.gridx = 1; ic.gridwidth = 2; issuePanel.add(bTitleField, ic); ic.gridwidth = 1; r++;

        JTextField userIdField = new JTextField();
        JTextField usernameField = new JTextField();
        JTextField userNameField = new JTextField(); userNameField.setEditable(false);
        JButton findUserById = new JButton("Find by User ID");
        JButton findUserByUsername = new JButton("Find by Username");

        ic.gridx = 0; ic.gridy = r; issuePanel.add(new JLabel("User ID:"), ic);
        ic.gridx = 1; ic.weightx = 1.0; issuePanel.add(userIdField, ic);
        ic.gridx = 2; ic.weightx = 0; issuePanel.add(findUserById, ic); r++;

        ic.gridx = 0; ic.gridy = r; issuePanel.add(new JLabel("Username:"), ic);
        ic.gridx = 1; ic.weightx = 1.0; issuePanel.add(usernameField, ic);
        ic.gridx = 2; ic.weightx = 0; issuePanel.add(findUserByUsername, ic); r++;

        ic.gridx = 0; ic.gridy = r; issuePanel.add(new JLabel("User Full Name:"), ic);
        ic.gridx = 1; ic.gridwidth = 2; issuePanel.add(userNameField, ic); ic.gridwidth = 1; r++;

        JFormattedTextField issueDateField = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
        issueDateField.setValue(new Date());
        JFormattedTextField dueDateField = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
        {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.DAY_OF_MONTH, 14);
            dueDateField.setValue(cal.getTime());
        }

        ic.gridx = 0; ic.gridy = r; issuePanel.add(new JLabel("Issue Date (YYYY-MM-DD):"), ic);
        ic.gridx = 1; ic.weightx = 1.0; issuePanel.add(issueDateField, ic); r++;

        ic.gridx = 0; ic.gridy = r; issuePanel.add(new JLabel("Due Date (YYYY-MM-DD):"), ic);
        ic.gridx = 1; ic.weightx = 1.0; issuePanel.add(dueDateField, ic); r++;

        JTextField requestedByField = new JTextField(); // user can mention their name
        JTextArea remarksArea = new JTextArea(4, 30);
        JScrollPane remarksScroll = new JScrollPane(remarksArea);

        ic.gridx = 0; ic.gridy = r; issuePanel.add(new JLabel("Requested By (Name):"), ic);
        ic.gridx = 1; ic.gridwidth = 2; issuePanel.add(requestedByField, ic); ic.gridwidth = 1; r++;

        ic.gridx = 0; ic.gridy = r; ic.gridwidth = 3; issuePanel.add(new JLabel("Remarks (optional):"), ic); r++;
        ic.gridx = 0; ic.gridy = r; issuePanel.add(remarksScroll, ic); ic.gridwidth = 1; r++;

        JPanel issueBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRequestIssue = new JButton("Request Issue");
        JButton btnCloseIssue = new JButton("Close");
        issueBtns.add(btnCloseIssue); issueBtns.add(btnRequestIssue);
        ic.gridx = 0; ic.gridy = r; ic.gridwidth = 3; issuePanel.add(issueBtns, ic);

        // ----- Return Request Panel -----
        JPanel returnPanel = new JPanel(new GridBagLayout());
        GridBagConstraints rc = new GridBagConstraints();
        rc.insets = new Insets(8,8,8,8);
        rc.fill = GridBagConstraints.HORIZONTAL;
        int rr = 0;

        JTextField issueIdField = new JTextField();
        JButton findIssueBtn = new JButton("Find Issue");
        JTextField rBookId = new JTextField(); rBookId.setEditable(false);
        JTextField rBookTitle = new JTextField(); rBookTitle.setEditable(false);
        JTextField rUserId = new JTextField(); rUserId.setEditable(false);
        JTextField rUserName = new JTextField(); rUserName.setEditable(false);
        JFormattedTextField rIssueDate = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd")); rIssueDate.setEditable(false);
        JFormattedTextField rDueDate = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd")); rDueDate.setEditable(false);
        JFormattedTextField rReturnDate = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd")); rReturnDate.setValue(new Date());
        JTextField rLateDays = new JTextField(); rLateDays.setEditable(false);
        JTextArea rRemarks = new JTextArea(4, 30);
        JScrollPane rRemarksScroll = new JScrollPane(rRemarks);

        rc.gridx = 0; rc.gridy = rr; returnPanel.add(new JLabel("Issue ID (enter to find):"), rc);
        rc.gridx = 1; rc.weightx = 1.0; returnPanel.add(issueIdField, rc);
        rc.gridx = 2; rc.weightx = 0; returnPanel.add(findIssueBtn, rc); rr++;

        rc.gridx = 0; rc.gridy = rr; returnPanel.add(new JLabel("Book ID:"), rc);
        rc.gridx = 1; rc.gridwidth = 2; returnPanel.add(rBookId, rc); rc.gridwidth = 1; rr++;

        rc.gridx = 0; rc.gridy = rr; returnPanel.add(new JLabel("Book Title:"), rc);
        rc.gridx = 1; rc.gridwidth = 2; returnPanel.add(rBookTitle, rc); rc.gridwidth = 1; rr++;

        rc.gridx = 0; rc.gridy = rr; returnPanel.add(new JLabel("User ID:"), rc);
        rc.gridx = 1; rc.gridwidth = 2; returnPanel.add(rUserId, rc); rc.gridwidth = 1; rr++;

        rc.gridx = 0; rc.gridy = rr; returnPanel.add(new JLabel("User Name:"), rc);
        rc.gridx = 1; rc.gridwidth = 2; returnPanel.add(rUserName, rc); rc.gridwidth = 1; rr++;

        rc.gridx = 0; rc.gridy = rr; returnPanel.add(new JLabel("Issue Date:"), rc);
        rc.gridx = 1; returnPanel.add(rIssueDate, rc); rr++;

        rc.gridx = 0; rc.gridy = rr; returnPanel.add(new JLabel("Due Date:"), rc);
        rc.gridx = 1; returnPanel.add(rDueDate, rc); rr++;

        rc.gridx = 0; rc.gridy = rr; returnPanel.add(new JLabel("Return Date (YYYY-MM-DD):"), rc);
        rc.gridx = 1; returnPanel.add(rReturnDate, rc); rr++;

        rc.gridx = 0; rc.gridy = rr; returnPanel.add(new JLabel("Late Days:"), rc);
        rc.gridx = 1; returnPanel.add(rLateDays, rc); rr++;

        rc.gridx = 0; rc.gridy = rr; rc.gridwidth = 3; returnPanel.add(new JLabel("Return Remarks (optional):"), rc); rr++;
        rc.gridx = 0; rc.gridy = rr; rc.gridwidth = 3; returnPanel.add(rRemarksScroll, rc); rr++;

        JPanel returnBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRequestReturn = new JButton("Request Return");
        JButton btnCloseReturn = new JButton("Close");
        returnBtns.add(btnCloseReturn); returnBtns.add(btnRequestReturn);
        rc.gridx = 0; rc.gridy = rr; rc.gridwidth = 3; returnPanel.add(returnBtns, rc);

        // Add tabs
        tabs.addTab("Request Issue", issuePanel);
        tabs.addTab("Request Return", returnPanel);
        add(tabs);

        setVisible(true);

        // --- wiring actions ---

        // Find book for issue
        findById.addActionListener(ae -> {
            String t = bIdField.getText().trim();
            if (t.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter Book ID first."); return; }
            try {
                int id = Integer.parseInt(t);
                Object[] book = DBHelper.getBookById(id);
                if (book == null) { JOptionPane.showMessageDialog(this, "Book not found."); return; }
                bTitleField.setText(String.valueOf(book[2]));
                int avail = (int) book[9];
                if (avail <= 0) JOptionPane.showMessageDialog(this, "No available copies currently.");
            } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Book ID must be numeric."); }
        });

        findByIsbn.addActionListener(ae -> {
            String t = isbnField.getText().trim();
            if (t.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter ISBN first."); return; }
            Object[] book = DBHelper.getBookByISBN(t);
            if (book == null) { JOptionPane.showMessageDialog(this, "Book not found."); return; }
            bIdField.setText(String.valueOf(book[0]));
            bTitleField.setText(String.valueOf(book[2]));
            int avail = (int) book[9];
            if (avail <= 0) JOptionPane.showMessageDialog(this, "No available copies currently.");
        });

        // Find user
        findUserById.addActionListener(ae -> {
            String t = userIdField.getText().trim();
            if (t.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter User ID first."); return; }
            try {
                int uid = Integer.parseInt(t);
                Object[] u = DBHelper.getUserById(uid);
                if (u == null) { JOptionPane.showMessageDialog(this, "User not found."); return; }
                usernameField.setText(String.valueOf(u[2]));
                userNameField.setText(String.valueOf(u[1]));
            } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "User ID must be numeric."); }
        });

        findUserByUsername.addActionListener(ae -> {
            String un = usernameField.getText().trim();
            if (un.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter username first."); return; }
            Object[] u = DBHelper.getUserByUsername(un);
            if (u == null) { JOptionPane.showMessageDialog(this, "User not found."); return; }
            userIdField.setText(String.valueOf(u[0]));
            userNameField.setText(String.valueOf(u[1]));
        });

        btnRequestIssue.addActionListener(ae -> {
            String bIdt = bIdField.getText().trim();
            String bTitle = bTitleField.getText().trim();
            String uIdt = userIdField.getText().trim();
            String uName = userNameField.getText().trim();
            String requestedBy = requestedByField.getText().trim();
            String issueDate = issueDateField.getText().trim();
            String dueDate = dueDateField.getText().trim();
            String remarks = remarksArea.getText().trim();

            if (bIdt.isEmpty() || bTitle.isEmpty() || uIdt.isEmpty() || uName.isEmpty() || requestedBy.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill Book ID (or ISBN), Book Title, User ID (or Username), User Name, and Requested By.");
                return;
            }
            int bookId, userId;
            try { bookId = Integer.parseInt(bIdt); userId = Integer.parseInt(uIdt); }
            catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Book ID and User ID must be numeric."); return; }

            boolean ok = DBHelper.insertIssue(bookId, bTitle, userId, uName, issueDate, dueDate, requestedBy, remarks);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Issue request recorded. A librarian will process it.");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to record issue request. Maybe no available copies.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Find issue for return
        findIssueBtn.addActionListener(ae -> {
            String it = issueIdField.getText().trim();
            if (it.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter Issue ID first."); return; }
            try {
                int iid = Integer.parseInt(it);
                Object[] iss = DBHelper.getIssueById(iid);
                if (iss == null) { JOptionPane.showMessageDialog(this, "Issue not found."); return; }
                // issue columns: id, book_id, book_title, user_id, user_name, issue_date, due_date, return_date, late_days, issued_by, remarks
                rBookId.setText(String.valueOf(iss[1]));
                rBookTitle.setText(String.valueOf(iss[2]));
                rUserId.setText(String.valueOf(iss[3]));
                rUserName.setText(String.valueOf(iss[4]));
                rIssueDate.setText(String.valueOf(iss[5]));
                rDueDate.setText(String.valueOf(iss[6]));
                String existingReturn = iss[7] == null ? "" : String.valueOf(iss[7]);
                if (!existingReturn.isEmpty()) JOptionPane.showMessageDialog(this, "This issue already returned on: " + existingReturn);
                rLateDays.setText(String.valueOf(iss[8]));
            } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Issue ID must be numeric."); }
        });

        btnRequestReturn.addActionListener(ae -> {
            String it = issueIdField.getText().trim();
            if (it.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter Issue ID first."); return; }
            int iid;
            try { iid = Integer.parseInt(it); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Issue ID must be numeric."); return; }

            String returnDateStr = rReturnDate.getText().trim();
            String dueDateStr = rDueDate.getText().trim();

            try {
                DateFormat dfmt = new SimpleDateFormat("yyyy-MM-dd");
                Date rDate = dfmt.parse(returnDateStr);
                Date dDate = dueDateStr == null || dueDateStr.trim().isEmpty() ? null : dfmt.parse(dueDateStr);
                int lateDays = 0;
                if (dDate != null) {
                    long diff = rDate.getTime() - dDate.getTime();
                    long days = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diff);
                    lateDays = (int) Math.max(0, days);
                }
                String rem = rRemarks.getText().trim();
                boolean ok = DBHelper.returnIssue(iid, returnDateStr, lateDays, rem);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Return request recorded. Late days: " + lateDays);
                    rLateDays.setText(String.valueOf(lateDays));
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to record return. It might already be returned.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.");
            }
        });

        // Close buttons
        btnCloseIssue.addActionListener(ae -> dispose());
        btnCloseReturn.addActionListener(ae -> dispose());
    }
}

/* ---------------- MyIssueHistoryPage ----------------
 * Show user's issue history. Since we don't have a global "current user" here,
 * page allows filtering by User ID or Username.
 */
class MyIssueHistoryPage extends JFrame {
    private JTable table;
    private DefaultTableModel model;

    public MyIssueHistoryPage() {
        setTitle("My Issue History");
        setSize(1000, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JTextField filterField = new JTextField(30);
        JButton btnSearch = new JButton("Search (User ID or Username)");
        JButton btnRefresh = new JButton("Refresh (All)");
        top.add(new JLabel("Filter:"));
        top.add(filterField);
        top.add(btnSearch);
        top.add(btnRefresh);

        add(top, BorderLayout.NORTH);

        model = DBHelper.getAllIssuesTableModel();
        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // bottom: status
        JLabel lblStatus = new JLabel(model.getRowCount() + " records");
        add(lblStatus, BorderLayout.SOUTH);

        setVisible(true);

        btnRefresh.addActionListener(ae -> {
            model = DBHelper.getAllIssuesTableModel();
            table.setModel(model);
            table.setAutoCreateRowSorter(true);
            lblStatus.setText(model.getRowCount() + " records");
        });

        btnSearch.addActionListener(ae -> {
            String q = filterField.getText().trim();
            if (q.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter a User ID or Username to filter, or click Refresh to show all.");
                return;
            }
            DefaultTableModel all = DBHelper.getAllIssuesTableModel();
            DefaultTableModel filtered = new DefaultTableModel(new String[]{
                    "Issue ID", "Book ID", "Book Title", "User ID", "User Name", "Issue Date", "Due Date", "Return Date", "Late Days", "Issued By", "Remarks"
            }, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
            // if numeric, filter by user id; else by username substring (case-insensitive)
            boolean numeric = true;
            try { Integer.parseInt(q); } catch (NumberFormatException ex) { numeric = false; }
            for (int i = 0; i < all.getRowCount(); i++) {
                Object uid = all.getValueAt(i, 3);
                Object uname = all.getValueAt(i, 4);
                if (numeric) {
                    if (String.valueOf(uid).equals(q)) filtered.addRow(getRowVector(all, i));
                } else {
                    if (uname != null && String.valueOf(uname).toLowerCase().contains(q.toLowerCase())) filtered.addRow(getRowVector(all, i));
                }
            }
            table.setModel(filtered);
            table.setAutoCreateRowSorter(true);
            lblStatus.setText(filtered.getRowCount() + " records (filtered)");
        });

        // helper: copy row
    }

    private Vector<Object> getRowVector(DefaultTableModel m, int row) {
        Vector<Object> v = new Vector<>();
        for (int c = 0; c < m.getColumnCount(); c++) v.add(m.getValueAt(row, c));
        return v;
    }
}


