package com.github.mariambarakzai;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {

    private static final String URL = "jdbc:mariadb://localhost:3306/bank_demo";
    private static final String USER = "bank";
    private static final String PASSWORD = "bank123";

    public static void main(String[] args) {
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {

            System.out.println("=== Start ===");
            zeigeKonten(con);

            System.out.println("\n--- Test 1: Absturz mitten in der Überweisung ---");
            try {
                ueberweisungMitTransaktion(con, 1, 2, new BigDecimal("50.00"), true);
            } catch (SQLException | RuntimeException e) {
                System.out.println("Abgebrochen: " + e.getMessage());
            }
            zeigeKonten(con);

            System.out.println("\n--- Test 2: Normale Überweisung ---");
            try {
                ueberweisungMitTransaktion(con, 1, 2, new BigDecimal("50.00"), false);
                System.out.println("Überweisung erfolgreich");
            } catch (SQLException | RuntimeException e) {
                System.out.println("Abgebrochen: " + e.getMessage());
            }
            zeigeKonten(con);

            System.out.println("\n--- Test 3: Zu wenig Geld auf dem Konto ---");
            try {
                ueberweisungMitTransaktion(con, 1, 2, new BigDecimal("5000.00"), false);
                System.out.println("Überweisung erfolgreich");
            } catch (SQLException | RuntimeException e) {
                System.out.println("Abgebrochen: " + e.getMessage());
            }
            zeigeKonten(con);

        } catch (SQLException e) {
            System.out.println("Fehler: " + e.getMessage());
        }
    }

    static void ueberweisungMitTransaktion(Connection con, int von, int nach,
                                           BigDecimal betrag, boolean absturz) throws SQLException {
        boolean alterModus = con.getAutoCommit();
        con.setAutoCommit(false);              // START TRANSACTION
        try (PreparedStatement abbuchen = con.prepareStatement(
                "UPDATE konto SET saldo = saldo - ? WHERE ktonr = ?");
             PreparedStatement gutschreiben = con.prepareStatement(
                     "UPDATE konto SET saldo = saldo + ? WHERE ktonr = ?");
             PreparedStatement protokoll = con.prepareStatement(
                     "INSERT INTO buchung (von_kto, nach_kto, betrag) VALUES (?, ?, ?)")) {

            abbuchen.setBigDecimal(1, betrag);
            abbuchen.setInt(2, von);
            abbuchen.executeUpdate();

            if (absturz) {
                throw new RuntimeException("Server-Absturz zwischen den UPDATEs");
            }

            gutschreiben.setBigDecimal(1, betrag);
            gutschreiben.setInt(2, nach);
            gutschreiben.executeUpdate();

            protokoll.setInt(1, von);
            protokoll.setInt(2, nach);
            protokoll.setBigDecimal(3, betrag);
            protokoll.executeUpdate();

            con.commit();                      // COMMIT
        } catch (SQLException | RuntimeException e) {
            con.rollback();                    // ROLLBACK
            throw e;
        } finally {
            con.setAutoCommit(alterModus);
        }
    }

    static void zeigeKonten(Connection con) throws SQLException {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT ktonr, inhaber, saldo FROM konto")) {
            while (rs.next()) {
                System.out.println(rs.getInt("ktonr") + " | "
                        + rs.getString("inhaber") + " | "
                        + rs.getBigDecimal("saldo"));
            }
        }
    }
}