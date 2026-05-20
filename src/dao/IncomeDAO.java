package dao;

import db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class IncomeDAO {

    // Gets all income for the table, now including the NOTE column
    public static Object[][] getIncomeForTable(int userId) {
        ArrayList<Object[]> list = new ArrayList<>();
        String sql = "SELECT id, date, amount, note FROM income WHERE user_id = ? ORDER BY date DESC";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("id"),
                        rs.getString("date"),
                        String.valueOf(rs.getDouble("amount")),
                        rs.getString("note") // Fetching the new note column
                };
                list.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list.toArray(new Object[0][]);
    }

    // Updates an existing income record, now including the NOTE column
    public static void updateIncome(int id, double amount, String date, String note) {
        String sql = "UPDATE income SET amount = ?, date = ?, note = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, amount);
            ps.setString(2, date);
            ps.setString(3, note);
            ps.setInt(4, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Deletes an income record
    public static void deleteIncome(int id) {
        String sql = "DELETE FROM income WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Calculates total income for the Dashboard
    public static double getTotalIncome(int userId) {
        String sql = "SELECT SUM(amount) FROM income WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}