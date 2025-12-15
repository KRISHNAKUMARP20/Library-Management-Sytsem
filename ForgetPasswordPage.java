import javax.swing.*;
import java.awt.*;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Improved Forget Password + OTP demo components.
 * - Uses SecureRandom for OTP
 * - Stores OTP with expiry
 * - Allows resend and checks expiry/attempts
 *
 * NOTE: Replace sendSmsPlaceholder(...) with real SMS send in production.
 */
public class ForgetPasswordPage {

    // -------------------------
    // Minimal LoginPage static maps required by ForgetPasswordPage
    // (In your real project, integrate with your existing LoginPage/user store)
    // -------------------------
    public static class LoginPage {
        // map phone->username (for lookup)
        public static final Map<String, String> phoneToUser = new ConcurrentHashMap<>();
        // map username->OTP (value class holds otp + expiry)
        public static final Map<String, OTPRecord> resetOtps = new ConcurrentHashMap<>();
        // map username->password (demo user store)
        public static final Map<String, String> demoUsers = new ConcurrentHashMap<>();

        static {
            // demo data
            demoUsers.put("alice", "alice123");
            phoneToUser.put("6381112606", "alice");
        }
    }

    // OTP record
    static record OTPRecord(String otp, long expiresAtEpochMs, int attemptsLeft) {
        boolean isExpired() {
            return Instant.now().toEpochMilli() > expiresAtEpochMs;
        }
    }

    // -------------------------
    // ForgetPasswordPage (improved)
    // -------------------------
    public static class ForgetPasswordDialog extends JDialog {
        private final JTextField phoneField = new JTextField(15);
        private final SecureRandom secureRandom = new SecureRandom();
        private final JFrame owner;
        private final int OTP_VALIDITY_MINUTES = 5;
        private final int MAX_VERIFY_ATTEMPTS = 3;
        private final boolean DEMO_SHOW_OTP_IN_CONSOLE = true; // set false in production

        public ForgetPasswordDialog(JFrame owner) {
            super(owner, "Forgot Password (Phone OTP)", true);
            this.owner = owner;
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setSize(460, 220);
            setResizable(false);
            setLocationRelativeTo(owner);

            initUI();
        }

        private void initUI() {
            JPanel p = new JPanel(new GridBagLayout());
            p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(8, 8, 8, 8);
            c.fill = GridBagConstraints.HORIZONTAL;

            // Title
            c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
            JLabel t = new JLabel("<html><center>Enter your phone number<br><small>OTP will be sent to your phone</small></center></html>");
            t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            p.add(t, c);

            // Phone label + field
            c.gridy++; c.gridwidth = 1;
            p.add(new JLabel("Phone Number:"), c);
            c.gridx = 1;
            p.add(phoneField, c);

            // Buttons: Send OTP, Resend, Cancel
            c.gridx = 0; c.gridy++; c.gridwidth = 2;
            JPanel row = new JPanel();
            JButton send = new JButton("Send OTP");
            JButton cancel = new JButton("Cancel");
            row.add(send);
            row.add(cancel);
            p.add(row, c);

            send.addActionListener(e -> sendOtp());
            cancel.addActionListener(e -> dispose());

            add(p);
        }

