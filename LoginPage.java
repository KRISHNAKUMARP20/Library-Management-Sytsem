import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class LoginPage extends JFrame {

    // -------------------
    // In-memory stores
    // -------------------
    public static Map<String, String[]> users = new HashMap<>();       // username -> [password, role]
    public static Map<String, String> emailToUser = new HashMap<>();  // email -> username
    public static Map<String, String> phoneToUser = new HashMap<>();  // phone -> username
    public static Map<String, String> resetOtps = new HashMap<>();    // username -> otp (for reset flow)

    static {
        // default accounts
        users.put("admin", new String[]{"admin123", "admin"});
        users.put("librarian", new String[]{"lib123", "librarian"});
        users.put("user", new String[]{"user123", "user"}); // default demo user

        // default email / phone mappings (example)
        emailToUser.put("admin@example.com", "admin");
        emailToUser.put("librarian@example.com", "librarian");
        emailToUser.put("user@example.com", "user");

        phoneToUser.put("+911234567890", "admin");
        phoneToUser.put("+919876543210", "librarian");
        phoneToUser.put("+919999888777", "user");
    }

    // UI components
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox showPasswordCheck;
    private char defaultEcho;

    public LoginPage() {
        System.out.println("LoginPage constructor called");
        setTitle("Library Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // -----------------------
        // Background (00.jpg)
        // -----------------------
        ImageIcon rawBg = new ImageIcon("00.jpg");
        Image bgScaled = rawBg.getImage().getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
        JLabel background = new JLabel(new ImageIcon(bgScaled));
        background.setLayout(new GridBagLayout());
        add(background, BorderLayout.CENTER);

        // Rescale background on resize
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                ImageIcon ri = new ImageIcon("00.jpg");
                Image sc = ri.getImage().getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
                background.setIcon(new ImageIcon(sc));
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12,12,12,12);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTH;

        // -----------------------
        // Big Title (size 48, black)
        // -----------------------
        JLabel bigTitle = new JLabel("Library Management System", SwingConstants.CENTER);
        bigTitle.setFont(new Font("Segoe UI", Font.BOLD, 58));
        bigTitle.setForeground(Color.GREEN);
        bigTitle.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        background.add(bigTitle, gbc);

        // -----------------------
        // Login Card (centered)
        // -----------------------
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel card = new JPanel(new GridBagLayout());
        card.setPreferredSize(new Dimension(520, 380));
        card.setBackground(new Color(255,255,255,230));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200,200,200)),
                BorderFactory.createEmptyBorder(14,14,14,14)
        ));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8,8,8,8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;

        JLabel cardTitle = new JLabel("Library Login", SwingConstants.CENTER);
        cardTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        card.add(cardTitle, c);

        // Username
        c.gridy++; c.gridwidth = 1; c.anchor = GridBagConstraints.WEST;
        card.add(new JLabel("Username:"), c);
        c.gridx = 1;
        usernameField = new JTextField(18);
        card.add(usernameField, c);

        // Password
        c.gridy++; c.gridx = 0; c.anchor = GridBagConstraints.WEST;
        card.add(new JLabel("Password:"), c);
        c.gridx = 1;
        passwordField = new JPasswordField(18);
        defaultEcho = passwordField.getEchoChar();
        card.add(passwordField, c);

        // NEW ROW: Show Password (left) + Forget Password (right)
        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 2;

        JPanel passRow = new JPanel(new BorderLayout());
        passRow.setOpaque(false);

        // LEFT: Show password checkbox
        showPasswordCheck = new JCheckBox("Show Password");
        showPasswordCheck.setOpaque(false);
        showPasswordCheck.addActionListener(e -> {
            if (showPasswordCheck.isSelected()) passwordField.setEchoChar((char)0);
            else passwordField.setEchoChar(defaultEcho);
        });
        passRow.add(showPasswordCheck, BorderLayout.WEST);

        // RIGHT: Forget password button (opens themed dialog)
        JButton forgetBtn = new JButton("Forget Password?");
        forgetBtn.setFocusable(false);
        forgetBtn.setBorderPainted(false);
        forgetBtn.setContentAreaFilled(false);
        forgetBtn.setForeground(Color.BLUE);
        forgetBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        passRow.add(forgetBtn, BorderLayout.EAST);

        card.add(passRow, c);

        // Buttons row: Admin | Librarian | User (same row)
        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;

        JPanel loginBtnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        loginBtnRow.setOpaque(false);

        JButton adminBtn = new JButton("Login as Admin");
        JButton librarianBtn = new JButton("Login as Librarian");
        JButton userBtn = new JButton("Login as User");

        loginBtnRow.add(adminBtn);
        loginBtnRow.add(librarianBtn);
        loginBtnRow.add(userBtn);

        card.add(loginBtnRow, c);

        // Create Account row below login buttons
        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;

        JPanel createRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        createRow.setOpaque(false);
        JButton createAccountBtn = new JButton("Create Account");
        createRow.add(createAccountBtn);
        card.add(createRow, c);

        // Add card to background
        background.add(card, gbc);

        // -----------------------
        // Action listeners
        // -----------------------

        // IMPORTANT: pass LoginPage.this as owner for dialogs
        forgetBtn.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                ForgetPasswordPage.ForgetPasswordDialog fp = new ForgetPasswordPage.ForgetPasswordDialog(LoginPage.this);
                fp.setVisible(true);
            });
        });

        adminBtn.addActionListener(e -> authenticateAndOpenDashboard("admin"));
        librarianBtn.addActionListener(e -> authenticateAndOpenDashboard("librarian"));
        userBtn.addActionListener(e -> authenticateAndOpenDashboard("user"));

        // Create account: ask username, password, role, email, phone
        createAccountBtn.addActionListener(e -> {
            JTextField uField = new JTextField();
            JPasswordField pField = new JPasswordField();
            String[] roles = {"librarian", "user"};
            JComboBox<String> roleBox = new JComboBox<>(roles);
            JTextField emailField = new JTextField();
            JTextField phoneField = new JTextField();

            Object[] form = {
                    "Username:", uField,
                    "Password:", pField,
                    "Role:", roleBox,
                    "Email (optional):", emailField,
                    "Phone (optional, include +countryCode):", phoneField
            };

            int res = JOptionPane.showConfirmDialog(this, form, "Create Account", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res == JOptionPane.OK_OPTION) {
                String u = uField.getText().trim();
                String p = new String(pField.getPassword()).trim();
                String r = (String) roleBox.getSelectedItem();
                String em = emailField.getText().trim();
                String ph = phoneField.getText().trim();

                if (u.isEmpty() || p.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill username and password.", "Missing", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (users.containsKey(u)) {
                    JOptionPane.showMessageDialog(this, "Username already exists.", "Duplicate", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                users.put(u, new String[]{p, r});
                if (!em.isEmpty()) emailToUser.put(em, u);
                if (!ph.isEmpty()) phoneToUser.put(ph, u);

                JOptionPane.showMessageDialog(this, "Account created: " + u + " (" + r + ")", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Set system L&F for nicer visuals
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        setVisible(true);
    }

    private void authenticateAndOpenDashboard(String expectedRole) {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter username and password.", "Missing", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!users.containsKey(user)) {
            JOptionPane.showMessageDialog(this, "User not found.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] data = users.get(user);
        String storedPass = data[0];
        String role = data[1];

        if (!storedPass.equals(pass)) {
            JOptionPane.showMessageDialog(this, "Incorrect password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!role.equals(expectedRole)) {
            JOptionPane.showMessageDialog(this, "This account is not a " + expectedRole + ".", "Role Mismatch", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // success: open dashboard maximized and close login
        System.out.println("Login successful for role: " + expectedRole);
        SwingUtilities.invokeLater(() -> {
            DashboardPage dash = new DashboardPage(role, true);
            dash.setVisible(true);
            dash.toFront();
            dash.requestFocus();
        });
        dispose();
    }

    // Allow running LoginPage directly
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new LoginPage());
    }
}
