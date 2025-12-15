import javax.swing.*;
import java.awt.*;


public class CreateAccountPage extends JDialog {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleCombo;
    private JButton createBtn, cancelBtn;

    public CreateAccountPage(JFrame owner) {
        super(owner, "Create Account", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(380, 260);
        setResizable(false);
        setLocationRelativeTo(owner); // center relative to login

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8,8,8,8);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0;
        panel.add(new JLabel("Username:"), c);
        c.gridx = 1;
        usernameField = new JTextField(16);
        panel.add(usernameField, c);

        c.gridx = 0; c.gridy++;
        panel.add(new JLabel("Password:"), c);
        c.gridx = 1;
        passwordField = new JPasswordField(16);
        panel.add(passwordField, c);

        c.gridx = 0; c.gridy++;
        panel.add(new JLabel("Role:"), c);
        c.gridx = 1;
        roleCombo = new JComboBox<>(new String[]{"librarian", "user"});
        panel.add(roleCombo, c);

        c.gridx = 0; c.gridy++;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        createBtn = new JButton("Create Account");
        cancelBtn = new JButton("Cancel");
        btnRow.add(createBtn);
        btnRow.add(cancelBtn);
        panel.add(btnRow, c);

        createBtn.addActionListener(e -> createAccount());
        cancelBtn.addActionListener(e -> dispose());

        add(panel);
    }

    private void createAccount() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();
        String role = (String) roleCombo.getSelectedItem();

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password required.", "Missing", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (LoginPage.users.containsKey(user)) {
            JOptionPane.showMessageDialog(this, "Username already exists.", "Duplicate", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LoginPage.users.put(user, new String[]{pass, role});
        JOptionPane.showMessageDialog(this, "Account created: " + user + " (" + role + ")", "Success", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}