        private void sendOtp() {
            final String phone = phoneField.getText().trim();
            if (phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter phone number!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // lookup username by phone
            final String username = LoginPage.phoneToUser.get(phone);
            if (username == null) {
                JOptionPane.showMessageDialog(this, "Phone number not registered!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // generate secure 6-digit OTP
            final String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
            final long expiresAt = Instant.now().plusSeconds(OTP_VALIDITY_MINUTES * 60L).toEpochMilli();

            // store OTP record with limited attempts
            LoginPage.resetOtps.put(username, new OTPRecord(otp, expiresAt, MAX_VERIFY_ATTEMPTS));

            // placeholder send (replace with real SMS sending)
            sendSmsPlaceholder(phone, otp);

            if (DEMO_SHOW_OTP_IN_CONSOLE) System.out.println("[DEBUG] OTP for " + username + " -> " + otp);

            // open verification UI (non-blocking)
            SwingUtilities.invokeLater(() -> {
                new VerifyOtpDialog(this, username).setVisible(true);
            });

            // close this dialog (verification dialog opens)
            dispose();
        }

        // replace this method with real provider call
        private void sendSmsPlaceholder(String phone, String otp) {
            // For demo, we do not show OTP in UI. In production you must send SMS here.
            JOptionPane.showMessageDialog(this,
                    "OTP has been sent to " + phone + ".\n(For demo the OTP is printed to console.)",
                    "OTP Sent", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // -------------------------
    // VerifyOtpDialog (improved)
    // -------------------------
    public static class VerifyOtpDialog extends JDialog {
        private final String username;

        public VerifyOtpDialog(Window owner, String username) {
            super(owner, "Verify OTP", ModalityType.APPLICATION_MODAL);
            this.username = username;
            setSize(480, 220);
            setLocationRelativeTo(owner);
            setLayout(new GridBagLayout());
            initUI();
        }

        private void initUI() {
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(8, 8, 8, 8);
            c.fill = GridBagConstraints.HORIZONTAL;

            c.gridx = 0; c.gridy = 0;
            add(new JLabel("Enter OTP sent to your registered phone:"), c);
            JTextField otpField = new JTextField(12);
            c.gridx = 1; add(otpField, c);

            c.gridx = 0; c.gridy = 1;
            add(new JLabel("New password:"), c);
            JPasswordField passField = new JPasswordField(12);
            c.gridx = 1; add(passField, c);

            c.gridx = 0; c.gridy = 2;
            JButton verify = new JButton("Verify & Reset");
            add(verify, c);
            c.gridx = 1;
            JButton cancel = new JButton("Cancel");
            add(cancel, c);

            verify.addActionListener(e -> {
                String enteredOtp = otpField.getText().trim();
                String newPass = new String(passField.getPassword());

                if (enteredOtp.isEmpty() || newPass.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Both fields required.", "Verify", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                OTPRecord rec = LoginPage.resetOtps.get(username);
                if (rec == null) {
                    JOptionPane.showMessageDialog(this, "No OTP found. Request a new OTP.", "Verify", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (rec.isExpired()) {
                    LoginPage.resetOtps.remove(username);
                    JOptionPane.showMessageDialog(this, "OTP expired. Request a new OTP.", "Verify", JOptionPane.ERROR_MESSAGE);
                    dispose();
                    return;
                }

                if (!rec.otp.equals(enteredOtp)) {
                    OTPRecord newRec = new OTPRecord(rec.otp, rec.expiresAtEpochMs, rec.attemptsLeft - 1);
                    LoginPage.resetOtps.put(username, newRec);
                    rec = newRec;
                    if (rec.attemptsLeft <= 0) {
                        LoginPage.resetOtps.remove(username);
                        JOptionPane.showMessageDialog(this, "Maximum attempts exceeded. Request a new OTP.", "Verify", JOptionPane.ERROR_MESSAGE);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Incorrect OTP. Attempts left: " + rec.attemptsLeft, "Verify", JOptionPane.ERROR_MESSAGE);
                    }
                    return;
                }

                // OTP correct → reset password
                LoginPage.resetOtps.remove(username);
                LoginPage.demoUsers.put(username, newPass); // in real app, update DB/user store
                JOptionPane.showMessageDialog(this, "Password reset successful. You may now login.", "Success", JOptionPane.INFORMATION_MESSAGE);

                // Open reset confirmation or login (here we open a small ResetPasswordPage UI)
                SwingUtilities.invokeLater(() -> {
                    ResetPasswordPage.showConfirmation(username);
                });

                dispose();
            });

            cancel.addActionListener(e -> dispose());
        }
    }

    // -------------------------
    // ResetPasswordPage (simple confirmation)
    // -------------------------
    public static class ResetPasswordPage {
        public static void showConfirmation(String username) {
            JFrame f = new JFrame("Password Reset");
            f.setSize(420, 180);
            f.setLocationRelativeTo(null);
            f.setLayout(new BorderLayout());
            JLabel l = new JLabel("<html><center>Password for <b>" + username + "</b> was updated.<br/>Please login with the new password.</center></html>", SwingConstants.CENTER);
            l.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
            f.add(l, BorderLayout.CENTER);
            JButton ok = new JButton("OK");
            ok.addActionListener(e -> f.dispose());
            JPanel p = new JPanel(); p.add(ok);
            f.add(p, BorderLayout.SOUTH);
            f.setVisible(true);
        }
    }

    // -------------------------
    // Simple demo main to run ForgetPassword dialog standalone
    // -------------------------
    public static void main(String[] args) {
        // ensure demo user exists:
        LoginPage.demoUsers.putIfAbsent("alice", "alice123");
        LoginPage.phoneToUser.putIfAbsent("6381112606", "alice");

        SwingUtilities.invokeLater(() -> {
            JFrame dummy = new JFrame();
            dummy.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            dummy.setSize(200,100);
            dummy.setLocationRelativeTo(null);
            dummy.setVisible(false);

            ForgetPasswordDialog dlg = new ForgetPasswordDialog(dummy);
            dlg.setVisible(true);
        });
    }
}
