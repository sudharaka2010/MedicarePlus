# MedicarePlus

MedicarePlus is a desktop hospital management system built with Java Swing and SQLite. It provides a simple dashboard for managing patients, doctors, appointments, notifications, billing, and monthly reports.

## Features

- Patient records with contact details, medical history, and advance payments
- Doctor records with specialty, contact details, and availability
- Appointment scheduling with patient and doctor assignments
- Notification management with read/unread status
- Billing with total amount, advance usage, payable amount, and notes
- Monthly report generation
- Automatic SQLite table creation on application startup

## Tech Stack

- Java 17
- Maven
- Java Swing
- SQLite
- Xerial SQLite JDBC driver

## Project Structure

```text
src/main/java/com/medicareplus
|-- app        # Application entry point
|-- dao        # Database access classes
|-- db         # SQLite connection and schema initialization
|-- model      # Domain models
|-- service    # Business/reporting services
`-- ui         # Swing screens and dashboard

src/main/resources
|-- icons      # UI icons
`-- images     # Logo and dashboard images
```

## Database

The application uses a local SQLite database:

```text
jdbc:sqlite:medicareplus.db
```

When the app starts, `DBInitializer.createTables()` creates the required tables if they do not already exist:

- `patients`
- `doctors`
- `appointments`
- `notifications`
- `bills`

The repository currently includes `medicareplus.db`. For real deployments, avoid committing local database files that may contain private patient, billing, or operational data.

## Requirements

- JDK 17 or later
- Maven 3.8 or later

Check your local versions:

```bash
java -version
mvn -version
```

## Run the Application

### Option 1: IntelliJ IDEA

1. Open the project folder that contains `pom.xml`.
2. Select JDK 17 in the project settings.
3. Wait for Maven dependencies to download.
4. Run `src/main/java/com/medicareplus/app/Main.java`.

### Option 2: Maven Command

Compile the project:

```bash
mvn clean compile
```

Run the main class:

```bash
mvn exec:java -Dexec.mainClass="com.medicareplus.app.Main"
```

If the `exec-maven-plugin` is not configured in your Maven environment, run the app from your IDE or add the plugin before using the command above.

## Build

Create the Maven build output:

```bash
mvn clean package
```

The compiled output is generated in the `target/` directory.

## Git Notes

The `.gitignore` file already excludes common IDE, build, and operating system files such as:

- `target/`
- IntelliJ, Eclipse, NetBeans, and VS Code metadata
- macOS `.DS_Store`

Consider adding local SQLite database patterns before using this project with real data:

```gitignore
*.db
*.sqlite
*.sqlite3
```

## Entry Point

```text
com.medicareplus.app.Main
```

Startup flow:

1. Apply the system look and feel.
2. Initialize the SQLite schema.
3. Open the dashboard window.
