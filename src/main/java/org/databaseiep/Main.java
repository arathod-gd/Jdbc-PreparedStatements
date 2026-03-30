package org.databaseiep;

import org.databaseiep.sqlconnection.SqlConn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Application started - JDBC CLI");

        SqlConn conn = new SqlConn();
        try (Connection connection = conn.getConnection()) {
            createTable(connection);
            createLoginDemoTable(connection);
            runCli(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Application terminated....");
    }

    private static void runCli(Connection connection) throws SQLException {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;

            while (running) {
                printMenu();
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1" -> handleCreate(connection, scanner);
                    case "2" -> readStudents(connection);
                    case "3" -> handleUpdate(connection, scanner);
                    case "4" -> handleDelete(connection, scanner);
                    case "5" -> runSqlInjectionDemo(connection);
                    case "6" -> {
                        running = false;
                        System.out.println("Exiting CLI...");
                    }
                    default -> System.out.println("Invalid option. Choose 1 to 6.");
                }
            }
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("Choose an operation:");
        System.out.println("1. Create student");
        System.out.println("2. Read students");
        System.out.println("3. Update student email");
        System.out.println("4. Delete student");
        System.out.println("5. Run SQL injection demo");
        System.out.println("6. Exit");
        System.out.print("Enter choice: ");
    }

    private static void handleCreate(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Enter student name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter student email: ");
        String email = scanner.nextLine().trim();

        if (name.isBlank() || email.isBlank()) {
            System.out.println("Name and email are required.");
            return;
        }

        createStudent(connection, name, email);
    }

    private static void handleUpdate(Connection connection, Scanner scanner) throws SQLException {
        Integer id = readStudentId(scanner, "Enter student id to update: ");
        if (id == null) {
            return;
        }

        System.out.print("Enter new email: ");
        String email = scanner.nextLine().trim();
        if (email.isBlank()) {
            System.out.println("Email is required.");
            return;
        }

        updateStudent(connection, id, email);
    }

    private static void handleDelete(Connection connection, Scanner scanner) throws SQLException {
        Integer id = readStudentId(scanner, "Enter student id to delete: ");
        if (id == null) {
            return;
        }

        deleteStudent(connection, id);
    }

    private static Integer readStudentId(Scanner scanner, String prompt) {
        System.out.print(prompt);
        String value = scanner.nextLine().trim();

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.out.println("Student id must be a number.");
            return null;
        }
    }

    private static void createTable(Connection connection) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS students (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    email VARCHAR(150) NOT NULL UNIQUE
                )
                """;

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
            System.out.println("students table is ready");
        }
    }

    private static void createLoginDemoTable(Connection connection) throws SQLException {
        String createTableSql = """
                CREATE TABLE IF NOT EXISTS login_demo_users (
                    id SERIAL PRIMARY KEY,
                    username VARCHAR(100) NOT NULL UNIQUE,
                    password VARCHAR(100) NOT NULL
                )
                """;
        String seedSql = """
                INSERT INTO login_demo_users (username, password)
                VALUES ('admin', 'admin123')
                ON CONFLICT (username) DO NOTHING
                """;

        try (Statement statement = connection.createStatement()) {
            statement.execute(createTableSql);
            statement.executeUpdate(seedSql);
            System.out.println("login_demo_users table is ready");
        }
    }

    private static void createStudent(Connection connection, String name, String email) throws SQLException {
        String sql = "INSERT INTO students (name, email) VALUES (?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, email);
            preparedStatement.executeUpdate();
            System.out.println("Student inserted");
        }
    }

    private static void readStudents(Connection connection) throws SQLException {
        String sql = "SELECT id, name, email FROM students ORDER BY id";

        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)
        ) {
            System.out.println("Current students:");
            boolean hasRows = false;
            while (resultSet.next()) {
                hasRows = true;
                System.out.println(
                        resultSet.getInt("id") + " | "
                                + resultSet.getString("name") + " | "
                                + resultSet.getString("email")
                );
            }
            if (!hasRows) {
                System.out.println("No students found.");
            }
        }
    }

    private static void updateStudent(Connection connection, int id, String email) throws SQLException {
        String sql = "UPDATE students SET email = ? WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, email);
            preparedStatement.setInt(2, id);
            int updatedRows = preparedStatement.executeUpdate();
            System.out.println("Updated rows: " + updatedRows);
        }
    }

    private static void deleteStudent(Connection connection, int id) throws SQLException {
        String sql = "DELETE FROM students WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            int deletedRows = preparedStatement.executeUpdate();
            System.out.println("Deleted rows: " + deletedRows);
        }
    }

    private static void runSqlInjectionDemo(Connection connection) throws SQLException {
        String safeUsername = "admin";
        String safePassword = "admin123";
        String attackerUsername = "does-not-matter";
        String attackerPassword = "' OR '1'='1";

        System.out.println();
        System.out.println("SQL injection demo");
        System.out.println("Seeded login user: admin / admin123");
        System.out.println("Attack payload password: " + attackerPassword);
        System.out.println();

        boolean validStatementLogin = loginWithStatement(connection, safeUsername, safePassword);
        boolean injectedStatementLogin = loginWithStatement(connection, attackerUsername, attackerPassword);
        boolean validPreparedLogin = loginWithPreparedStatement(connection, safeUsername, safePassword);
        boolean injectedPreparedLogin = loginWithPreparedStatement(connection, attackerUsername, attackerPassword);

        System.out.println("Statement with valid credentials: " + validStatementLogin);
        System.out.println("Statement with injected credentials: " + injectedStatementLogin);
        System.out.println("PreparedStatement with valid credentials: " + validPreparedLogin);
        System.out.println("PreparedStatement with injected credentials: " + injectedPreparedLogin);
    }

    private static boolean loginWithStatement(Connection connection, String username, String password) throws SQLException {
        String sql = "SELECT id FROM login_demo_users WHERE username = '"
                + username
                + "' AND password = '"
                + password
                + "'";

        System.out.println("Statement SQL: " + sql);

        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)
        ) {
            return resultSet.next();
        }
    }

    private static boolean loginWithPreparedStatement(Connection connection, String username, String password) throws SQLException {
        String sql = "SELECT id FROM login_demo_users WHERE username = ? AND password = ?";

        System.out.println("PreparedStatement SQL template: " + sql);

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
}
