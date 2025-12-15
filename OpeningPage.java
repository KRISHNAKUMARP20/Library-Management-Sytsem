import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.*;

public class OpeningPage extends JFrame {

    private BufferedImage bg;
    private JPanel rolePanel;

    public OpeningPage() {
        System.out.println("OpeningPage constructor started");
        // Initialize database early
        try {
            initializeDatabase();
            System.out.println("Database initialized successfully");
        } catch (Exception e) {
            System.out.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }

        setTitle("Library Management System");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try { bg = ImageIO.read(new File("00.jpg")); }
        catch (Exception e) { bg = null; }

        JPanel bgPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bg != null)
                    g.drawImage(bg.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH),0,0,null);
                else {
                    g.setColor(new Color(40,60,90));
                    g.fillRect(0,0,getWidth(),getHeight());
                }
            }
        };
        bgPanel.setLayout(new GridBagLayout());
        add(bgPanel);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(255,255,255,220));
        card.setPreferredSize(new Dimension(760,330));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10,10,10,10);

        JLabel title = new JLabel("Library Management System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 42));
        title.setForeground(Color.GREEN.darker());
        c.gridy = 0;
        card.add(title, c);

        JButton enter = new JButton("ENTER");
        enter.setFont(new Font("Segoe UI", Font.BOLD, 26));
        enter.setPreferredSize(new Dimension(220,60));
        c.gridy = 1;
        card.add(enter, c);

        JButton arrow = new JButton("^");
        arrow.setFont(new Font("Segoe UI", Font.BOLD, 20));
        arrow.setPreferredSize(new Dimension(60,35));
        c.gridy = 2;
        card.add(arrow, c);

        // Role buttons (hidden)
        rolePanel = new JPanel(new GridLayout(1,3,15,15));
        rolePanel.setOpaque(false);
        rolePanel.setVisible(false);

        JButton admin = new JButton("Admin");
        JButton librarian = new JButton("Librarian");
        JButton user = new JButton("User");

        Font rf = new Font("Segoe UI", Font.BOLD, 16);
        admin.setFont(rf);
        librarian.setFont(rf);
        user.setFont(rf);

        rolePanel.add(admin);
        rolePanel.add(librarian);
        rolePanel.add(user);

        c.gridy = 3;
        card.add(rolePanel, c);

        bgPanel.add(card);

        // ENTER -> LoginPage
        enter.addActionListener(e -> {
            dispose();
            new LoginPage();
        });

        // ^ -> show/hide dashboard roles
        arrow.addActionListener(e ->
                rolePanel.setVisible(!rolePanel.isVisible())
        );

        admin.addActionListener(e -> openDashboard("admin"));
        librarian.addActionListener(e -> openDashboard("librarian"));
        user.addActionListener(e -> openDashboard("user"));

        setVisible(true);
    }

    private void openDashboard(String role) {
        dispose();
        new DashboardPage(role, true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(OpeningPage::new);
    }

    // Database initialization methods
    private void initializeDatabase() {
        System.out.println("Initializing database...");
        try { Class.forName("org.sqlite.JDBC"); System.out.println("JDBC driver loaded."); } catch (Exception e) { System.out.println("Failed to load JDBC: " + e.getMessage()); }
        createBooksTableIfNotExists();
        createUsersTableIfNotExists();
        createLibrariansTableIfNotExists();
        createIssuesTableIfNotExists();
        insertSampleBooks();
    }

    private void createBooksTableIfNotExists() {
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

    private void createUsersTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "username TEXT NOT NULL UNIQUE," +
                "password TEXT NOT NULL," +
                "email TEXT," +
                "phone TEXT," +
                "role TEXT NOT NULL" +
                ");";
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.execute(sql);
            // Insert default users if empty
            insertDefaultUsersIfEmpty(c);
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void createLibrariansTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS librarians (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "username TEXT NOT NULL UNIQUE," +
                "password TEXT NOT NULL," +
                "email TEXT," +
                "phone TEXT," +
                "photo_path TEXT" +
                ");";
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.execute(sql);
            insertDefaultLibrariansIfEmpty(c);
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void createIssuesTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS issues (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "book_id INTEGER NOT NULL," +
                "book_title TEXT NOT NULL," +
                "user_id INTEGER NOT NULL," +
                "user_name TEXT NOT NULL," +
                "issue_date TEXT NOT NULL," +
                "due_date TEXT NOT NULL," +
                "return_date TEXT," +
                "late_days INTEGER DEFAULT 0," +
                "issued_by TEXT," +
                "remarks TEXT," +
                "FOREIGN KEY (book_id) REFERENCES books(id)," +
                "FOREIGN KEY (user_id) REFERENCES users(id)" +
                ");";
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.execute(sql);
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void insertSampleBooks() {
        try (Connection c = getConnection()) {
            // Insert sample books, ignore if already exist
            insertBook(c, "978-0-123456-78-9", "Java Programming", "John Doe", "Programming", "Tech Books Inc", "2020", "1st Edition", 5, 5);
            insertBook(c, "978-0-987654-32-1", "Data Structures", "Jane Smith", "Computer Science", "Academic Press", "2019", "2nd Edition", 3, 3);
            insertBook(c, "978-0-111111-11-1", "Database Management", "Bob Johnson", "Databases", "DB Publishers", "2021", "1st Edition", 4, 4);
            insertBook(c, "978-0-222222-22-2", "Algorithms", "Alice Brown", "Computer Science", "Algo Press", "2018", "3rd Edition", 2, 2);
            insertBook(c, "978-0-333333-33-3", "Web Development", "Charlie Wilson", "Web", "Web Books Ltd", "2022", "1st Edition", 6, 6);
            insertBook(c, "978-0-444444-44-4", "Machine Learning", "David Lee", "AI", "AI Books Co", "2023", "1st Edition", 3, 3);
            insertBook(c, "978-0-555555-55-5", "History of Computing", "Eva Green", "History", "History Press", "2017", "2nd Edition", 4, 4);
            insertBook(c, "978-0-666666-66-6", "Python Basics", "Frank White", "Programming", "Code Publishers", "2021", "Revised Edition", 5, 5);

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
                insertBook(c, isbn, title, author, category, publisher, year, edition, quantity, available);
            }
            System.out.println("Sample books ensured in database.");
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private boolean insertBook(Connection c, String isbn, String title, String author, String category, String publisher, String publishYear, String edition, int quantity, int available) {
        String sql = "INSERT OR IGNORE INTO books (isbn, title, author, category, publisher, publish_year, edition, quantity, available) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
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

    private void insertDefaultUsersIfEmpty(Connection c) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM users;";
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(countSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String sql = "INSERT INTO users (name, username, password, email, phone, role) VALUES (?, ?, ?, ?, ?, ?);";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    // Admin
                    ps.setString(1, "Admin User");
                    ps.setString(2, "admin");
                    ps.setString(3, "admin123");
                    ps.setString(4, "admin@library.com");
                    ps.setString(5, "1234567890");
                    ps.setString(6, "admin");
                    ps.executeUpdate();

                    // Librarian
                    ps.setString(1, "Librarian User");
                    ps.setString(2, "librarian");
                    ps.setString(3, "lib123");
                    ps.setString(4, "lib@library.com");
                    ps.setString(5, "0987654321");
                    ps.setString(6, "librarian");
                    ps.executeUpdate();

                    // User
                    ps.setString(1, "Regular User");
                    ps.setString(2, "user");
                    ps.setString(3, "user123");
                    ps.setString(4, "user@library.com");
                    ps.setString(5, "1122334455");
                    ps.setString(6, "user");
                    ps.executeUpdate();
                }
            }
        }
    }

    private void insertDefaultLibrariansIfEmpty(Connection c) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM librarians;";
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(countSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String sql = "INSERT INTO librarians (name, username, password, email, phone, photo_path) VALUES (?, ?, ?, ?, ?, ?);";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, "John Librarian");
                    ps.setString(2, "johnlib");
                    ps.setString(3, "john123");
                    ps.setString(4, "john@lib.com");
                    ps.setString(5, "1112223333");
                    ps.setString(6, "");
                    ps.executeUpdate();
                }
            }
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:library.db");
    }
}
