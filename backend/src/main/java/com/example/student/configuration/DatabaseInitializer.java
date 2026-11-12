package com.example.student.configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

public class DatabaseInitializer
		implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	private static final Pattern POSTGRES_URL = Pattern.compile(
			"^jdbc:postgresql://([^/]+)(?:/([^?]+))?(\\?.*)?$");

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		Environment environment = applicationContext.getEnvironment();
		if (!environment.getProperty("app.database.auto-create", Boolean.class, true)) {
			return;
		}

		String url = environment.getProperty("spring.datasource.url");
		String username = environment.getProperty("spring.datasource.username");
		String password = environment.getProperty("spring.datasource.password");
		if (url == null || username == null || password == null) {
			throw new IllegalStateException(
					"Database auto-creation requires spring.datasource.url, username and password");
		}

		Matcher matcher = POSTGRES_URL.matcher(url);
		if (!matcher.matches() || matcher.group(2) == null || matcher.group(2).isBlank()) {
			throw new IllegalStateException(
					"Database auto-creation supports PostgreSQL URLs with a database name");
		}

		String hostAndPort = matcher.group(1);
		String databaseName = matcher.group(2);
		String query = matcher.group(3) == null ? "" : matcher.group(3);
		String maintenanceUrl = "jdbc:postgresql://" + hostAndPort + "/postgres" + query;

		try (Connection connection = DriverManager.getConnection(maintenanceUrl, username, password)) {
			if (databaseExists(connection, databaseName)) {
				return;
			}

			try (Statement statement = connection.createStatement()) {
				statement.execute("CREATE DATABASE " + quoteIdentifier(databaseName));
			}
		} catch (SQLException exception) {
			throw new IllegalStateException(
					"Could not create PostgreSQL database '" + databaseName
							+ "'. Check PostgreSQL availability and user permissions",
					exception);
		}
	}

	private boolean databaseExists(Connection connection, String databaseName) throws SQLException {
		try (var statement = connection.prepareStatement(
				"SELECT 1 FROM pg_database WHERE datname = ?")) {
			statement.setString(1, databaseName);
			try (ResultSet resultSet = statement.executeQuery()) {
				return resultSet.next();
			}
		}
	}

	private String quoteIdentifier(String identifier) {
		return "\"" + identifier.replace("\"", "\"\"") + "\"";
	}
}
