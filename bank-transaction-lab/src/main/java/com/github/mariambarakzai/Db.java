package com.github.mariambarakzai;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Db {

    private static final String URL =
            env("DB_URL", "jdbc:mariadb://localhost:3306/bank_demo");
    private static final String USER =
            env("DB_USER", "bank");
    private static final String PASSWORD =
            env("DB_PASSWORD", "bank123");

    private Db() {
    }

    public static Connection connect() throws SQLException {
        return Db.connect();
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return (value == null || value.isBlank()) ? fallback : value;
    }
}