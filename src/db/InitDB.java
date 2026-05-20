package db;

import java.sql.*;

public class InitDB {

    public static void init() {
        String url = "jdbc:sqlite:myrupee.db";

        String usersTable = """
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT UNIQUE,
                        pin TEXT
                    )
                """;

        String expenseTable = """
                    CREATE TABLE IF NOT EXISTS expenses (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER,
                        amount REAL,
                        category TEXT,
                        date TEXT,
                        note TEXT
                    )
                """;

        String incomeTable = """
                    CREATE TABLE IF NOT EXISTS income (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER,
                        amount REAL,
                        date TEXT
                    )
                """;

        String debtTable = """
                    CREATE TABLE IF NOT EXISTS debts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER,
                        person TEXT,
                        amount REAL,
                        status TEXT,
                        date TEXT,
                        note TEXT,
                        email TEXT
                    )
                """;

        String budgetTable = """
                    CREATE TABLE IF NOT EXISTS budget (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER,
                        amount REAL,
                        month TEXT
                    )
                """;

        try (Connection conn = DriverManager.getConnection(url);
                Statement stmt = conn.createStatement()) {

            stmt.execute(usersTable);
            stmt.execute(expenseTable);
            stmt.execute(incomeTable);
            stmt.execute(debtTable);
            stmt.execute(budgetTable);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}