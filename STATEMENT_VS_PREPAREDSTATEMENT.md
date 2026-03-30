# Statement vs PreparedStatement

## Overview

Both `Statement` and `PreparedStatement` are JDBC APIs used to send SQL to the database, but they are not equivalent.

## Statement

`Statement` is used when you build the SQL string directly in Java code.

Example:

```java
String sql = "SELECT id FROM login_demo_users WHERE username = '" + username
        + "' AND password = '" + password + "'";
Statement statement = connection.createStatement();
ResultSet resultSet = statement.executeQuery(sql);
```

### Characteristics

- SQL is created by concatenating strings
- User input becomes part of the SQL text
- It is easier to write insecure code
- It is vulnerable to SQL injection when untrusted input is concatenated
- It is usually less efficient for repeated execution of the same query pattern

## PreparedStatement

`PreparedStatement` uses placeholders (`?`) and sends user input separately from the SQL command.

Example:

```java
String sql = "SELECT id FROM login_demo_users WHERE username = ? AND password = ?";
PreparedStatement preparedStatement = connection.prepareStatement(sql);
preparedStatement.setString(1, username);
preparedStatement.setString(2, password);
ResultSet resultSet = preparedStatement.executeQuery();
```

### Characteristics

- SQL structure is fixed first
- Values are bound separately
- Safer for user input
- Protects against SQL injection in normal parameter usage
- Better choice for repeated queries with different values

## Main Difference

The core difference is how user input is handled:

- `Statement`: input is merged into the SQL string
- `PreparedStatement`: input is treated as data, not executable SQL

That is why `PreparedStatement` is the default choice when a query depends on user input.

## SQL Injection Example

This repository contains a simple login demo using the table `login_demo_users`.

Seeded user:

- username: `admin`
- password: `admin123`

Unsafe `Statement` query:

```sql
SELECT id
FROM login_demo_users
WHERE username = 'does-not-matter'
  AND password = '' OR '1'='1'
```

Because `'1'='1'` is always true, the query can return rows even when the credentials are fake.

Safe `PreparedStatement` query:

```sql
SELECT id
FROM login_demo_users
WHERE username = ?
  AND password = ?
```

In this version, the payload is treated as a plain string value, so the attack does not change the meaning of the SQL.

## How This Project Demonstrates It

The CLI includes a menu option:

- `5. Run SQL injection demo`

When selected, the app tests four cases:

1. `Statement` with valid credentials
2. `Statement` with injected credentials
3. `PreparedStatement` with valid credentials
4. `PreparedStatement` with injected credentials

Expected result:

- `Statement` with valid credentials: `true`
- `Statement` with injected credentials: `true`
- `PreparedStatement` with valid credentials: `true`
- `PreparedStatement` with injected credentials: `false`

## Connection Setup

This project uses `DriverManager` to create the JDBC connection:

```java
String url = "jdbc:postgresql://" + host + ":" + port + "/" + db;
Connection connection = DriverManager.getConnection(url, user, password);
```

That logic is implemented in `src/main/java/org/databaseiep/sqlconnection/SqlConn.java`.

## How to Run the Demo with Docker Database

Start PostgreSQL in Docker:

```bash
docker compose up -d db
```

Run the app locally against the Docker database:

```bash
mvn exec:java -Dexec.mainClass=org.databaseiep.Main
```

Or run the app in Docker:

```bash
docker compose run --rm app
```

Then choose:

```text
5
```

## Conclusion

Use `PreparedStatement` for any query that accepts user input. `Statement` is acceptable only when the SQL is fully fixed and no untrusted data is concatenated into it.
