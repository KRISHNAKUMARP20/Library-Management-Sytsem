import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;

/**
 * ManageUsersPage - admin-only user management UI.
 * Fields shown in form:
 *  - User ID (display-only)
 *  - Full Name
 *  - Username
 *  - Password
 *  - Email
 *  - Phone Number
 *  - Role (Admin / Librarian / User)
 *  - Address (optional)
 *  - Gender (optional)
 *  - Account Status (Active / Inactive)
 *  - Date Created (display-only)
 *  - Last Login (display-only)
 */
public class ManageUsersPage extends JFrame {
    private JTable table;
    private DefaultTableModel model;

    // Form fields
    JTextField idField = new JTextField();
    JTextField fullNameField = new JTextField();
    JTextField usernameField = new JTextField();
    JPasswordField passwordField = new JPasswordField();
    JTextField emailField = new JTextField();
    JTextField phoneField = new JTextField();
    JComboBox<String> roleBox = new JComboBox<>(new String[]{"Admin", "Librarian", "User"});
    JTextField addressField = new JTextField();
    JComboBox<String> genderBox = new JComboBox<>(new String[]{"", "Male", "Female", "Other"});
    JComboBox<String> statusBox = new JComboBox<>(new String[]{"Active", "Inactive"});
    JTextField dateCreatedField = new JTextField();
    JTextField lastLoginField = new JTextField();

