package screens;

import javax.swing.*;
import java.awt.*;
import dao.UserDAO;

public class LoginScreen extends JFrame {

    public LoginScreen() {
        setTitle("MyRupee - Login");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new GridBagLayout());

        // ================= CARD =================
        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(380, 320));
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // TITLE
        JLabel title = new JLabel("Login");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // USERNAME
        JTextField userField = new JTextField();
        userField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        userField.setBorder(BorderFactory.createTitledBorder("Username"));

        // PIN
        JPasswordField pinField = new JPasswordField();
        pinField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        pinField.setBorder(BorderFactory.createTitledBorder("PIN"));

        // BUTTONS
        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");

        loginBtn.setFocusPainted(false);
        registerBtn.setFocusPainted(false);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnPanel.add(loginBtn);
        btnPanel.add(registerBtn);

        // ================= LOGIN ACTION =================
        loginBtn.addActionListener(e -> {

            String username = userField.getText().trim();
            String pin = new String(pinField.getPassword()).trim();

            if (username.isEmpty() || pin.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter username and PIN");
                return;
            }

            int userId = UserDAO.login(username, pin);

            if (userId != -1) {
                new Dashboard(username, userId);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Credentials");
            }
        });

        // ================= REGISTER ACTION =================
        registerBtn.addActionListener(e -> {

            String username = userField.getText().trim();
            String pin = new String(pinField.getPassword()).trim();

            if (username.isEmpty() || pin.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fill all fields");
                return;
            }

            if (UserDAO.register(username, pin)) {
                JOptionPane.showMessageDialog(this, "User Registered Successfully");
            } else {
                JOptionPane.showMessageDialog(this, "Username already exists");
            }
        });

        // ================= BUILD UI =================
        card.add(title);
        card.add(Box.createVerticalStrut(20));
        card.add(userField);
        card.add(Box.createVerticalStrut(15));
        card.add(pinField);
        card.add(Box.createVerticalStrut(20));
        card.add(btnPanel);

        add(card);

        setVisible(true);
    }
}