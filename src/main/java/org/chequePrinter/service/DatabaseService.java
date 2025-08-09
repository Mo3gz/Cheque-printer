package org.chequePrinter.service;

import org.chequePrinter.model.ChequeData;
import org.chequePrinter.util.ExceptionHandler;
import org.chequePrinter.util.LoggerUtil;
import org.slf4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {

    private static final Logger logger = LoggerUtil.getLogger(DatabaseService.class);
    private static final String DB_URL = "jdbc:sqlite:cheques.db";

    public static void initializeDatabase() {
        LoggerUtil.logMethodEntry(logger, "initializeDatabase");
        
        ExceptionHandler.executeWithExceptionHandling(logger, "initializeDatabase", () -> {
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                if (conn != null) {
                    LoggerUtil.logDatabaseOperation(logger, "connect", "cheques", DB_URL);
                    
                    Statement stmt = conn.createStatement();
                    // Enable UTF-8 support for SQLite
                    stmt.execute("PRAGMA encoding = 'UTF-8';");
                    logger.debug("Enabled UTF-8 encoding for SQLite database");
                    
                    // Create table if it doesn't exist
                    String createTableSql = "CREATE TABLE IF NOT EXISTS cheques ("
                            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + "cheque_date TEXT NOT NULL,"
                            + "beneficiary_name TEXT NOT NULL,"
                            + "amount_numeric REAL NOT NULL,"
                            + "amount_words TEXT NOT NULL,"
                            + "signer_name TEXT NOT NULL"
                            + ");";
                    stmt.execute(createTableSql);
                    LoggerUtil.logDatabaseOperation(logger, "create_table", "cheques");
                    
                    // Try to add phone_number column if it doesn't exist
                    try {
                        stmt.execute("ALTER TABLE cheques ADD COLUMN phone_number TEXT");
                        logger.info("Added phone_number column to existing table");
                    } catch (SQLException e) {
                        // Column might already exist, which is fine
                        logger.debug("Phone number column already exists: {}", e.getMessage());
                    }
                    
                    logger.info("Database initialized successfully with UTF-8 encoding");
                } else {
                    throw new SQLException("Failed to establish database connection");
                }
            } catch (SQLException e) {
                throw new RuntimeException("Database initialization failed", e);
            }
        }, "Failed to initialize database. Please check if the database file is accessible.");
        
        LoggerUtil.logMethodExit(logger, "initializeDatabase");
    }

    public static void saveCheque(ChequeData cheque) {
        LoggerUtil.logMethodEntry(logger, "saveCheque", cheque.getBeneficiaryName(), cheque.getAmountNumeric());
        
        ExceptionHandler.executeWithExceptionHandling(logger, "saveCheque", () -> {
            String sql = "INSERT INTO cheques(cheque_date, beneficiary_name, amount_numeric, amount_words, signer_name, phone_number) VALUES(?,?,?,?,?,?)";

            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                LoggerUtil.logDatabaseOperation(logger, "insert", "cheques",
                    cheque.getBeneficiaryName(), cheque.getAmountNumeric());
                
                pstmt.setString(1, cheque.getDate());
                pstmt.setString(2, cheque.getBeneficiaryName());
                pstmt.setDouble(3, Double.parseDouble(cheque.getAmountNumeric()));
                pstmt.setString(4, cheque.getAmountWords());
                pstmt.setString(5, cheque.getSignerName());
                pstmt.setString(6, cheque.getPhoneNumber());
                
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    logger.info("Cheque saved successfully: beneficiary={}, amount={}",
                        cheque.getBeneficiaryName(), cheque.getAmountNumeric());
                } else {
                    throw new SQLException("No rows were inserted");
                }
                
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save cheque to database", e);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid amount format: " + cheque.getAmountNumeric(), e);
            }
        }, "Failed to save cheque to database. Please check your data and try again.");
        
        LoggerUtil.logMethodExit(logger, "saveCheque");
    }

    public static List<ChequeData> getAllCheques() {
        LoggerUtil.logMethodEntry(logger, "getAllCheques");
        
        return ExceptionHandler.executeWithExceptionHandling(logger, "getAllCheques", () -> {
            String sql = "SELECT * FROM cheques ORDER BY id DESC";
            List<ChequeData> cheques = new ArrayList<>();

            try (Connection conn = DriverManager.getConnection(DB_URL);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                LoggerUtil.logDatabaseOperation(logger, "select_all", "cheques");
                
                int count = 0;
                while (rs.next()) {
                    cheques.add(new ChequeData(
                            rs.getInt("id"),
                            rs.getString("cheque_date"),
                            rs.getString("beneficiary_name"),
                            String.valueOf(rs.getDouble("amount_numeric")),
                            rs.getString("amount_words"),
                            rs.getString("signer_name"),
                            rs.getString("phone_number")
                    ));
                    count++;
                }
                
                logger.info("Retrieved {} cheques from database", count);
                LoggerUtil.logMethodExit(logger, "getAllCheques", count);
                return cheques;
                
            } catch (SQLException e) {
                throw new RuntimeException("Failed to retrieve cheques from database", e);
            }
        }, "Failed to load cheques from database. Please check the database connection.").orElse(new ArrayList<>());
    }

    public static void deleteCheque(int id) {
        LoggerUtil.logMethodEntry(logger, "deleteCheque", id);
        
        ExceptionHandler.executeWithExceptionHandling(logger, "deleteCheque", () -> {
            String sql = "DELETE FROM cheques WHERE id = ?";

            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                LoggerUtil.logDatabaseOperation(logger, "delete", "cheques", id);
                
                pstmt.setInt(1, id);
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    logger.info("Successfully deleted cheque with ID: {}", id);
                } else {
                    logger.warn("No cheque found with ID: {} - nothing was deleted", id);
                }
                
            } catch (SQLException e) {
                throw new RuntimeException("Failed to delete cheque from database", e);
            }
        }, "Failed to delete cheque from database. Please try again.");
        
        LoggerUtil.logMethodExit(logger, "deleteCheque");
    }
}
