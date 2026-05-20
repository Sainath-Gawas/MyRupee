package screens;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.time.YearMonth;
import java.util.Map;
import dao.ExpenseDAO;

public class InsightsScreen extends JFrame {

    private int userId;
    private Dashboard dashboard;

    public InsightsScreen(int userId, Dashboard dashboard) {
        this.userId = userId;
        this.dashboard = dashboard;

        setTitle("Insights");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245, 247, 250));
        setLayout(new BorderLayout());

        JPanel mainContainer = new JPanel(new GridLayout(1, 2, 20, 0));
        mainContainer.setBackground(new Color(245, 247, 250));
        mainContainer.setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- LEFT PANEL (Pie Chart & Legend) ---
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel leftTitle = new JLabel("Category Breakdown");
        leftTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        leftTitle.setBorder(new EmptyBorder(0, 0, 20, 0));
        leftPanel.add(leftTitle, BorderLayout.NORTH);

        JPanel pieContainer = new JPanel();
        pieContainer.setLayout(new BoxLayout(pieContainer, BoxLayout.Y_AXIS));
        pieContainer.setBackground(Color.WHITE);

        Map<String, Double> categoryData = ExpenseDAO.getCategoryBreakdown(userId);

        JPanel pieChart = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                int size = Math.min(width, height) - 40;
                int x = (width - size) / 2;
                int y = (height - size) / 2;

                double total = categoryData.values().stream().mapToDouble(Double::doubleValue).sum();
                double startAngle = 90;

                Color[] colors = { new Color(96, 165, 250), new Color(52, 211, 153), new Color(192, 132, 252),
                        new Color(251, 146, 60), new Color(248, 113, 113) };
                int colorIndex = 0;

                if (total == 0) {
                    g2d.setColor(new Color(226, 232, 240));
                    g2d.fillOval(x, y, size, size);
                } else {
                    for (Map.Entry<String, Double> entry : categoryData.entrySet()) {
                        double extent = (entry.getValue() / total) * 360;
                        g2d.setColor(colors[colorIndex % colors.length]);
                        g2d.fill(new Arc2D.Double(x, y, size, size, startAngle, -extent, Arc2D.PIE));
                        startAngle -= extent;
                        colorIndex++;
                    }
                }

                g2d.setColor(Color.WHITE);
                int innerSize = size / 3;
                g2d.fillOval(x + (size - innerSize) / 2, y + (size - innerSize) / 2, innerSize, innerSize);
            }
        };
        pieChart.setPreferredSize(new Dimension(300, 250));
        pieChart.setMaximumSize(new Dimension(300, 250));
        pieChart.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel summaryLabel = new JLabel("Summary");
        summaryLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        summaryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        summaryLabel.setBorder(new EmptyBorder(20, 0, 15, 0));

        JPanel legendPanel = new JPanel();
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        legendPanel.setBackground(Color.WHITE);
        legendPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        for (Map.Entry<String, Double> entry : categoryData.entrySet()) {
            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(Color.WHITE);
            row.setMaximumSize(new Dimension(250, 30));

            JLabel catLabel = new JLabel("📌 " + entry.getKey());
            catLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

            JLabel amtLabel = new JLabel("₹" + entry.getValue());
            amtLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

            row.add(catLabel, BorderLayout.WEST);
            row.add(amtLabel, BorderLayout.EAST);
            legendPanel.add(row);
            legendPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        // FIX: Wrap legend in a ScrollPane so it doesn't break if there are many
        // categories
        JScrollPane legendScroll = new JScrollPane(legendPanel);
        legendScroll.setBorder(null);
        legendScroll.setBackground(Color.WHITE);
        legendScroll.getVerticalScrollBar().setUnitIncrement(16);

        pieContainer.add(pieChart);
        pieContainer.add(summaryLabel);
        pieContainer.add(legendScroll);

        // FIX: Add vertical glue to stop elements from stretching awkwardly when window
        // is resized
        pieContainer.add(Box.createVerticalGlue());

        leftPanel.add(pieContainer, BorderLayout.CENTER);

        // --- RIGHT PANEL (Top Cards & Line Graph) ---
        JPanel rightPanel = new JPanel(new BorderLayout(0, 20));
        rightPanel.setBackground(new Color(245, 247, 250));

        JPanel topCardsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        topCardsPanel.setBackground(new Color(245, 247, 250));

        // Fetch Data for Cards
        String[] mostSpent = ExpenseDAO.getMostSpentCategory(userId);
        String[] leastSpent = ExpenseDAO.getLeastSpentCategory(userId);
        double avgDaily = ExpenseDAO.getAverageDailyExpense(userId);

        // Fetch current month's expenses for the new card
        String currentMonth = YearMonth.now().toString();
        double monthlyTotal = ExpenseDAO.getCurrentMonthExpense(userId, currentMonth);

        // Build 4 Cards
        JPanel mostSpentCard = createStatCard("Most Spent Category", mostSpent[0], "Total ₹" + mostSpent[1],
                new Color(30, 58, 138), Color.WHITE, new Color(147, 197, 253));

        JPanel leastSpentCard = createStatCard("Least Spent Category", leastSpent[0], "Total ₹" + leastSpent[1],
                Color.WHITE, Color.BLACK, new Color(100, 116, 139));

        // NEW CARD: Monthly Expenses (Styled in Emerald Green to stand out)
        JPanel monthlyExpenseCard = createStatCard("This Month's Exp.", "₹" + Math.round(monthlyTotal), currentMonth,
                new Color(16, 185, 129), Color.WHITE, new Color(209, 250, 229));

        JPanel avgDailyCard = createStatCard("Avg Daily Expense", "₹" + Math.round(avgDaily), "All Time",
                Color.WHITE, Color.BLACK, new Color(100, 116, 139));

        topCardsPanel.add(mostSpentCard);
        topCardsPanel.add(leastSpentCard);
        topCardsPanel.add(monthlyExpenseCard); // Placed in the empty space
        topCardsPanel.add(avgDailyCard);

        JPanel graphCard = new JPanel(new BorderLayout());
        graphCard.setBackground(Color.WHITE);
        graphCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel graphTitle = new JLabel("Weekly Expense Trend");
        graphTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        graphTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        graphCard.add(graphTitle, BorderLayout.NORTH);

        double[] weeklyData = ExpenseDAO.getWeeklyExpenseTrend(userId);

        JPanel lineGraph = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int padding = 30;
                int width = getWidth();
                int height = getHeight();

                g2d.setColor(new Color(226, 232, 240));
                g2d.drawLine(padding, height - padding, width - padding, height - padding);
                g2d.drawLine(padding, padding, padding, height - padding);

                double max = 0;
                for (double v : weeklyData) {
                    if (v > max)
                        max = v;
                }
                if (max == 0)
                    max = 100;

                g2d.setColor(new Color(41, 100, 232));
                g2d.setStroke(new BasicStroke(3f));

                int xStep = (width - 2 * padding) / (weeklyData.length > 1 ? weeklyData.length - 1 : 1);

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
        graphCard.add(lineGraph, BorderLayout.CENTER);

        rightPanel.add(topCardsPanel, BorderLayout.NORTH);
        rightPanel.add(graphCard, BorderLayout.CENTER);

        mainContainer.add(leftPanel);
        mainContainer.add(rightPanel);

        add(mainContainer, BorderLayout.CENTER);
        setVisible(true);
    }

    private JPanel createStatCard(String title, String mainValue, String subtitle, Color bgColor, Color fgColor,
            Color subColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bgColor);
        if (bgColor.equals(Color.WHITE)) {
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                    new EmptyBorder(15, 15, 15, 15)));
        } else {
            card.setBorder(new EmptyBorder(16, 16, 16, 16));
        }

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        titleLabel.setForeground(bgColor.equals(Color.WHITE) ? new Color(100, 116, 139) : subColor);

        JLabel valueLabel = new JLabel(mainValue);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 24)); // Slightly reduced font so long names fit
        valueLabel.setForeground(fgColor);

        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subLabel.setForeground(subColor);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(bgColor);
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(valueLabel, BorderLayout.CENTER);

        card.add(topPanel, BorderLayout.CENTER);
        card.add(subLabel, BorderLayout.SOUTH);

        return card;
    }
}