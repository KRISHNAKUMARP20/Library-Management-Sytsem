import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ResetPasswordPage extends JDialog {

    private JPasswordField newPass;
    private JPasswordField reNewPass;
    private String username;

    public ResetPasswordPage(String username) {
        super((Frame) null, "Reset Password", true);
        this.username = username;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(460, 260);
        setResizable(false);
        setLocationRelativeTo(null);

        ImageIcon raw = new ImageIcon("00.jpg");
        Image bgImg = raw.getImage().getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
        JLabel background = new JLabel(new ImageIcon(bgImg));
        background.setLayout(new GridBagLayout());
        add(background);

        JPanel p = new JPanel(new GridBagLayout()); p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(8,8,8,8); c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx=0; c.gridy=0; c.gridwidth=2;
        JLabel header = new JLabel("Reset Password for: " + username, SwingConstants.CENTER); header.setFont(new Font("Segoe UI", Font.BOLD, 14)); header.setForeground(Color.BLACK); p.add(header, c);

        c.gridy++; c.gridwidth=1; c.gridx=0; p.add(new JLabel("New Password:"), c);
        c.gridx=1; newPass = new JPasswordField(16); p.add(newPass, c);

        c.gridy++; c.gridx=0; p.add(new JLabel("Re-enter Password:"), c);
        c.gridx=1; reNewPass = new JPasswordField(16); p.add(reNewPass, c);

        c.gridy++; c.gridx=0; c.gridwidth=2; JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER,10,0)); row.setOpaque(false);
        JButton resetBtn = new JButton("Reset Password"); JButton cancelBtn = new JButton("Cancel"); row.add(resetBtn); row.add(cancelBtn); p.add(row, c);

        background.add(p);

        resetBtn.addActionListener(e -> onReset());
        cancelBtn.addActionListener(e -> dispose());
    }

    private void onReset() {
        String np = new String(newPass.getPassword()).trim();
        String rnp = new String(reNewPass.getPassword()).trim();
        if (np.isEmpty() || rnp.isEmpty()) { JOptionPane.showMessageDialog(this, "Please fill both password fields.", "Missing", JOptionPane.WARNING_MESSAGE); return; }
        if (!np.equals(rnp)) { JOptionPane.showMessageDialog(this, "Passwords do not match.", "Mismatch", JOptionPane.ERROR_MESSAGE); return; }
        if (!LoginPage.users.containsKey(username)) { JOptionPane.showMessageDialog(this, "User account not found.", "Error", JOptionPane.ERROR_MESSAGE); return; }

        String[] data = LoginPage.users.get(username);
        data[0] = np; LoginPage.users.put(username, data);

        JDialog progress = new JDialog((Frame) null, "Applying...", true);
        progress.setSize(360,110); progress.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new BorderLayout(8,8)); panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JLabel lab = new JLabel("Applying new password...", SwingConstants.CENTER);
        JProgressBar bar = new JProgressBar(0,100); bar.setStringPainted(true); panel.add(lab, BorderLayout.NORTH); panel.add(bar, BorderLayout.CENTER);
        progress.add(panel);

        Timer timer = new Timer(20, null);
        timer.addActionListener(new ActionListener() {
            int value = 0;
            public void actionPerformed(ActionEvent e) {
                value += 4; bar.setValue(value);
                if (value >= 100) {
                    timer.stop();
                    progress.dispose();
                    JOptionPane.showMessageDialog(null, "Password successfully reset for user: " + username, "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                }
            }
        });
        timer.start();
        progress.setVisible(true);
    }
}
