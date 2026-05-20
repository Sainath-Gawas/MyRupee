package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import db.DBConnection;

public class ExpenseDAO {

    public static double getTotalExpense(int userId) {
        double total = 0;
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT SUM(amount) FROM expenses WHERE user_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                total = rs.getDouble(1);
                if (rs.wasNull()) {
                    total = 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    public static Map<String, Double> getCategoryBreakdown(int userId) {
        Map<String, Double> map = new HashMap<>();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT category, SUM(amount) FROM expenses WHERE user_id = ? GROUP BY category";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                map.put(rs.getString(1), rs.getDouble(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static String[] getMostSpentCategory(int userId) {
        String[] result = { "N/A", "0" };
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT category, SUM(amount) as total FROM expenses WHERE user_id = ? GROUP BY category ORDER BY total DESC LIMIT 1";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result[0] = rs.getString(1);
                result[1] = String.valueOf(rs.getDouble(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String[] getLeastSpentCategory(int userId) {
        String[] result = { "N/A", "0" };
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT category, SUM(amount) as total FROM expenses WHERE user_id = ? GROUP BY category ORDER BY total ASC LIMIT 1";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result[0] = rs.getString(1);
                result[1] = String.valueOf(rs.getDouble(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static double getAverageDailyExpense(int userId) {
        double avg = 0;
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT SUM(amount) FROM expenses WHERE user_id = ? AND date >= date('now', '-30 days')";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double sum = rs.getDouble(1);
                avg = sum / 30.0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return avg;
    }

    public static double[] getWeeklyExpenseTrend(int userId) {
        double[] trend = new double[7];
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT date, SUM(amount) FROM expenses WHERE user_id = ? AND date >= date('now', '-6 days') GROUP BY date";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            Map<String, Double> map = new HashMap<>();
            while (rs.next()) {
                map.put(rs.getString(1), rs.getDouble(2));
            }
            LocalDate today = LocalDate.now();
            for (int i = 0; i < 7; i++) {
                String d = today.minusDays(6 - i).toString();
                trend[i] = map.getOrDefault(d, 0.0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return trend;
    }

    /**
     * Retrieves total expenses for a specific month.
     * 
     * @param userId      The ID of the user.
     * @param monthYYYYMM The month in "YYYY-MM" format.
     * @return The total expense for that month.
     */
    public static double getCurrentMonthExpense(int userId, String monthYYYYMM) {
        double total = 0;
        try (Connection conn = DBConnection.getConnection()) {
            // Using LIKE to match all dates starting with the specified year and month
            String sql = "SELECT SUM(amount) FROM expenses WHERE user_id = ? AND date LIKE ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setString(2, monthYYYYMM + "-%");
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                total = rs.getDouble(1);
                if (rs.wasNull()) {
                    total = 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    public static Object[][] getExpensesForTable(int userId) {
        String countSql = "SELECT COUNT(*) FROM expenses WHERE user_id = ?";
        String dataSql = "SELECT id, date, category, amount, note FROM expenses WHERE user_id = ? ORDER BY date DESC, id DESC";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement countPs = conn.prepareStatement(countSql);
                PreparedStatement dataPs = conn.prepareStatement(dataSql)) {

            countPs.setInt(1, userId);
            ResultSet countRs = countPs.executeQuery();
            int rowCount = 0;
            if (countRs.next()) {
                rowCount = countRs.getInt(1);
            }

            Object[][] data = new Object[rowCount][5];
            dataPs.setInt(1, userId);
            ResultSet rs = dataPs.executeQuery();

            int i = 0;
            while (rs.next()) {
                data[i][0] = rs.getInt("id"); // Hidden ID
                data[i][1] = rs.getString("date");
                data[i][2] = rs.getString("category");
                data[i][3] = String.valueOf(rs.getDouble("amount"));
                data[i][4] = rs.getString("note");
                i++;
            }
            return data;

        } catch (Exception e) {
            e.printStackTrace();
            return new Object[0][5];
        }
    }

    public static void updateExpense(int expenseId, double amount, String category, String date, String note) {
        String sql = "UPDATE expenses SET amount = ?, category = ?, date = ?, note = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setString(2, category);
            ps.setString(3, date);
            ps.setString(4, note);
            ps.setInt(5, expenseId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteExpense(int expenseId) {
        String sql = "DELETE FROM expenses WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, expenseId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}