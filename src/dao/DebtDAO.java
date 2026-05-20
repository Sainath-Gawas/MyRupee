package dao;

import db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DebtDAO {

    public static double getTotalPendingDebt(int userId) {
        double total = 0.0;
        String sql = "SELECT SUM(amount) FROM debts WHERE user_id = ? AND status = 'Pending'";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                total = rs.getDouble(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return total;
    }

    public static Object[][] getDebtsForTable(int userId) {
        String countSql = "SELECT COUNT(*) FROM debts WHERE user_id = ?";
        // Fetch all details so we can use them for editing, even if hidden in the UI
        String dataSql = "SELECT id, person, amount, date, status, note, email FROM debts WHERE user_id = ? ORDER BY date DESC, id DESC";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement countPs = conn.prepareStatement(countSql);
                PreparedStatement dataPs = conn.prepareStatement(dataSql)) {

            countPs.setInt(1, userId);
            ResultSet countRs = countPs.executeQuery();
            int rowCount = 0;
            if (countRs.next()) {
                rowCount = countRs.getInt(1);
            }

            Object[][] data = new Object[rowCount][7];
            dataPs.setInt(1, userId);
            ResultSet rs = dataPs.executeQuery();

            int i = 0;
            while (rs.next()) {
                data[i][0] = rs.getInt("id"); // Hidden
                data[i][1] = rs.getString("person");
                data[i][2] = String.valueOf(rs.getDouble("amount")); // String format for UI
                data[i][3] = rs.getString("date");
                data[i][4] = rs.getString("status");
                data[i][5] = rs.getString("note"); // Hidden
                data[i][6] = rs.getString("email"); // Hidden
                i++;
            }

            return data;

        } catch (Exception e) {
            e.printStackTrace();
            return new Object[0][7];
        }
    }

    public static void addDebt(int userId, String person, double amount, String date, String note, String email) {
        String sql = "INSERT INTO debts(user_id, person, amount, status, date, note, email) VALUES (?, ?, ?, 'Pending', ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, person);
            ps.setDouble(3, amount);
            ps.setString(4, date);
            ps.setString(5, note);
            ps.setString(6, email);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateDebt(int debtId, String person, double amount, String date, String note, String email) {
        String sql = "UPDATE debts SET person = ?, amount = ?, date = ?, note = ?, email = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, person);
            ps.setDouble(2, amount);
            ps.setString(3, date);
            ps.setString(4, note);
            ps.setString(5, email);
            ps.setInt(6, debtId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void markDebtAsPaid(int debtId) {
        String sql = "UPDATE debts SET status = 'Paid' WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, debtId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteDebt(int debtId) {
        String sql = "DELETE FROM debts WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, debtId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}