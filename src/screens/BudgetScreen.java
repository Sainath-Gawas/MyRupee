package screens;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.YearMonth;
import dao.BudgetDAO;
import dao.ExpenseDAO;

public class BudgetScreen extends JFrame {

    private int userId;
    private Dashboard dashboard;
    private String currentMonth;

    // UI components that need to be updated dynamically
    private JLabel totalBudgetLbl;
    private JLabel spentLbl;
    private JLabel remainingLbl;
    private JLabel percentageLbl;
    private JProgressBar progressBar;
    private JPanel statusPanel;
    private JLabel statusTextLbl;
    private JLabel alertTextLbl;

    public BudgetScreen(int userId, Dashboard dashboard) {
        this.userId = userId;
        this.dashboard = dashboard;
        this.currentMonth = YearMonth.now().toString(); // Gets "YYYY-MM"

        setTitle("Budget Management");
        setSize(800, 550);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245, 247, 250));
        setLayout(new BorderLayout());

        // FIX: Changed mainContainer to BorderLayout to prevent awkward vertical
        // spacing
        JPanel mainContainer = new JPanel(new BorderLayout(0, 20));
        mainContainer.setBackground(new Color(245, 247, 250));
        mainContainer.setBorder(new EmptyBorder(20, 30, 20, 30));

        // --- TOP PANEL: Set Budget Form ---
        JPanel topCard = createCardPanel();
        topCard.setLayout(new BorderLayout(0, 15));

        JLabel topTitle = new JLabel("Set Monthly Budget");
        topTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        topCard.add(topTitle, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        formPanel.setBackground(Color.WHITE);

        JTextField amountField = new JTextField();
        amountField.setPreferredSize(new Dimension(300, 45));
        amountField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        amountField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(5, 15, 5, 15)));

        JButton saveBtn = new JButton("Save Budget");
        saveBtn.setPreferredSize(new Dimension(180, 45));
        saveBtn.setBackground(new Color(41, 100, 232));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        saveBtn.setFocusPainted(false);
        saveBtn.setBorderPainted(false);

        formPanel.add(amountField);
        formPanel.add(Box.createRigidArea(new Dimension(20, 0))); // Gap between field and button
        formPanel.add(saveBtn);

        topCard.add(formPanel, BorderLayout.CENTER);

        // --- BOTTOM PANEL: Budget Overview ---
        JPanel bottomCard = createCardPanel();
        bottomCard.setLayout(new BorderLayout(0, 20));

        JLabel bottomTitle = new JLabel("Budget Overview");
        bottomTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        bottomCard.add(bottomTitle, BorderLayout.NORTH);

        // 1. Stats Row
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        totalBudgetLbl = new JLabel("₹0");
        spentLbl = new JLabel("₹0");
        remainingLbl = new JLabel("₹0");

        statsPanel.add(createStatBlock("Total Budget", totalBudgetLbl));
        statsPanel.add(createStatBlock("Amount Spent", spentLbl));
        statsPanel.add(createStatBlock("Remaining Budget", remainingLbl));

        // 2. Progress Bar Area
        JPanel progressArea = new JPanel(new BorderLayout(0, 8));
        progressArea.setBackground(Color.WHITE);
        progressArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        progressArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        percentageLbl = new JLabel("0% Used");
        percentageLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        percentageLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 12));
        progressBar.setBorderPainted(false);
        progressBar.setBackground(new Color(226, 232, 240));

        progressArea.add(percentageLbl, BorderLayout.NORTH);
        progressArea.add(progressBar, BorderLayout.CENTER);

        // 3. Status Box
        statusPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        statusPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        statusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        statusPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        statusTextLbl = new JLabel("Budget Status: Not Set");
        statusTextLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        alertTextLbl = new JLabel("Alerts: Set a budget to track spending");
        alertTextLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));

        statusPanel.add(statusTextLbl);
        statusPanel.add(alertTextLbl);

        // FIX: Use a vertical BoxLayout to perfectly stack the 3 areas with exact gaps
        JPanel stackPanel = new JPanel();
        stackPanel.setLayout(new BoxLayout(stackPanel, BoxLayout.Y_AXIS));
        stackPanel.setBackground(Color.WHITE);

        stackPanel.add(statsPanel);
        stackPanel.add(Box.createRigidArea(new Dimension(0, 25))); // Exact spacing
        stackPanel.add(progressArea);
        stackPanel.add(Box.createRigidArea(new Dimension(0, 25))); // Exact spacing
        stackPanel.add(statusPanel);

        // FIX: Wrap the stack in a top-anchored panel so it doesn't stretch to the
        // bottom of the screen!
        JPanel topAnchorPanel = new JPanel(new BorderLayout());
        topAnchorPanel.setBackground(Color.WHITE);
        topAnchorPanel.add(stackPanel, BorderLayout.NORTH);

        bottomCard.add(topAnchorPanel, BorderLayout.CENTER);

        // Assemble Main Container
        mainContainer.add(topCard, BorderLayout.NORTH);
        mainContainer.add(bottomCard, BorderLayout.CENTER);

        add(mainContainer, BorderLayout.CENTER);

        // --- ACTIONS ---
        // --- ACTIONS ---
        saveBtn.addActionListener(e -> {
            String input = amountField.getText().trim();
            if (input.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter an amount.");
                return;
            }
            try {
                double amount = Double.parseDouble(input.replace("₹", "").replace(",", ""));

                // --- NEW: Negative & Zero Value Check ---
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "Budget amount must be greater than zero.",
                            "Invalid Input",
                            JOptionPane.ERROR_MESSAGE);
                    return; // Stop the save process
                }
                // ----------------------------------------

                BudgetDAO.saveBudget(userId, amount, currentMonth);
                refreshOverview();
                if (dashboard != null) {
                    dashboard.loadDashboardData();
                }
                amountField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount format.");
            }
        });

        // Load initial data
        refreshOverview();

        setVisible(true);
    }

    private void refreshOverview() {
        double totalBudget = BudgetDAO.getMonthlyBudget(userId, currentMonth);
        double amountSpent = ExpenseDAO.getCurrentMonthExpense(userId, currentMonth);
        double remaining = totalBudget - amountSpent;

        totalBudgetLbl.setText("₹" + String.format("%.0f", totalBudget));
        spentLbl.setText("₹" + String.format("%.0f", amountSpent));
        remainingLbl.setText("₹" + String.format("%.0f", remaining));

        if (totalBudget > 0) {
            int percentage = (int) ((amountSpent / totalBudget) * 100);
            percentageLbl.setText(percentage + "% Used");
            progressBar.setValue(Math.min(percentage, 100)); // Cap at 100 for the bar

            if (percentage >= 100) {
                // Exceeded
                progressBar.setForeground(new Color(220, 38, 38)); // Red
                statusPanel.setBackground(new Color(254, 226, 226));
                statusTextLbl.setForeground(new Color(220, 38, 38));
                statusTextLbl.setText("Budget Status: Exceeded");
                alertTextLbl.setText("Alerts: You have crossed your monthly limit!");
            } else if (percentage >= 80) {
                // Warning
                progressBar.setForeground(new Color(202, 138, 4)); // Yellow/Orange
                statusPanel.setBackground(new Color(254, 249, 195));
                statusTextLbl.setForeground(new Color(202, 138, 4));
                statusTextLbl.setText("Budget Status: Nearing Limit");
                alertTextLbl.setText("Alerts: You have used " + percentage + "% of your budget.");
            } else {
                // On Track
                progressBar.setForeground(new Color(41, 100, 232)); // Standard Blue
                // FIX: Removed the neon pink typo! Now a soft, light success green
                statusPanel.setBackground(new Color(240, 253, 244));
                statusTextLbl.setForeground(new Color(22, 163, 74)); // Green text
                statusTextLbl.setText("Budget Status: On Track");
                alertTextLbl.setText("Alerts: Your spending is well within the limit.");
            }
        } else {
            // No budget set
            percentageLbl.setText("0% Used");
            progressBar.setValue(0);
            statusPanel.setBackground(new Color(248, 250, 252));
            statusTextLbl.setForeground(new Color(100, 116, 139));
            statusTextLbl.setText("Budget Status: Not Set");
            alertTextLbl.setText("Alerts: Set a budget above to start tracking.");
        }
    }

    private JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(25, 25, 25, 25)));
        return card;
    }

    private JPanel createStatBlock(String title, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(Color.WHITE);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        titleLbl.setForeground(new Color(100, 116, 139));

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        valueLabel.setForeground(new Color(15, 23, 42)); // Dark Slate

        panel.add(titleLbl, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        return panel;
    }
}