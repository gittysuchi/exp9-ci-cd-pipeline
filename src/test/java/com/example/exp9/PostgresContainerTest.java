package com.example.exp9;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers(disabledWithoutDocker = true)
class PostgresContainerTest {
    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("exp9db")
                    .withUsername("testuser")
                    .withPassword("testpass");

    @Test
    void canCreateAndQueryTableInRealPostgresContainer() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE ci_check (id INT PRIMARY KEY, label VARCHAR(30))");
            statement.execute("INSERT INTO ci_check VALUES (1, 'Testcontainers works')");
            try (ResultSet result = statement.executeQuery("SELECT label FROM ci_check WHERE id = 1")) {
                result.next();
                assertEquals("Testcontainers works", result.getString("label"));
            }
        }
    }
}
