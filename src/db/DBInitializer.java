package db;

import java.sql.Connection;
import java.sql.Statement;

public class DBInitializer {

    public static void init() {
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            // USERS
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS users (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            username TEXT UNIQUE,
                            pin TEXT
                        )
                    """);

            // EXPENSES
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS expenses (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            user_id INTEGER,
                            amount REAL,
                            category TEXT,
                            date TEXT
                        )
                    """);

            // INCOME
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS income (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            user_id INTEGER,
                            amount REAL,
                            source TEXT,
                            date TEXT
                        )
                    """);

            // DEBTS
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS debts (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            user_id INTEGER,
                            person TEXT,
                            amount REAL,
                            status TEXT,
                            date TEXT
                        )
                    """);

            // BUDGET
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS budget (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            user_id INTEGER,
                            amount REAL,
                            month TEXT
                        )
                    """);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}