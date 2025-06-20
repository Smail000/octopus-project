package bonch.space;

import java.sql.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

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

		try {
			Connection conn = DriverManager.getConnection(url, user, password);
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

	public static ArrayList<Map<String, String>> getAllTags() {
		String query = "SELECT * FROM public.tags";
		ArrayList<Map<String, String>> result = new ArrayList<Map<String, String>>();
		try (Statement tStatement = connection.createStatement()) {
			ResultSet rSet = tStatement.executeQuery(query);
			while (rSet.next()) {
				String id = rSet.getString("id");
				String title = rSet.getString("title");
				result.add(Map.of("title", title, "id", id));
			}

		} catch (Exception e) {
			System.err.println("Error while parsing the table Tags: " + e.toString());
			e.printStackTrace();
		}
		return result;

	}

	public static void addTag(String title) {
		String query = "INSERT INTO tags(id,title) VALUES (?,?)";
		while (true) {
			try (PreparedStatement statement = connection.prepareStatement(query)) {
				UUID id = UUID.randomUUID();
				statement.setObject(1, id);
				statement.setString(2, title);
				int affectedRows = statement.executeUpdate();
				if (affectedRows > 0) {
					System.out.println("Tag \"" + title + "\" added");
					return;
				}

			} catch (SQLException e) {
				if (e.getSQLState() != null && e.getSQLState().equals("23505")) {
					continue;
				} else {
					System.err.println("Ошибка добавления тега: " + e.getMessage());
					e.printStackTrace();
					return;
				}
			}
			break;
		}
	}

	// TODO: Удалить это в релизе.
	public static void test() {
		System.out.println("IM READY!");
	}

}
