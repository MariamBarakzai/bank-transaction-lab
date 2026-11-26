package com.github.mariambarakzai;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {

    private static final String URL = "jdbc:mariadb://localhost:3306/bank_demo";
    private static final String USER = "bank";
    private static final String PASSWORD = "bank123";

    public static void main(String[] args) {

        try {
            Class.forName("org.mariadb.jdbc.Driver");
            System.out.println("Treiber gefunden");
        } catch (ClassNotFoundException e) {
            System.out.println("Treiber NICHT im Classpath");
        }

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT ktonr, inhaber, saldo FROM konto")) {

            System.out.println("Verbindung steht!");
            while (rs.next()) {
                System.out.println(rs.getInt("ktonr") + " | "
                        + rs.getString("inhaber") + " | "
                        + rs.getBigDecimal("saldo"));
            }

        } catch (SQLException e) {
            System.out.println("Fehler: " + e.getMessage());
        }
    }
}