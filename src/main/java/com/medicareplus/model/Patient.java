package com.medicareplus.model;

public class Patient {

    private int patientId;
    private String fullName;
    private String nic;
    private String phone;
    private String email;
    private String address;
    private String medicalHistory;
    private double advancePaid;   // ✅ NEW

    // No-argument constructor
    public Patient() {
    }

    // Constructor without ID (for insert)
    public Patient(String fullName, String nic, String phone,
                   String email, String address, String medicalHistory) {
        this.fullName = fullName;
        this.nic = nic;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.medicalHistory = medicalHistory;
        this.advancePaid = 0;
    }

    // Constructor with ID (for read/update)
    public Patient(int patientId, String fullName, String nic, String phone,
                   String email, String address, String medicalHistory) {
        this.patientId = patientId;
        this.fullName = fullName;
        this.nic = nic;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.medicalHistory = medicalHistory;
        this.advancePaid = 0;
    }

    // ✅ Constructor with advance (optional future use)
    public Patient(int patientId, String fullName, String nic, String phone,
                   String email, String address, String medicalHistory, double advancePaid) {
        this.patientId = patientId;
        this.fullName = fullName;
        this.nic = nic;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.medicalHistory = medicalHistory;
        this.advancePaid = advancePaid;
    }

    // -----------------
    // Getters & Setters
    // -----------------

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    // ✅ Advance Payment
    public double getAdvancePaid() {
        return advancePaid;
    }

    public void setAdvancePaid(double advancePaid) {
        this.advancePaid = advancePaid;
    }
}
