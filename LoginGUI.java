import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginGUI {
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton;

    public LoginGUI() {
        frame = new JFrame("FinTrack - Login/Register");
        frame.setSize(400, 250);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2, 10, 10));
        panel.setBackground(new Color(100, 50, 150));

        // Username
        JLabel UsernameLabel;
        JLabel PasswordLabel;
        panel.add(UsernameLabel = new JLabel("Username:", SwingConstants.RIGHT));
        usernameField = new JTextField();
        usernameField.setForeground(Color.BLACK);
        usernameField.setBackground(Color.WHITE);
        panel.add(usernameField);
        UsernameLabel.setForeground(Color.WHITE);

        // Password
        panel.add(PasswordLabel = new JLabel("Password:", SwingConstants.RIGHT));
        passwordField = new JPasswordField();
        passwordField.setForeground(Color.BLACK);
        passwordField.setBackground(Color.WHITE);
        panel.add(passwordField);
        PasswordLabel.setForeground(Color.WHITE);

        // Buttons
        loginButton = new JButton("Login");
        styleButton(loginButton);
        panel.add(loginButton);

        registerButton = new JButton("Register");
        styleButton(registerButton);
        panel.add(registerButton);

        frame.add(panel);
        frame.setVisible(true);

        // Login Button Functionality
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword()).trim();

                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Fields cannot be empty!", "Login Failed", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (DatabaseHelper.validateUser(username, password)) {
                    frame.dispose(); // Close login screen
                    SwingUtilities.invokeLater(() -> new FinTrackGUI(username)); // Open main app
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid username or password!", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Register Button Functionality
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword()).trim();

                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Fields cannot be empty!", "Registration Failed", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (DatabaseHelper.registerUser(username, password)) {
                    JOptionPane.showMessageDialog(frame, "Registration successful! You can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(frame, "Username already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    // Style buttons for consistency
    private void styleButton(JButton button) {
        button.setBackground(new Color(55, 13, 94));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
    }

    public static void main(String[] args) {
        new LoginGUI();
    }
}



