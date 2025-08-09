package org.chequePrinter.model;

public class ChequeData {
    private int id;
    private String date;
    private String beneficiaryName;
    private String amountNumeric;
    private String amountWords;
    private String signerName;
    private String phoneNumber;

    // Default constructor
    public ChequeData() {}

    public ChequeData(String date, String beneficiaryName, String amountNumeric, String amountWords, String signerName) {
        this.date = date;
        this.beneficiaryName = beneficiaryName;
        this.amountNumeric = amountNumeric;
        this.amountWords = amountWords;
        this.signerName = signerName;
    }

    public ChequeData(String date, String beneficiaryName, String amountNumeric, String amountWords, String signerName, String phoneNumber) {
        this.date = date;
        this.beneficiaryName = beneficiaryName;
        this.amountNumeric = amountNumeric;
        this.amountWords = amountWords;
        this.signerName = signerName;
        this.phoneNumber = phoneNumber;
    }

    public ChequeData(int id, String date, String beneficiaryName, String amountNumeric, String amountWords, String signerName) {
        this.id = id;
        this.date = date;
        this.beneficiaryName = beneficiaryName;
        this.amountNumeric = amountNumeric;
        this.amountWords = amountWords;
        this.signerName = signerName;
    }

    public ChequeData(int id, String date, String beneficiaryName, String amountNumeric, String amountWords, String signerName, String phoneNumber) {
        this.id = id;
        this.date = date;
        this.beneficiaryName = beneficiaryName;
        this.amountNumeric = amountNumeric;
        this.amountWords = amountWords;
        this.signerName = signerName;
        this.phoneNumber = phoneNumber;
    }

    // Getters
    public int getId() { return id; }
    public String getDate() { return date; }
    public String getBeneficiaryName() { return beneficiaryName; }
    public String getAmountNumeric() { return amountNumeric; }
    public String getAmountWords() { return amountWords; }
    public String getSignerName() { return signerName; }
    public String getPhoneNumber() { return phoneNumber; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setDate(String date) { this.date = date; }
    public void setBeneficiaryName(String beneficiaryName) { this.beneficiaryName = beneficiaryName; }
    public void setAmountNumeric(String amountNumeric) { this.amountNumeric = amountNumeric; }
    public void setAmountWords(String amountWords) { this.amountWords = amountWords; }
    public void setSignerName(String signerName) { this.signerName = signerName; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    // Helper method to validate phone number format (11 digits starting with 01)
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return true; // Phone number is optional
        }
        return phoneNumber.matches("^01\\d{9}$");
    }
}
