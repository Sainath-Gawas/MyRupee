package dao;

import java.sql.*;

import db.DBConnection;

public class UserDAO {

    public static int login(String username, String pin) {
        int userId = -1;

        String sql = "SELECT id FROM users WHERE username = ? AND pin = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, pin);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                userId = rs.getInt("id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return userId;
    }

    public static boolean register(String username, String pin) {
        String sql = "INSERT INTO users(username, pin) VALUES(?, ?)";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, pin);

            stmt.executeUpdate();
            return true;

        } catch (Exception e) {
            return false;
        }
    }
}