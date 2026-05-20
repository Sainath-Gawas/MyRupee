package dao;

import db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class BudgetDAO {

    /**
     * Retrieves the budget amount for a specific user and month.
     * 
     * @param userId The ID of the user.
     * @param month  The month in "YYYY-MM" format.
     * @return The budget amount, or 0.0 if not set.
     */
    public static double getMonthlyBudget(int userId, String month) {
        double budget = 0.0;
        String sql = "SELECT amount FROM budget WHERE user_id = ? AND month = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, month);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                budget = rs.getDouble("amount");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return budget;
    }

    /**
     * Saves or updates the budget for a specific user and month.
     * 
     * @param userId The ID of the user.
     * @param amount The budget amount.
     * @param month  The month in "YYYY-MM" format.
     */
    public static void saveBudget(int userId, double amount, String month) {
        // First, check if a budget for this month already exists
        String checkSql = "SELECT id FROM budget WHERE user_id = ? AND month = ?";
        String updateSql = "UPDATE budget SET amount = ? WHERE id = ?";
        String insertSql = "INSERT INTO budget(user_id, amount, month) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement checkPs = conn.prepareStatement(checkSql)) {

            checkPs.setInt(1, userId);
            checkPs.setString(2, month);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {
                // Budget exists, update it
                int id = rs.getInt("id");
                try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                    updatePs.setDouble(1, amount);
                    updatePs.setInt(2, id);
                    updatePs.executeUpdate();
                }
            } else {
                // Budget does not exist, insert new
                try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                    insertPs.setInt(1, userId);
                    insertPs.setDouble(2, amount);
                    insertPs.setString(3, month);
                    insertPs.executeUpdate();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}