package com.github.mariambarakzai;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DeadlockDemo {

    private static final String URL = "jdbc:mariadb://localhost:3306/bank_demo";
    private static final String USER = "bank";
    private static final String PASSWORD = "bank123";

    private static final int DEADLOCK_FEHLERCODE = 1213;

    public static void main(String[] args) throws Exception {
        durchlauf(false);
        durchlauf(true);
    }

    static void durchlauf(boolean mitRetry) throws Exception {
        System.out.println("\n=== " + (mitRetry ? "MIT" : "OHNE") + " automatisches Wiederholen ===");

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (Statement st = con.createStatement()) {
                st.executeUpdate("UPDATE konto SET saldo = 1000.00 WHERE ktonr = 1");
                st.executeUpdate("UPDATE konto SET saldo = 500.00 WHERE ktonr = 2");
            }
            System.out.println("Start:");
            zeigeKonten(con);

            Thread t1 = new Thread(() -> ueberweisen("T1", 1, 2, new BigDecimal("100.00"), mitRetry));
            Thread t2 = new Thread(() -> ueberweisen("T2", 2, 1, new BigDecimal("50.00"), mitRetry));
            t1.start();
            t2.start();
            t1.join();
            t2.join();

            System.out.println("Ende:");
            zeigeKonten(con);
        }
    }

    static void ueberweisen(String name, int von, int nach, BigDecimal betrag, boolean mitRetry) {
        int maxVersuche = mitRetry ? 3 : 1;

        for (int versuch = 1; versuch <= maxVersuche; versuch++) {
            try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {
                con.setAutoCommit(false);
                try {
                    aendereSaldo(con, von, betrag.negate());
                    Thread.sleep(500); // beide haben jetzt ihre erste Zeile gesperrt
                    aendereSaldo(con, nach, betrag);
                    con.commit();
                    System.out.println(name + " erfolgreich (Versuch " + versuch + ")");
                    return;
                } catch (SQLException e) {
                    con.rollback();
                    if (e.getErrorCode() == DEADLOCK_FEHLERCODE) {
                        System.out.println(name + " ist Deadlock-Opfer (Versuch " + versuch + ")");
                    } else {
                        throw e;
                    }
                }
            } catch (SQLException | InterruptedException e) {
                System.out.println(name + " Fehler: " + e.getMessage());
                return;
            }
        }
        System.out.println(name + " gibt auf");
    }

    static void aendereSaldo(Connection con, int ktonr, BigDecimal delta) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE konto SET saldo = saldo + ? WHERE ktonr = ?")) {
            ps.setBigDecimal(1, delta);
            ps.setInt(2, ktonr);
            ps.executeUpdate();
        }
    }

    static void zeigeKonten(Connection con) throws SQLException {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT ktonr, inhaber, saldo FROM konto")) {
            while (rs.next()) {
                System.out.println("  " + rs.getInt("ktonr") + " | "
                        + rs.getString("inhaber") + " | "
                        + rs.getBigDecimal("saldo"));
            }
        }
    }
}