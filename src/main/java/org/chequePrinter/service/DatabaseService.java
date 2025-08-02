package org.chequePrinter.service;

import org.chequePrinter.model.ChequeData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {

    private static final String DB_URL = "jdbc:sqlite:cheques.db";

    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (conn != null) {
                Statement stmt = conn.createStatement();
                // Enable UTF-8 support for SQLite
                stmt.execute("PRAGMA encoding = 'UTF-8';");
                String sql = "CREATE TABLE IF NOT EXISTS cheques ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "cheque_date TEXT NOT NULL,"
                        + "beneficiary_name TEXT NOT NULL,"
                        + "amount_numeric REAL NOT NULL,"
                        + "amount_words TEXT NOT NULL,"
                        + "signer_name TEXT NOT NULL"
                        + ");";
                stmt.execute(sql);
                System.out.println("Database initialized with UTF-8 encoding");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void saveCheque(ChequeData cheque) {
        String sql = "INSERT INTO cheques(cheque_date, beneficiary_name, amount_numeric, amount_words, signer_name) VALUES(?,?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cheque.getDate());
            pstmt.setString(2, cheque.getBeneficiaryName());
            pstmt.setDouble(3, Double.parseDouble(cheque.getAmountNumeric()));
            pstmt.setString(4, cheque.getAmountWords());
            pstmt.setString(5, cheque.getSignerName());
            pstmt.executeUpdate();
            System.out.println("Cheque saved with Arabic text: " + cheque.getBeneficiaryName());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<ChequeData> getAllCheques() {
        String sql = "SELECT * FROM cheques ORDER BY id DESC";
        List<ChequeData> cheques = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                cheques.add(new ChequeData(
                        rs.getInt("id"),
                        rs.getString("cheque_date"),
                        rs.getString("beneficiary_name"),
                        String.valueOf(rs.getDouble("amount_numeric")),
                        rs.getString("amount_words"),
                        rs.getString("signer_name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cheques;
    }

    public static void deleteCheque(int id) {
        String sql = "DELETE FROM cheques WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Deleted cheque with ID: " + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
