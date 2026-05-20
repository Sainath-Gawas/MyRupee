package screens;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import db.DBConnection;
import dao.IncomeDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class IncomeScreen extends JFrame {

    private int userId;
    private Dashboard dashboard;
    private DefaultTableModel tableModel;
    private JTable incomeTable;
    private int currentEditId = -1;

    public IncomeScreen(int userId, Dashboard dashboard) {
        this.userId = userId;
        this.dashboard = dashboard;

        setTitle("Income");
        setSize(900, 600);
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

        JLabel formTitle = new JLabel("Add Income");
        formTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel amountLabel = new JLabel("Amount");
        JTextField amountField = new JTextField();
        amountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JLabel dateLabel = new JLabel("Date (YYYY-MM-DD)");
        JTextField dateField = new JTextField(java.time.LocalDate.now().toString());
        dateField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JLabel noteLabel = new JLabel("Note (e.g., Prize Money)");
        JTextField noteField = new JTextField();
        noteField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JButton addBtn = new JButton("Add Income");
        addBtn.setBackground(new Color(41, 100, 232));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        formPanel.add(formTitle);
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
        formPanel.add(noteField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(addBtn);

        // --- RIGHT PANEL (TABLE) ---
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel tableTitle = new JLabel("All Income");
        tableTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        tableTitle.setBorder(new EmptyBorder(0, 0, 15, 0));

        String[] columnNames = { "ID", "Date", "Amount", "Note" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        incomeTable = new JTable(tableModel);
        incomeTable.setRowHeight(35);
        incomeTable.setShowGrid(true);
        incomeTable.setGridColor(new Color(240, 240, 240));
        incomeTable.getTableHeader().setBackground(new Color(248, 250, 252));
        incomeTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        incomeTable.getColumnModel().getColumn(0).setMinWidth(0);
        incomeTable.getColumnModel().getColumn(0).setMaxWidth(0);
        incomeTable.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane tableScroll = new JScrollPane(incomeTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(Color.WHITE);
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        actionPanel.add(editBtn);
        actionPanel.add(deleteBtn);

        tablePanel.add(tableTitle, BorderLayout.NORTH);
        tablePanel.add(tableScroll, BorderLayout.CENTER);
        tablePanel.add(actionPanel, BorderLayout.SOUTH);

        mainContainer.add(formPanel);
        mainContainer.add(tablePanel);

        add(mainContainer, BorderLayout.CENTER);

        // Load data on startup
        refreshTableData();

        // --- ACTION LISTENERS ---

        addBtn.addActionListener(e -> {
            String amountText = amountField.getText().trim();
            String date = dateField.getText().trim();
            String note = noteField.getText().trim();

            if (amountText.isEmpty() || date.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Amount and Date are required");
                return;
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

                // ADD MODE
                try (Connection conn = DBConnection.getConnection()) {
                    String sql = "INSERT INTO income(user_id, amount, date, note) VALUES (?, ?, ?, ?)";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, userId);
                    ps.setDouble(2, amount);
                    ps.setString(3, date);
                    ps.setString(4, note);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Income Added!");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error adding income");
                }
            } else {
                // UPDATE MODE
                IncomeDAO.updateIncome(currentEditId, amount, date, note);
                JOptionPane.showMessageDialog(this, "Income Updated!");

                currentEditId = -1;
                addBtn.setText("Add Income");
                formTitle.setText("Add Income");
            }

            refreshTableData();
            if (dashboard != null)
                dashboard.loadDashboardData();

            amountField.setText("");
            noteField.setText("");
            dateField.setText(java.time.LocalDate.now().toString());
        });

        editBtn.addActionListener(e -> {
            int selectedRow = incomeTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an income record to edit.");
                return;
            }

            currentEditId = (int) tableModel.getValueAt(selectedRow, 0);
            String date = (String) tableModel.getValueAt(selectedRow, 1);
            String amount = (String) tableModel.getValueAt(selectedRow, 2);
            String note = (String) tableModel.getValueAt(selectedRow, 3);

            dateField.setText(date);
            amountField.setText(amount);
            noteField.setText(note != null ? note : "");

            formTitle.setText("Edit Income");
            addBtn.setText("Update Income");
        });

        deleteBtn.addActionListener(e -> {
            int selectedRow = incomeTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an income record to delete.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this income record?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                int idToDelete = (int) tableModel.getValueAt(selectedRow, 0);
                IncomeDAO.deleteIncome(idToDelete);

                if (currentEditId == idToDelete) {
                    currentEditId = -1;
                    addBtn.setText("Add Income");
                    formTitle.setText("Add Income");
                    amountField.setText("");
                    noteField.setText("");
                }

                refreshTableData();
                if (dashboard != null)
                    dashboard.loadDashboardData();
            }
        });

        setVisible(true);
    }

    private void refreshTableData() {
        tableModel.setRowCount(0);
        Object[][] data = IncomeDAO.getIncomeForTable(userId);
        for (Object[] row : data) {
            tableModel.addRow(row);
        }
    }
}