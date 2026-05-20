package screens;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URI;
import java.net.URLEncoder;
import dao.DebtDAO;
import java.sql.Connection;

public class DebtScreen extends JFrame {

    private int userId;
    private Dashboard dashboard;
    private DefaultTableModel tableModel;
    private JTable debtTable;

    // State variable to track editing
    private int currentEditId = -1;

    public DebtScreen(int userId, Dashboard dashboard) {
        this.userId = userId;
        this.dashboard = dashboard;

        setTitle("Debt Management");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245, 247, 250));
        setLayout(new BorderLayout());

        JPanel mainContainer = new JPanel(new GridLayout(1, 2, 20, 0));
        mainContainer.setBackground(new Color(245, 247, 250));
        mainContainer.setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- LEFT PANEL (FORM) ---
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel formTitle = new JLabel("Add Debt");
        formTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel personLabel = new JLabel("Person Name");
        JTextField personField = new JTextField();
        personField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JLabel amountLabel = new JLabel("Amount");
        JTextField amountField = new JTextField();
        amountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JLabel dateLabel = new JLabel("Date (YYYY-MM-DD)");
        JTextField dateField = new JTextField(java.time.LocalDate.now().toString());
        dateField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JLabel noteLabel = new JLabel("Note");
        JTextArea noteArea = new JTextArea(3, 20);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        JScrollPane noteScroll = new JScrollPane(noteArea);
        noteScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel emailLabel = new JLabel("Email");
        JTextField emailField = new JTextField();
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JButton addBtn = new JButton("Add Debt");
        addBtn.setBackground(new Color(41, 100, 232));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        formPanel.add(formTitle);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(personLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(personField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(amountLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(amountField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(dateLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(dateField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(noteLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(noteScroll);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(emailLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(emailField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(addBtn);

        // --- RIGHT PANEL (TABLE) ---
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel tableTitle = new JLabel("All Debts");
        tableTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        tableTitle.setBorder(new EmptyBorder(0, 0, 15, 0));

        // Note: 7 Columns. Indices 0, 5, and 6 will be hidden.
        String[] columnNames = { "ID", "Person Name", "Amount", "Date", "Status", "Note", "Email" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        debtTable = new JTable(tableModel);
        debtTable.setRowHeight(35);
        debtTable.setShowGrid(true);
        debtTable.setGridColor(new Color(240, 240, 240));
        debtTable.getTableHeader().setBackground(new Color(248, 250, 252));
        debtTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        // Hide ID, Note, and Email columns from the user
        hideColumn(0);
        hideColumn(5);
        hideColumn(6);

        JScrollPane tableScroll = new JScrollPane(debtTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionPanel.setBackground(Color.WHITE);

        JButton reminderBtn = createSecondaryButton("Send Reminder");
        JButton updateBtn = createSecondaryButton("Edit Info");
        JButton markPaidBtn = createSecondaryButton("Mark as Paid");
        JButton deleteBtn = createSecondaryButton("Delete");

        actionPanel.add(reminderBtn);
        actionPanel.add(updateBtn);
        actionPanel.add(markPaidBtn);
        actionPanel.add(deleteBtn);

        tablePanel.add(tableTitle, BorderLayout.NORTH);
        tablePanel.add(tableScroll, BorderLayout.CENTER);
        tablePanel.add(actionPanel, BorderLayout.SOUTH);

        mainContainer.add(formPanel);
        mainContainer.add(tablePanel);

        add(mainContainer, BorderLayout.CENTER);

        // Load Data
        refreshTableData();

        // --- ACTION LISTENERS ---

        addBtn.addActionListener(e -> {
            String person = personField.getText().trim();
            String amountText = amountField.getText().trim();
            String date = dateField.getText().trim();
            String note = noteArea.getText().trim();
            String email = emailField.getText().trim();

            if (person.isEmpty() || amountText.isEmpty() || date.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name, Amount, and Date are required.");
                return;
            }

            // Email Validation Checker
            if (!email.isEmpty()) {
                String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
                if (!email.matches(emailRegex)) {
                    JOptionPane.showMessageDialog(this,
                            "Please enter a valid email address.",
                            "Invalid Email",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            double amount;
            try {
                amount = Double.parseDouble(amountText);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount format.");
                return;
            }

            if (amount <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Amount must be greater than zero.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (currentEditId == -1) {
                DebtDAO.addDebt(userId, person, amount, date, note, email);
                JOptionPane.showMessageDialog(this, "Debt Added Successfully!");
            } else {
                DebtDAO.updateDebt(currentEditId, person, amount, date, note, email);
                JOptionPane.showMessageDialog(this, "Debt Updated Successfully!");
                currentEditId = -1;
                addBtn.setText("Add Debt");
                formTitle.setText("Add Debt");
            }

            refreshTableData();
            if (dashboard != null)
                dashboard.loadDashboardData();

            personField.setText("");
            amountField.setText("");
            noteArea.setText("");
            emailField.setText("");
            dateField.setText(java.time.LocalDate.now().toString());
        });

        updateBtn.addActionListener(e -> {
            int selectedRow = debtTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a debt to edit.");
                return;
            }

            // Extract from all columns (including hidden ones)
            currentEditId = (int) tableModel.getValueAt(selectedRow, 0);
            personField.setText((String) tableModel.getValueAt(selectedRow, 1));
            amountField.setText((String) tableModel.getValueAt(selectedRow, 2));
            dateField.setText((String) tableModel.getValueAt(selectedRow, 3));

            // Status is at index 4, we don't edit it via form

            noteArea.setText((String) tableModel.getValueAt(selectedRow, 5));
            emailField.setText((String) tableModel.getValueAt(selectedRow, 6));

            formTitle.setText("Edit Debt Info");
            addBtn.setText("Update Debt");
        });

        markPaidBtn.addActionListener(e -> {
            int selectedRow = debtTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a debt.");
                return;
            }
            int debtId = (int) tableModel.getValueAt(selectedRow, 0);
            DebtDAO.markDebtAsPaid(debtId);

            refreshTableData();
            if (dashboard != null)
                dashboard.loadDashboardData();
        });

        reminderBtn.addActionListener(e -> {
            int selectedRow = debtTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a debt.");
                return;
            }

            String person = (String) tableModel.getValueAt(selectedRow, 1);
            String amount = (String) tableModel.getValueAt(selectedRow, 2);
            String email = (String) tableModel.getValueAt(selectedRow, 6);

            if (email == null || email.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "No email saved for " + person + "!");
            } else {
                try {
                    // Check if the OS supports opening a web browser
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {

                        String subject = "Reminder: Pending Debt of Rs. " + amount;
                        String body = "Hi " + person + ",\n\n"
                                + "Just a friendly reminder that a pending amount of Rs. " + amount + " is due.\n\n"
                                + "Please let me know when you can settle this.\n\n"
                                + "Thanks!";

                        // Encode the text to handle spaces and newlines properly in the web URL
                        String encodedSubject = URLEncoder.encode(subject, "UTF-8").replace("+", "%20");
                        String encodedBody = URLEncoder.encode(body, "UTF-8").replace("+", "%20");

                        // Construct the specific Google Mail compose URL
                        String gmailUrl = "https://mail.google.com/mail/?view=cm&fs=1&to=" + email
                                + "&su=" + encodedSubject
                                + "&body=" + encodedBody;

                        // Open the URL in the default web browser (Chrome, Edge, Safari, etc.)
                        URI webURI = new URI(gmailUrl);
                        Desktop.getDesktop().browse(webURI);

                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Web browser is not supported on your system.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Failed to open browser: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        deleteBtn.addActionListener(e -> {
            int selectedRow = debtTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a debt to delete.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Delete this record?", "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                int idToDelete = (int) tableModel.getValueAt(selectedRow, 0);
                DebtDAO.deleteDebt(idToDelete);

                if (currentEditId == idToDelete) {
                    currentEditId = -1;
                    addBtn.setText("Add Debt");
                    formTitle.setText("Add Debt");
                    personField.setText("");
                    amountField.setText("");
                }

                refreshTableData();
                if (dashboard != null)
                    dashboard.loadDashboardData();
            }
        });

        setVisible(true);
    }

    private void hideColumn(int index) {
        debtTable.getColumnModel().getColumn(index).setMinWidth(0);
        debtTable.getColumnModel().getColumn(index).setMaxWidth(0);
        debtTable.getColumnModel().getColumn(index).setWidth(0);
    }

    private void refreshTableData() {
        tableModel.setRowCount(0);
        Object[][] data = DebtDAO.getDebtsForTable(userId);
        for (Object[] row : data) {
            tableModel.addRow(row);
        }
    }

    private JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(226, 232, 240));
        btn.setForeground(new Color(15, 23, 42));
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setBorder(new EmptyBorder(8, 15, 8, 15));
        return btn;
    }
}