package com.github.mariambarakzai;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LostUpdateDemo {

    private static final String URL = "jdbc:mariadb://localhost:3306/bank_demo";
    private static final String USER = "bank";
    private static final String PASSWORD = "bank123";

    public static void main(String[] args) throws Exception {
        durchlauf(false);
        durchlauf(true);
    }

    static void durchlauf(boolean mitSperre) throws Exception {
        System.out.println("\n=== " + (mitSperre ? "MIT" : "OHNE") + " SELECT ... FOR UPDATE ===");

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (Statement st = con.createStatement()) {
                st.executeUpdate("UPDATE konto SET saldo = 1000.00 WHERE ktonr = 1");
            }
            System.out.println("Start: " + leseSaldo(con));

            Thread t1 = new Thread(() -> einzahlen("T1", new BigDecimal("100.00"), mitSperre));
            Thread t2 = new Thread(() -> einzahlen("T2", new BigDecimal("100.00"), mitSperre));
            t1.start();
            t2.start();
            t1.join();
            t2.join();

            System.out.println("Ende:  " + leseSaldo(con) + "  (erwartet: 1200.00)");
        }
    }

    static void einzahlen(String name, BigDecimal betrag, boolean mitSperre) {
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {
            con.setAutoCommit(false);

            String sql = "SELECT saldo FROM konto WHERE ktonr = 1"
                    + (mitSperre ? " FOR UPDATE" : "");
            BigDecimal saldo;
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                rs.next();
                saldo = rs.getBigDecimal("saldo");
            }
            System.out.println(name + " liest " + saldo);

            Thread.sleep(500); // simuliert Rechenzeit der Anwendung

            BigDecimal neu = saldo.add(betrag);
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE konto SET saldo = ? WHERE ktonr = 1")) {
                ps.setBigDecimal(1, neu);
                ps.executeUpdate();
            }
            con.commit();
            System.out.println(name + " schreibt " + neu + " und committet");

        } catch (SQLException | InterruptedException e) {
            System.out.println(name + " Fehler: " + e.getMessage());
        }
    }

    static BigDecimal leseSaldo(Connection con) throws SQLException {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT saldo FROM konto WHERE ktonr = 1")) {
            rs.next();
            return rs.getBigDecimal("saldo");
        }
    }
}