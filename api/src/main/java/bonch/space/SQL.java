package bonch.space;

import java.sql.*;

/**
 * SQL
 */
public class SQL {
	public static Connection connection = null;

	// Ленивое подключение к базе данных и инициализация таблиц
	static {
		String url = "jdbc:postgresql://localhost:5432/skb?currentSchema=public";
		String user = "bonch_space";
		String password = "bonchspace";

		try (Connection conn = DriverManager.getConnection(url, user, password)) {
			if (conn != null) {
				System.out.println("Connected to the database!");
				connection = conn;
				System.out.println("Current schema: " + conn.getSchema());
				initSQLTables();
			} else {
				System.out.println("Failed to make connection!");
			}
		} catch (SQLException e) {
			System.err.println("Connection error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static void initSQLTables() {
		String createTagsTable = """
				    CREATE TABLE IF NOT EXISTS public.tags (
				        id UUID PRIMARY KEY,
				        title TEXT UNIQUE NOT NULL
				    );
				""";
		try (Statement statement = connection.createStatement()) {
			statement.execute(createTagsTable);
			System.out.println("Table tags initialisated");
		} catch (SQLException e) {
			System.err.println("Error creating tables: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void test() {
		System.out.println("IM READY!");
	}

}
