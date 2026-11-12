package com.example.student.configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DatabaseInitializerTest {

    private final DatabaseInitializer initializer = new DatabaseInitializer();

    @Test
    void skipsCreationWhenAutoCreateIsDisabled() {
        ConfigurableApplicationContext context = contextWithProperties(false, null, null, null);

        initializer.initialize(context);
    }

    @Test
    void rejectsMissingDatabaseProperties() {
        ConfigurableApplicationContext context = contextWithProperties(true, null, "user", "password");

        assertThatThrownBy(() -> initializer.initialize(context))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("requires");
    }

    @Test
    void rejectsNonPostgresUrlWithoutDatabaseName() {
        ConfigurableApplicationContext context = contextWithProperties(
                true, "jdbc:h2:mem:test", "user", "password");

        assertThatThrownBy(() -> initializer.initialize(context))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PostgreSQL URLs");
    }

    @Test
    void leavesExistingDatabaseUnchanged() throws SQLException {
        ConfigurableApplicationContext context = contextWithProperties(
                true, "jdbc:postgresql://db.example:5432/student?sslmode=disable", "user", "password");
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        try (MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class)) {
            driverManager.when(() -> DriverManager.getConnection(
                    "jdbc:postgresql://db.example:5432/postgres?sslmode=disable", "user", "password"))
                    .thenReturn(connection);

            initializer.initialize(context);
        }

        verify(connection).prepareStatement("SELECT 1 FROM pg_database WHERE datname = ?");
    }

    @Test
    void createsMissingDatabaseAndQuotesItsName() throws SQLException {
        ConfigurableApplicationContext context = contextWithProperties(
                true, "jdbc:postgresql://localhost/student\"archive", "user", "password");
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        Statement statement = mock(Statement.class);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        when(connection.createStatement()).thenReturn(statement);

        try (MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class)) {
            driverManager.when(() -> DriverManager.getConnection(
                    "jdbc:postgresql://localhost/postgres", "user", "password"))
                    .thenReturn(connection);

            initializer.initialize(context);
        }

        verify(statement).execute("CREATE DATABASE \"student\"\"archive\"");
    }

    @Test
    void wrapsConnectionErrors() throws SQLException {
        ConfigurableApplicationContext context = contextWithProperties(
                true, "jdbc:postgresql://localhost/student", "user", "password");
        SQLException connectionException = new SQLException("connection refused");

        try (MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class)) {
            driverManager.when(() -> DriverManager.getConnection(
                    "jdbc:postgresql://localhost/postgres", "user", "password"))
                    .thenThrow(connectionException);

            assertThatThrownBy(() -> initializer.initialize(context))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Could not create PostgreSQL database 'student'")
                    .hasCauseInstanceOf(SQLException.class);
        }
    }

    private ConfigurableApplicationContext contextWithProperties(
            boolean autoCreate, String url, String username, String password) {
        ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
        ConfigurableEnvironment environment = mock(ConfigurableEnvironment.class);
        when(context.getEnvironment()).thenReturn(environment);
        when(environment.getProperty("app.database.auto-create", Boolean.class, true))
                .thenReturn(autoCreate);
        when(environment.getProperty("spring.datasource.url")).thenReturn(url);
        when(environment.getProperty("spring.datasource.username")).thenReturn(username);
        when(environment.getProperty("spring.datasource.password")).thenReturn(password);
        return context;
    }
}
