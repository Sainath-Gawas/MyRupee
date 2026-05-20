package screens;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.YearMonth;
import dao.ExpenseDAO;
import dao.IncomeDAO;
import dao.DebtDAO;
import dao.BudgetDAO;

public class Dashboard extends JFrame {

    private int userId;
    private String username;

    private JPanel cardPanel;
    private JPanel insightsPanel;

    public Dashboard(String username, int userId) {
        this.username = username;
        this.userId = userId;

        setTitle("MyRupee - Dashboard");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 247, 250));

        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(new Color(248, 250, 252));
        sidebar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 20));
        sidebar.setBorder(new EmptyBorder(20, 10, 20, 10));

        JLabel logo = new JLabel("💸 MyRupee");
        logo.setFont(new Font("SansSerif", Font.BOLD, 20));
        logo.setForeground(new Color(41, 100, 232));
        logo.setBorder(new EmptyBorder(0, 10, 30, 0));
        sidebar.add(logo);

        JButton dashboardBtn = createSidebarButton("📊 Dashboard", true);

        JButton addIncomeBtn = createSidebarButton("💰 Add Income", false);
        addIncomeBtn.addActionListener(e -> new IncomeScreen(userId, this));

        JButton expenseBtn = createSidebarButton("📉 Add Expense", false);
        expenseBtn.addActionListener(e -> new ExpenseScreen(userId, this));

        JButton addDebtBtn = createSidebarButton("🤝 Add Debt", false);
        addDebtBtn.addActionListener(e -> new DebtScreen(userId, this));

        JButton budgetBtn = createSidebarButton("💼 Budget", false);
        budgetBtn.addActionListener(e -> new BudgetScreen(userId, this));

        JButton insightsBtn = createSidebarButton("🎯 Insights", false);
        insightsBtn.addActionListener(e -> new InsightsScreen(userId, this));

        // --- LOGOUT BUTTON ---
        JButton logoutBtn = createSidebarButton("🚪 Logout", false);
        logoutBtn.setForeground(new Color(220, 38, 38));
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to logout?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                this.dispose();
                new LoginScreen();
            }
        });

        sidebar.add(dashboardBtn);
        sidebar.add(addIncomeBtn);
        sidebar.add(expenseBtn);
        sidebar.add(addDebtBtn);
        sidebar.add(budgetBtn);
        sidebar.add(insightsBtn);

        sidebar.add(Box.createRigidArea(new Dimension(180, 20)));
        sidebar.add(logoutBtn);

        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(new Color(245, 247, 250));
        mainContent.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel headerLabel = new JLabel("Dashboard");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        headerLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainContent.add(headerLabel, BorderLayout.NORTH);

        JPanel centerLayout = new JPanel(new BorderLayout(0, 20));
        centerLayout.setBackground(new Color(245, 247, 250));

        cardPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        cardPanel.setBackground(new Color(245, 247, 250));

        insightsPanel = new JPanel(new BorderLayout());
        insightsPanel.setBackground(Color.WHITE);
        insightsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(20, 20, 20, 20)));

        centerLayout.add(cardPanel, BorderLayout.NORTH);
        centerLayout.add(insightsPanel, BorderLayout.CENTER);

        mainContent.add(centerLayout, BorderLayout.CENTER);

        add(sidebar, BorderLayout.WEST);
        add(mainContent, BorderLayout.CENTER);

        loadDashboardData();

        setVisible(true);
    }

    public void loadDashboardData() {
        cardPanel.removeAll();
        insightsPanel.removeAll();

        double income = IncomeDAO.getTotalIncome(userId);
        double expense = ExpenseDAO.getTotalExpense(userId);
        double debt = DebtDAO.getTotalPendingDebt(userId);

        String currentMonth = YearMonth.now().toString();
        double totalBudget = BudgetDAO.getMonthlyBudget(userId, currentMonth);
        double amountSpent = ExpenseDAO.getCurrentMonthExpense(userId, currentMonth);

        int budgetPercentage = 0;
        if (totalBudget > 0) {
            budgetPercentage = (int) ((amountSpent / totalBudget) * 100);
        }

        cardPanel.add(createStandardCard("Total Expenses", "₹" + Math.round(expense), "Current Month",
                new Color(41, 100, 232)));

        String budgetSubtitle = totalBudget > 0 ? "₹" + Math.round(amountSpent) + " / ₹" + Math.round(totalBudget)
                : "No budget set";
        cardPanel.add(createBudgetCard("Budget Used", budgetPercentage + "%", budgetSubtitle, budgetPercentage));

        cardPanel.add(createStandardCard("Total Pending Debt", "₹" + Math.round(debt), "Owed to Others", Color.BLACK));

        cardPanel.add(createStandardCard("Total Income", "₹" + Math.round(income), "Earned this month",
                new Color(34, 197, 94)));

        JLabel insightTitle = new JLabel("📈 Weekly Expense Trend");
        insightTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        insightTitle.setBorder(new EmptyBorder(0, 0, 15, 0));

        double[] weeklyData = ExpenseDAO.getWeeklyExpenseTrend(userId);

        JPanel lineGraph = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int padding = 50;
                int width = getWidth();
                int height = getHeight();

                // Draw Background Grid Lines
                g2d.setColor(new Color(240, 240, 245));
                for (int i = 0; i <= 4; i++) {
                    int yLine = padding + i * ((height - 2 * padding) / 4);
                    g2d.drawLine(padding, yLine, width - padding, yLine);
                }

                // Draw Axes
                g2d.setColor(new Color(200, 205, 215));
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawLine(padding, height - padding, width - padding, height - padding);
                g2d.drawLine(padding, padding, padding, height - padding);

                // Find max value
                double max = 0;
                for (double v : weeklyData) {
                    if (v > max)
                        max = v;
                }
                if (max == 0)
                    max = 100;

                // Draw Y-Axis Labels (Values)
                g2d.setColor(new Color(100, 116, 139));
                g2d.setFont(new Font("SansSerif", Font.PLAIN, 11));
                FontMetrics fm = g2d.getFontMetrics();

                for (int i = 0; i <= 4; i++) {
                    int yLine = padding + (4 - i) * ((height - 2 * padding) / 4);
                    String yLabel = String.valueOf(Math.round((max / 4.0) * i));
                    int labelWidth = fm.stringWidth(yLabel);

                    g2d.drawString(yLabel, padding - labelWidth - 8, yLine + 4);
                }

                int xStep = (width - 2 * padding) / (weeklyData.length > 1 ? weeklyData.length - 1 : 1);

                // --- NEW: Draw X-Axis Labels (Dynamic Days of the Week) ---
                for (int i = 0; i < weeklyData.length; i++) {
                    int x1 = padding + i * xStep;

                    // Dynamically calculate the day based on today's date
                    java.time.LocalDate date = java.time.LocalDate.now().minusDays(weeklyData.length - 1 - i);
                    String fullDayName = date.getDayOfWeek().name();

                    // Format from "MONDAY" to "Mon"
                    String xLabel = fullDayName.substring(0, 1) + fullDayName.substring(1, 3).toLowerCase();

                    int xLabelWidth = fm.stringWidth(xLabel);

                    // Draw centered below the X-axis
                    g2d.drawString(xLabel, x1 - (xLabelWidth / 2), height - padding + 18);
                }

                // Draw Line & Dots
                g2d.setColor(new Color(41, 100, 232));
                g2d.setStroke(new BasicStroke(3f));

                for (int i = 0; i < weeklyData.length - 1; i++) {
                    int x1 = padding + i * xStep;
                    int y1 = height - padding - (int) ((weeklyData[i] / max) * (height - 2 * padding));
                    int x2 = padding + (i + 1) * xStep;
                    int y2 = height - padding - (int) ((weeklyData[i + 1] / max) * (height - 2 * padding));

                    g2d.drawLine(x1, y1, x2, y2);

                    g2d.fillOval(x1 - 4, y1 - 4, 8, 8);
                    if (i == weeklyData.length - 2) {
                        g2d.fillOval(x2 - 4, y2 - 4, 8, 8);
                    }
                }
            }
        };
        lineGraph.setBackground(Color.WHITE);
        lineGraph.setPreferredSize(new Dimension(0, 250));

        insightsPanel.add(insightTitle, BorderLayout.NORTH);
        insightsPanel.add(lineGraph, BorderLayout.CENTER);

        cardPanel.revalidate();
        cardPanel.repaint();
        insightsPanel.revalidate();
        insightsPanel.repaint();
    }

    private JPanel createStandardCard(String title, String value, String subtitle, Color valueColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLabel.setForeground(new Color(100, 116, 139));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        valueLabel.setForeground(valueColor);

        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subLabel.setForeground(new Color(100, 116, 139));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(valueLabel, BorderLayout.CENTER);

        card.add(topPanel, BorderLayout.CENTER);
        card.add(subLabel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createBudgetCard(String title, String percentage, String subtitle, int progressValue) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLabel.setForeground(new Color(100, 116, 139));

        JLabel valueLabel = new JLabel(percentage);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 30));

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(Math.min(progressValue, 100));

        // Dynamic colors for progress bar
        if (progressValue >= 100) {
            progressBar.setForeground(new Color(220, 38, 38));
        } else if (progressValue >= 80) {
            progressBar.setForeground(new Color(202, 138, 4));
        } else {
            progressBar.setForeground(new Color(41, 100, 232));
        }

        progressBar.setBackground(new Color(226, 232, 240));
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new Dimension(100, 8));

        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subLabel.setForeground(new Color(100, 116, 139));
        subLabel.setBorder(new EmptyBorder(5, 0, 0, 0));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(valueLabel, BorderLayout.NORTH);
        centerPanel.add(progressBar, BorderLayout.CENTER);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);
        card.add(subLabel, BorderLayout.SOUTH);

        return card;
    }

    private JButton createSidebarButton(String text, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(180, 45));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBorder(new EmptyBorder(10, 15, 10, 15));

        if (isActive) {
            btn.setBackground(new Color(226, 232, 240));
            btn.setForeground(new Color(41, 100, 232));
        } else {
            btn.setBackground(new Color(248, 250, 252));
            btn.setForeground(new Color(100, 116, 139));
        }

        btn.setOpaque(true);
        btn.setBorderPainted(false);
        return btn;
    }
}