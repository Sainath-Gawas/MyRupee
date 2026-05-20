package screens;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import db.DBConnection;
import dao.ExpenseDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ExpenseScreen extends JFrame {

    private int userId;
    private Dashboard dashboard;
    private DefaultTableModel tableModel;
    private JTable expenseTable;

    // State variable to track if we are adding new or editing existing
    private int currentEditId = -1;

    public ExpenseScreen(int userId, Dashboard dashboard) {
        this.userId = userId;
        this.dashboard = dashboard;

        setTitle("Expenses");
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

        JLabel formTitle = new JLabel("Add Expense");
        formTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel amountLabel = new JLabel("Amount");
        JTextField amountField = new JTextField();
        amountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JLabel categoryLabel = new JLabel("Category");
        String[] categories = { "Food", "Travel", "Study", "Shopping", "Other" };
        JComboBox<String> categoryCombo = new JComboBox<>(categories);
        categoryCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        categoryCombo.setBackground(Color.WHITE);
        // 🔥 This allows the user to type their own custom category!
        categoryCombo.setEditable(true);

        JLabel dateLabel = new JLabel("Date (YYYY-MM-DD)");
        JTextField dateField = new JTextField(java.time.LocalDate.now().toString());
        dateField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JLabel noteLabel = new JLabel("Note");
        JTextArea noteArea = new JTextArea(3, 20);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        JScrollPane noteScroll = new JScrollPane(noteArea);
        noteScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JButton addBtn = new JButton("Add Expense");
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
        formPanel.add(categoryLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(categoryCombo);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(dateLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(dateField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(noteLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(noteScroll);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(addBtn);

        // --- RIGHT PANEL (TABLE) ---
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel tableTitle = new JLabel("All Expenses");
        tableTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        tableTitle.setBorder(new EmptyBorder(0, 0, 15, 0));

        // Note: Added an "ID" column at index 0 to track the database ID
        String[] columnNames = { "ID", "Date", "Category", "Amount", "Note" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            } // Prevent direct typing in table
        };

        expenseTable = new JTable(tableModel);
        expenseTable.setRowHeight(35);
        expenseTable.setShowGrid(true);
        expenseTable.setGridColor(new Color(240, 240, 240));
        expenseTable.getTableHeader().setBackground(new Color(248, 250, 252));
        expenseTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        // Hide the ID column from the user, but keep it in the model
        expenseTable.getColumnModel().getColumn(0).setMinWidth(0);
        expenseTable.getColumnModel().getColumn(0).setMaxWidth(0);
        expenseTable.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane tableScroll = new JScrollPane(expenseTable);
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
            String category = (String) categoryCombo.getSelectedItem(); // Works for typed or selected items
            String date = dateField.getText().trim();
            String note = noteArea.getText().trim();

            if (amountText.isEmpty() || date.isEmpty() || category == null || category.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Amount, Category, and Date are required");
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
                    String sql = "INSERT INTO expenses(user_id, amount, category, date, note) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, userId);
                    ps.setDouble(2, amount);
                    ps.setString(3, category);
                    ps.setString(4, date);
                    ps.setString(5, note);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Expense Added!");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error adding expense");
                }
            } else {
                // UPDATE MODE
                ExpenseDAO.updateExpense(currentEditId, amount, category, date, note);
                JOptionPane.showMessageDialog(this, "Expense Updated!");

                // Reset form state back to Add
                currentEditId = -1;
                addBtn.setText("Add Expense");
                formTitle.setText("Add Expense");
            }

            // Refresh everything
            refreshTableData();
            if (dashboard != null)
                dashboard.loadDashboardData();

            // Clear fields
            amountField.setText("");
            noteArea.setText("");
            dateField.setText(java.time.LocalDate.now().toString());
        });

        editBtn.addActionListener(e -> {
            int selectedRow = expenseTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an expense to edit.");
                return;
            }

            // Extract data from the selected row
            currentEditId = (int) tableModel.getValueAt(selectedRow, 0); // Hidden ID
            String date = (String) tableModel.getValueAt(selectedRow, 1);
            String category = (String) tableModel.getValueAt(selectedRow, 2);
            String amount = (String) tableModel.getValueAt(selectedRow, 3);
            String note = (String) tableModel.getValueAt(selectedRow, 4);

            // Populate form
            dateField.setText(date);
            categoryCombo.setSelectedItem(category);
            amountField.setText(amount);
            noteArea.setText(note);

            // Change UI to edit mode
            formTitle.setText("Edit Expense");
            addBtn.setText("Update Expense");
        });

        deleteBtn.addActionListener(e -> {
            int selectedRow = expenseTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an expense to delete.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this expense?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                int idToDelete = (int) tableModel.getValueAt(selectedRow, 0); // Hidden ID
                ExpenseDAO.deleteExpense(idToDelete);

                // If the user deleted the item they were currently editing, reset the form
                if (currentEditId == idToDelete) {
                    currentEditId = -1;
                    addBtn.setText("Add Expense");
                    formTitle.setText("Add Expense");
                    amountField.setText("");
                    noteArea.setText("");
                }

                refreshTableData();
                if (dashboard != null)
                    dashboard.loadDashboardData();
            }
        });

        setVisible(true);
    }

    private void refreshTableData() {
        tableModel.setRowCount(0); // Clear existing rows
        Object[][] data = ExpenseDAO.getExpensesForTable(userId);
        for (Object[] row : data) {
            tableModel.addRow(row);
        }
    }
}