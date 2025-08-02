package org.chequePrinter.model;

public class ChequeData {
    private int id;
    private String date;
    private String beneficiaryName;
    private String amountNumeric;
    private String amountWords;
    private String signerName;

    // Default constructor
    public ChequeData() {}

    public ChequeData(String date, String beneficiaryName, String amountNumeric, String amountWords, String signerName) {
        this.date = date;
        this.beneficiaryName = beneficiaryName;
        this.amountNumeric = amountNumeric;
        this.amountWords = amountWords;
        this.signerName = signerName;
    }

    public ChequeData(int id, String date, String beneficiaryName, String amountNumeric, String amountWords, String signerName) {
        this.id = id;
        this.date = date;
        this.beneficiaryName = beneficiaryName;
        this.amountNumeric = amountNumeric;
        this.amountWords = amountWords;
        this.signerName = signerName;
    }

    // Getters
    public int getId() { return id; }
    public String getDate() { return date; }
    public String getBeneficiaryName() { return beneficiaryName; }
    public String getAmountNumeric() { return amountNumeric; }
    public String getAmountWords() { return amountWords; }
    public String getSignerName() { return signerName; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setDate(String date) { this.date = date; }
    public void setBeneficiaryName(String beneficiaryName) { this.beneficiaryName = beneficiaryName; }
    public void setAmountNumeric(String amountNumeric) { this.amountNumeric = amountNumeric; }
    public void setAmountWords(String amountWords) { this.amountWords = amountWords; }
    public void setSignerName(String signerName) { this.signerName = signerName; }
}
