# MedicarePlus (MediCare Plus) — Hospital Management System (Java Swing + SQLite)

A desktop-based Hospital / Clinic Management System built using **Java Swing** with a **SQLite** database.
It supports managing **Patients, Doctors, Appointments, Notifications, Billing**, and **Monthly Reports**.

## Tech Stack
- Java 17
- Maven
- Java Swing (GUI)
- SQLite (sqlite-jdbc)

## Features
- ✅ Patient Management (add/update/delete/search)
- ✅ Doctor Management
- ✅ Appointment Scheduling & status tracking
- ✅ Notifications (read/unread)
- ✅ Billing (total, advance used, payable amount)
- ✅ Monthly Reports
- ✅ Auto database table creation on startup

## Project Structure (Important Packages)
- `com.medicareplus.app` — Main entry point
- `com.medicareplus.ui` — Swing UI frames (Dashboard, Patients, Doctors, etc.)
- `com.medicareplus.dao` — Database access (CRUD)
- `com.medicareplus.model` — Models (Patient, Doctor, Appointment, etc.)
- `com.medicareplus.db` — DB connection + table initializer

## How Database Works
- The app uses SQLite with the URL:
  - `jdbc:sqlite:medicareplus.db`
- On startup, it automatically creates required tables using:
  - `DBInitializer.createTables()`

> Tip: The database file `medicareplus.db` is generated/used locally.  
> Usually you should NOT commit it to GitHub (add it to `.gitignore`).

## How to Run (Recommended: IntelliJ IDEA)
1. Open the project in IntelliJ (open the folder that contains `pom.xml`)
2. Make sure you have **JDK 17** selected:
   - `File → Project Structure → Project SDK → 17`
3. Wait until Maven dependencies finish downloading.
4. Run:
   - `src/main/java/com/medicareplus/app/Main.java`

## Build with Maven (Optional)


---

## ✅ Add a proper `.gitignore` (recommended)
Create a file named **`.gitignore`** in the project root and paste:

```gitignore
# IntelliJ / IDEA
.idea/
*.iml
out/

# Maven
target/

# OS files
.DS_Store
Thumbs.db

# Logs
*.log

# SQLite local DB (don’t upload your local data)
*.db
*.sqlite
*.sqlite3

```bash
mvn clean package