    public ManageUsersPage() {
        setTitle("Manage Users (Admin)");
        setSize(1100, 640);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8,8));

        // Top - controls
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton newBtn = new JButton("New");
        JButton saveBtn = new JButton("Save");
        JButton deleteBtn = new JButton("Delete");
        JButton refreshBtn = new JButton("Refresh");
        JButton toggleStatusBtn = new JButton("Toggle Active/Inactive");
        top.add(newBtn); top.add(saveBtn); top.add(deleteBtn); top.add(refreshBtn); top.add(toggleStatusBtn);
        add(top, BorderLayout.NORTH);

        // Center - table
        model = DBHelper.getAllUsersTableModel();
        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Right - form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("User Details"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridy = 0;
        form.add(new JLabel("User ID:"), c);
        c.gridx = 1; c.weightx = 1.0; idField.setEditable(false); form.add(idField, c);

        c.gridy++; c.gridx = 0; form.add(new JLabel("Full Name:"), c);
        c.gridx = 1; form.add(fullNameField, c);

        c.gridy++; c.gridx = 0; form.add(new JLabel("Username:"), c);
        c.gridx = 1; form.add(usernameField, c);

        c.gridy++; c.gridx = 0; form.add(new JLabel("Password:"), c);
        c.gridx = 1; form.add(passwordField, c);

        c.gridy++; c.gridx = 0; form.add(new JLabel("Email:"), c);
        c.gridx = 1; form.add(emailField, c);

        c.gridy++; c.gridx = 0; form.add(new JLabel("Phone:"), c);
        c.gridx = 1; form.add(phoneField, c);

        c.gridy++; c.gridx = 0; form.add(new JLabel("Role:"), c);
        c.gridx = 1; form.add(roleBox, c);

        c.gridy++; c.gridx = 0; form.add(new JLabel("Address:"), c);
        c.gridx = 1; form.add(addressField, c);

        c.gridy++; c.gridx = 0; form.add(new JLabel("Gender:"), c);
        c.gridx = 1; form.add(genderBox, c);

        c.gridy++; c.gridx = 0; form.add(new JLabel("Status:"), c);
        c.gridx = 1; form.add(statusBox, c);

        c.gridy++; c.gridx = 0; form.add(new JLabel("Date Created:"), c);
        c.gridx = 1; dateCreatedField.setEditable(false); form.add(dateCreatedField, c);

        c.gridy++; c.gridx = 0; form.add(new JLabel("Last Login:"), c);
        c.gridx = 1; lastLoginField.setEditable(false); form.add(lastLoginField, c);

        add(form, BorderLayout.EAST);

        // Table row selection -> populate form
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateFormFromSelectedRow();
        });

        // Button actions
        newBtn.addActionListener(e -> clearForm());

        saveBtn.addActionListener(e -> {
            // validation
            String fullName = fullNameField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String role = (String) roleBox.getSelectedItem();
            String address = addressField.getText().trim();
            String gender = (String) genderBox.getSelectedItem();
            String status = (String) statusBox.getSelectedItem();

            if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || role == null || role.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill required fields: Full Name, Username, Password, Role.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String idText = idField.getText().trim();
            boolean ok;
            if (idText.isEmpty()) {
                // insert
                ok = DBHelper.insertUser(fullName, username, password, email, phone, role, address, gender, status);
            } else {
                // update
                int id = Integer.parseInt(idText);
                ok = DBHelper.updateUser(id, fullName, username, password, email, phone, role, address, gender, status);
            }
            if (ok) {
                JOptionPane.showMessageDialog(this, "Saved successfully.");
                refreshTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Save failed (see console).", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        deleteBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(this, "Select a user to delete."); return; }
            int modelRow = table.convertRowIndexToModel(r);
            int id = (Integer) model.getValueAt(modelRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Delete user ID " + id + " ?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            if (DBHelper.deleteUser(id)) {
                JOptionPane.showMessageDialog(this, "Deleted.");
                refreshTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Delete failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        refreshBtn.addActionListener(e -> refreshTable());

        toggleStatusBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(this, "Select a user."); return; }
            int modelRow = table.convertRowIndexToModel(r);
            int id = (Integer) model.getValueAt(modelRow, 0);
            String current = (String) model.getValueAt(modelRow, 6); // status column
            String newStatus = "Active".equalsIgnoreCase(current) ? "Inactive" : "Active";

            // fetch full user row to update (we only need fields for updateUser signature)
            String fullName = (String) model.getValueAt(modelRow, 1);
            String username = (String) model.getValueAt(modelRow, 2);
            // For password: cannot read from table; require admin to input password in form if changing.
            String password = new String(passwordField.getPassword()).trim();
            if (password.isEmpty()) {
                // prompt for new password or keep same? For safety, we keep existing password by reading from DB
                // (Add helper to fetch password if needed). For now, refuse if password not set in form.
                JOptionPane.showMessageDialog(this, "Enter user's password in the Password field before toggling status (required to update record).", "Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String email = (String) model.getValueAt(modelRow, 3);
            String phone = (String) model.getValueAt(modelRow, 4);
            String role = (String) model.getValueAt(modelRow, 5);
            String address = addressField.getText().trim();
            String gender = (String) genderBox.getSelectedItem();

            boolean ok = DBHelper.updateUser(id, fullName, username, password, email, phone, role, address, gender, newStatus);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Status updated to " + newStatus);
                refreshTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        setVisible(true);
    }

    private void populateFormFromSelectedRow() {
        int r = table.getSelectedRow();
        if (r == -1) return;
        int modelRow = table.convertRowIndexToModel(r);
        idField.setText(String.valueOf(model.getValueAt(modelRow, 0)));
        fullNameField.setText(String.valueOf(model.getValueAt(modelRow, 1)));
        usernameField.setText(String.valueOf(model.getValueAt(modelRow, 2)));
        emailField.setText(String.valueOf(model.getValueAt(modelRow, 3)));
        phoneField.setText(String.valueOf(model.getValueAt(modelRow, 4)));
        roleBox.setSelectedItem(String.valueOf(model.getValueAt(modelRow, 5)));
        statusBox.setSelectedItem(String.valueOf(model.getValueAt(modelRow, 6)));
        dateCreatedField.setText(String.valueOf(model.getValueAt(modelRow, 7)));
        lastLoginField.setText(String.valueOf(model.getValueAt(modelRow, 8)));
        // note: password and address, gender aren't in table — admin must type password in the form when updating
        addressField.setText("");
        genderBox.setSelectedIndex(0);
        passwordField.setText("");
    }

    private void refreshTable() {
        model = DBHelper.getAllUsersTableModel();
        table.setModel(model);
        table.setAutoCreateRowSorter(true);
    }

    private void clearForm() {
        idField.setText("");
        fullNameField.setText("");
        usernameField.setText("");
        passwordField.setText("");
        emailField.setText("");
        phoneField.setText("");
        roleBox.setSelectedIndex(0);
        addressField.setText("");
        genderBox.setSelectedIndex(0);
        statusBox.setSelectedIndex(0);
        dateCreatedField.setText("");
        lastLoginField.setText("");
        table.clearSelection();
    }
}
