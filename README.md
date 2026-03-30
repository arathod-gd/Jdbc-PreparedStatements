# JDBC CLI CRUD Demo

This project is a simple Java JDBC application that connects to PostgreSQL and lets you perform CLI-based CRUD operations on a `students` table.

## Features

- Create a student
- Read all students
- Update a student's email
- Delete a student
- Run locally with Maven
- Run in Docker with PostgreSQL

## Tech Stack

- Java 17
- Maven
- PostgreSQL
- Docker Compose

## Project Structure

- `src/main/java/org/databaseiep/Main.java`: CLI application and CRUD logic
- `src/main/java/org/databaseiep/sqlconnection/SqlConn.java`: JDBC connection setup
- `compose.yaml`: PostgreSQL and app container setup
- `Dockerfile`: multi-stage Docker build for the Java app
- `.env`: database configuration

## Environment Variables

The project reads database settings from `.env` or the system environment.

Current `.env` values:

```env
POSTGRES_HOST=localhost
POSTGRES_USER=user
POSTGRES_PASSWORD=password
POSTGRES_PORT=5433
POSTGRES_DB=jdbc_db
POSTGRES_URL=jdbc:postgresql://localhost:5433/jdbc_db
```

For Docker, `compose.yaml` overrides the app container to use `db:5432` internally, so you do not need to change `.env`.

## Run Locally

### 1. Start PostgreSQL with Docker

```bash
docker compose up -d db
```

### 2. Run the Java app

```bash
mvn exec:java -Dexec.mainClass=org.databaseiep.Main
```

If `mvn exec:java` is not available in your environment, you can use:

```bash
mvn -q -DskipTests package
java -jar target/jdbc-1.0-SNAPSHOT.jar
```

## Run with Docker

Use this flow for the interactive CLI:

### 1. Start PostgreSQL

```bash
docker compose up -d db
```

### 2. Run the app interactively

```bash
docker compose run --rm app
```

Do not use `docker compose up app` for the CLI. That mode mainly streams logs and is not reliable for interactive `Scanner(System.in)` input.

## How to Use the CLI

After the app starts, you will see:

```text
Choose an operation:
1. Create student
2. Read students
3. Update student email
4. Delete student
5. Exit
Enter choice:
```

### Create a student

```text
Enter choice: 1
Enter student name: Rahul
Enter student email: rahul@example.com
```

### Read students

```text
Enter choice: 2
```

### Update a student email

```text
Enter choice: 3
Enter student id to update: 1
Enter new email: rahul.new@example.com
```

### Delete a student

```text
Enter choice: 4
Enter student id to delete: 1
```

### Exit

```text
Enter choice: 5
```

## Example Session

```text
Enter choice: 1
Enter student name: Rahul
Enter student email: rahul@example.com

Enter choice: 2
Current students:
1 | Rahul | rahul@example.com

Enter choice: 3
Enter student id to update: 1
Enter new email: rahul.new@example.com

Enter choice: 2
Current students:
1 | Rahul | rahul.new@example.com

Enter choice: 4
Enter student id to delete: 1

Enter choice: 2
Current students:
No students found.

Enter choice: 5
```

## Stop the Project

Stop containers:

```bash
docker compose down
```

Stop containers and remove saved database data:

```bash
docker compose down -v
```

## Build Verification

Build the runnable jar:

```bash
mvn -q -DskipTests package
```

## Notes

- The app creates the `students` table automatically if it does not exist.
- The `email` field is unique, so inserting the same email twice will fail.
- The update operation currently changes only the student's email.
