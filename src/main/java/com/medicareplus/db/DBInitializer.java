package com.medicareplus.db;

import java.sql.Connection;
import java.sql.Statement;

public class DBInitializer {

    public static void createTables() {

        String createPatientsTable = """
               
                CREATE TABLE IF NOT EXISTS patients (
                   patient_id INTEGER PRIMARY KEY AUTOINCREMENT,
                   full_name TEXT NOT NULL,
                   nic TEXT,
                   phone TEXT,
                   email TEXT,
                   address TEXT,
                   medical_history TEXT,
                   advance_paid REAL DEFAULT 0,   -- ✅ NEW COLUMN
                   created_at TEXT DEFAULT CURRENT_TIMESTAMP
               );
               
                """;

        String createDoctorsTable = """
                CREATE TABLE IF NOT EXISTS doctors (
                    doctor_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    full_name TEXT NOT NULL,
                    specialty TEXT NOT NULL,
                    phone TEXT,
                    email TEXT,
                    available_days TEXT,
                    available_time TEXT,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                );
                """;

        String createAppointmentsTable = """
                CREATE TABLE IF NOT EXISTS appointments (
                    appointment_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    patient_id INTEGER NOT NULL,
                    doctor_id INTEGER NOT NULL,
                    appointment_date TEXT NOT NULL,
                    appointment_time TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'Scheduled',
                    notes TEXT,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
                    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id)
                );
                """;

        String createNotificationsTable = """
                CREATE TABLE IF NOT EXISTS notifications (
                    notification_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    receiver_type TEXT NOT NULL,
                    receiver_id INTEGER NOT NULL,
                    message TEXT NOT NULL,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    is_read INTEGER DEFAULT 0
                );
                """;

        // ✅ NEW: Bills table
        String createBillsTable = """
                CREATE TABLE IF NOT EXISTS bills (
                    bill_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    patient_id INTEGER NOT NULL,
                    bill_date TEXT DEFAULT CURRENT_TIMESTAMP,
                    total_amount REAL NOT NULL,
                    advance_used REAL DEFAULT 0,
                    payable_amount REAL NOT NULL,
                    notes TEXT,
                    FOREIGN KEY (patient_id) REFERENCES patients(patient_id)
                );
                """;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Create tables
            stmt.execute(createPatientsTable);
            System.out.println("Patients table ready");

            stmt.execute(createDoctorsTable);
            System.out.println("Doctors table ready");

            stmt.execute(createAppointmentsTable);
            System.out.println("Appointments table ready");

            stmt.execute(createNotificationsTable);
            System.out.println("Notifications table ready");

            stmt.execute(createBillsTable);
            System.out.println("Bills table ready");

            // ✅ Upgrade: add advance_paid column to patients (safe)
            try {
                stmt.execute("ALTER TABLE patients ADD COLUMN advance_paid REAL DEFAULT 0;");
                System.out.println("patients.advance_paid column added");
            } catch (Exception ignored) {
                // Column already exists, ignore
                System.out.println("patients.advance_paid already exists");
            }

        } catch (Exception e) {
            System.out.println("Table creation failed");
            e.printStackTrace();
        }
    }
}
