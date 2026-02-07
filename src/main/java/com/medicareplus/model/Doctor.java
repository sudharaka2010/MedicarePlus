package com.medicareplus.model;

public class Doctor {

    private int doctorId;
    private String fullName;
    private String specialty;
    private String phone;
    private String email;
    private String availableDays;
    private String availableTime;

    // No-arg constructor
    public Doctor() {
    }

    // Constructor without ID (for insert)
    public Doctor(String fullName, String specialty, String phone,
                  String email, String availableDays, String availableTime) {
        this.fullName = fullName;
        this.specialty = specialty;
        this.phone = phone;
        this.email = email;
        this.availableDays = availableDays;
        this.availableTime = availableTime;
    }

    // Constructor with ID (for read/update)
    public Doctor(int doctorId, String fullName, String specialty, String phone,
                  String email, String availableDays, String availableTime) {
        this.doctorId = doctorId;
        this.fullName = fullName;
        this.specialty = specialty;
        this.phone = phone;
        this.email = email;
        this.availableDays = availableDays;
        this.availableTime = availableTime;
    }

    // Getters and setters
    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
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

    public String getAvailableDays() {
        return availableDays;
    }

    public void setAvailableDays(String availableDays) {
        this.availableDays = availableDays;
    }

    public String getAvailableTime() {
        return availableTime;
    }

    public void setAvailableTime(String availableTime) {
        this.availableTime = availableTime;
    }
}
