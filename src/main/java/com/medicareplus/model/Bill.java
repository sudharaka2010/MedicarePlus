package com.medicareplus.model;

public class Bill {

    private int billId;
    private int patientId;
    private String billDate;
    private double totalAmount;
    private double advanceUsed;
    private double payableAmount;
    private String notes;

    // For inserting new bill (no billId/billDate needed)
    public Bill(int patientId, double totalAmount, double advanceUsed, double payableAmount, String notes) {
        this.patientId = patientId;
        this.totalAmount = totalAmount;
        this.advanceUsed = advanceUsed;
        this.payableAmount = payableAmount;
        this.notes = notes;
    }

    // For reading from DB
    public Bill(int billId, int patientId, String billDate, double totalAmount, double advanceUsed, double payableAmount, String notes) {
        this.billId = billId;
        this.patientId = patientId;
        this.billDate = billDate;
        this.totalAmount = totalAmount;
        this.advanceUsed = advanceUsed;
        this.payableAmount = payableAmount;
        this.notes = notes;
    }

    public int getBillId() { return billId; }
    public int getPatientId() { return patientId; }
    public String getBillDate() { return billDate; }
    public double getTotalAmount() { return totalAmount; }
    public double getAdvanceUsed() { return advanceUsed; }
    public double getPayableAmount() { return payableAmount; }
    public String getNotes() { return notes; }
}
