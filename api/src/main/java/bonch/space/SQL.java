package bonch.space;

import java.lang.reflect.Field;
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
		System.out.println("[INFO] Starting connecting to the database!");

		try {
			Connection conn = DriverManager.getConnection(url, user, password);
			if (conn != null) {
				System.out.println("[INFO] Connected to the database!");
				connection = conn;
				System.out.println("[INFO] Current schema: " + conn.getSchema());
				initSQLTables();
			} else {
				System.out.println("[ERROR] Failed to make connection!");
			}
		} catch (SQLException e) {
			System.err.println("[ERROR] Connection error: " + e.getMessage());
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
		String createUsersTable = """
				CREATE TABLE IF NOT EXISTS public.users (
					login TEXT UNIQUE NOT NULL,
					password TEXT NOT NULL,
					name TEXT NOT NULL,
					description TEXT,
					shortDescription TEXT,
					isAdmin BOOLEAN NOT NULL,
					isHiden BOOLEAN NOT NULL,
					priority INT NOT NULL,
					id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
					projects UUID[],
					token UUID UNIQUE NOT NULL,
					photo TEXT
				);
				""";
		String createBidTable = """
				CREATE TABLE IF NOT EXISTS public.bid (
					id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
					date TIMESTAMP NOT NULL,
					name TEXT,
					text TEXT NOT NULL,
					tag INT NOT NULL,
					contact TEXT
				);
				""";
		String createKeyTable = """
				CREATE TABLE IF NOT EXISTS public.key (
									key TEXT PRIMARY KEY,
									date TIMESTAMP NOT NULL
						);
								""";
		String createPostsTable = """
				CREATE TABLE IF NOT EXISTS public.posts (
						title TEXT NOT NULL,
						id UUID PRIMARY KEY,
						content TEXT NOT NULL,
						date TIMESTAMP NOT NULL,
						author INT NOT NULL,
						tags UUID[]
						);
					""";
		String createProjectsTable = """
				CREATE TABLE IF NOT EXISTS public.projects (
					title TEXT NOT NULL,
					shortTitle TEXT NOT NULL,
					id UUID PRIMARY KEY,
					description TEXT NOT NULL
					);
					""";

		try (Statement statement = connection.createStatement()) {
			statement.execute(createTagsTable);
			System.out.println("[INFO] Table tags initialisated");
			statement.execute(createBidTable);
			System.out.println("[INFO] Table bid initialistaed");
			statement.execute(createKeyTable);
			System.out.println("[INFO] Table key initialisated");
			statement.execute(createPostsTable);
			System.out.println("[INFO] Table posts initialisated");
			statement.execute(createUsersTable);
			System.out.println("[INFO] Table users initialisated");
			statement.execute(createProjectsTable);
			System.out.println("[INFO] Table projects initialisated");
		} catch (SQLException e) {
			System.err.println("[ERROR] Error creating tables: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// SelectAllTemplate
	// TODO: Сделать обратно приватным
	public static <T> ArrayList<T> Select(String tableName, Class<T> returnClass,
			Map<String, String> filters) {

		StringBuilder fieldsBuilder = new StringBuilder();
		Field[] fields = returnClass.getFields();
		for (int i = 0; i < fields.length; i++) {
			fieldsBuilder.append(fields[i].getName());
			if (i < fields.length - 1) {
				fieldsBuilder.append(", ");
			}
		}

		String fieldsString = fieldsBuilder.toString();
		StringBuilder filtersBuilder = new StringBuilder();
		if (filters != null && !filters.isEmpty()) {
			filtersBuilder.append(" WHERE ");
			for (Map.Entry<String, String> filter : filters.entrySet()) {
				filtersBuilder.append(filter.getKey() + " = " + filter.getValue() + ", ");
			}
			filtersBuilder.delete(filtersBuilder.length() - 2, filtersBuilder.length());
		}
		String filtersString = filtersBuilder.toString();

		String query = "SELECT " + fieldsString + " FROM " + tableName + filtersString;
		ArrayList<T> result = new ArrayList<T>();

		try (Statement statement = connection.createStatement()) {
			ResultSet rSet = statement.executeQuery(query);
			while (rSet.next()) {
				T resulElem = returnClass.getDeclaredConstructor().newInstance();
				for (int i = 0; i < fields.length; i++) {
					if (fields[i].getType() == String.class) {
						fields[i].set(resulElem, rSet.getString(fields[i].getName()));
					} else if (fields[i].getType() == int.class) {
						fields[i].set(resulElem, rSet.getInt(fields[i].getName()));
					} else if (fields[i].getType() == UUID.class) {
						fields[i].set(resulElem, rSet.getObject(fields[i].getName()));
					}

					else {
						throw new SQLException("А типа-то нет...");
					}
				}
				result.add(resulElem);
			}

		} catch (Exception e) {
			System.err.println("[ERROR] Ошибка поиска в таблице " + tableName + " : " + e.toString());
			e.printStackTrace();
			return null;
		}
		return result;

	}

	private static <T> ArrayList<T> Select(String tableName, Class<T> returnClass,
			Map<String, String> filters, String sortBy, int offset, int count) {

		StringBuilder fieldsBuilder = new StringBuilder();
		Field[] fields = returnClass.getFields();
		for (int i = 0; i < fields.length; i++) {
			fieldsBuilder.append(fields[i].getName());
			if (i < fields.length - 1) {
				fieldsBuilder.append(", ");
			}
		}

		String fieldsString = fieldsBuilder.toString();
		StringBuilder filtersBuilder = new StringBuilder();
		if (filters != null && !filters.isEmpty()) {
			filtersBuilder.append(" WHERE ");
			for (Map.Entry<String, String> filter : filters.entrySet()) {
				filtersBuilder.append(filter.getKey() + " = " + filter.getValue() + ", ");
			}
			filtersBuilder.delete(filtersBuilder.length() - 2, filtersBuilder.length());
		}
		String filtersString = filtersBuilder.toString();

		String query = "SELECT " + fieldsString + " FROM " + tableName + filtersString + " ORDER BY " + sortBy + " LIMIT "
				+ String.valueOf(count) + " OFFSET " + String.valueOf(offset);
		ArrayList<T> result = new ArrayList<T>();

		try (Statement statement = connection.createStatement()) {
			ResultSet rSet = statement.executeQuery(query);
			while (rSet.next()) {
				T resulElem = returnClass.getDeclaredConstructor().newInstance();
				for (int i = 0; i < fields.length; i++) {
					if (fields[i].getType() == String.class) {
						fields[i].set(resulElem, rSet.getString(fields[i].getName()));
					} else if (fields[i].getType() == int.class) {
						fields[i].set(resulElem, rSet.getInt(fields[i].getName()));
					} else if (fields[i].getType() == UUID.class) {
						fields[i].set(resulElem, rSet.getObject(fields[i].getName()));
					}

					else {
						throw new SQLException("А типа-то нет...");
					}
				}
				result.add(resulElem);
			}

		} catch (Exception e) {
			System.err.println("[ERROR] Ошибка поиска в таблице " + tableName + " : " + e.toString());
			e.printStackTrace();
			return null;
		}
		return result;

	}

	private static <T> boolean Update(String tableName, T newData,
			Map<String, String> filters) {

		StringBuilder fieldsBuilder = new StringBuilder();
		Field[] fields = newData.getClass().getFields();
		for (int i = 0; i < fields.length; i++) {
			fieldsBuilder.append(fields[i].getName() + " = ?");
			if (i < fields.length - 1) {
				fieldsBuilder.append(", ");
			}
		}

		String fieldsString = fieldsBuilder.toString();
		StringBuilder filtersBuilder = new StringBuilder();
		if (filters != null && !filters.isEmpty()) {
			filtersBuilder.append(" WHERE ");
			for (Map.Entry<String, String> filter : filters.entrySet()) {
				filtersBuilder.append(filter.getKey() + " = " + filter.getValue() + ", ");
			}
			filtersBuilder.delete(filtersBuilder.length() - 2, filtersBuilder.length());
		}
		String filtersString = filtersBuilder.toString();

		String query = "UPDATE " + tableName + " SET " + fieldsString + filtersString;

		try (PreparedStatement statement = connection.prepareStatement(query)) {
			for (int i = 0; i < fields.length; i++) {
				if (fields[i].getType() == String.class) {
					statement.setString(i + 1, (String) fields[i].get(newData));
				} else if (fields[i].getType() == int.class) {
					statement.setInt(i + 1, (int) fields[i].get(newData));
				} else if (fields[i].getType() == UUID.class) {
					statement.setObject(i + 1, (UUID) fields[i].get(newData));
				}

				else {
					throw new SQLException("А типа-то нет...");
				}
			}
			statement.executeUpdate();

		} catch (Exception e) {
			System.err.println("[ERROR] Ошибка обновления в таблице " + tableName + " : " + e.toString());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static <T> boolean Insert(String tableName, T newData) {

		StringBuilder fieldsBuilder = new StringBuilder();
		StringBuilder valuesBuilder = new StringBuilder();

		valuesBuilder.append("(");
		Field[] fields = newData.getClass().getFields();
		for (int i = 0; i < fields.length; i++) {
			fieldsBuilder.append(fields[i].getName() + " = ?");
			valuesBuilder.append(" ?");
			if (i < fields.length - 1) {
				fieldsBuilder.append(", ");
				valuesBuilder.append(", ");
			}
		}
		valuesBuilder.append(")");

		String fieldsString = fieldsBuilder.toString();
		String valuesString = valuesBuilder.toString();

		StringBuilder filtersBuilder = new StringBuilder();

		String query = "INSERT INTO " + tableName + fieldsString + " VALUES " + valuesString;

		try (PreparedStatement statement = connection.prepareStatement(query)) {
			for (int i = 0; i < fields.length; i++) {
				if (fields[i].getType() == String.class) {
					statement.setString(i + 1, (String) fields[i].get(newData));
				} else if (fields[i].getType() == int.class) {
					statement.setInt(i + 1, (int) fields[i].get(newData));
				} else if (fields[i].getType() == UUID.class) {
					statement.setObject(i + 1, (UUID) fields[i].get(newData));
				}

				else {
					throw new SQLException("А типа-то нет...");
				}
			}
			statement.executeUpdate();

		} catch (Exception e) {
			System.err.println("[ERROR] Ошибка добавления в таблицу " + tableName + " : " + e.toString());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static boolean Delete(String tableName,
			Map<String, String> filters) {

		StringBuilder filtersBuilder = new StringBuilder();
		if (filters != null && !filters.isEmpty()) {
			filtersBuilder.append(" WHERE ");
			for (Map.Entry<String, String> filter : filters.entrySet()) {
				filtersBuilder.append(filter.getKey() + " = " + filter.getValue() + ", ");
			}
			filtersBuilder.delete(filtersBuilder.length() - 2, filtersBuilder.length());
		}
		String filtersString = filtersBuilder.toString();

		String query = "DELETE " + tableName + filtersString;

		try (PreparedStatement statement = connection.prepareStatement(query)) {

			statement.executeUpdate();

		} catch (

		Exception e) {
			System.err.println("[ERROR] Ошибка обновления в таблице " + tableName + " : " + e.toString());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static ArrayList<Map<String, String>> getAllTags() {
		String query = "SELECT * FROM public.tags";
		ArrayList<Map<String, String>> result = new ArrayList<Map<String, String>>();
		try (Statement statement = connection.createStatement()) {
			ResultSet rSet = statement.executeQuery(query);
			while (rSet.next()) {
				String id = rSet.getString("id");
				String title = rSet.getString("title");
				result.add(Map.of("title", title, "id", id));
			}

		} catch (Exception e) {
			System.err.println("[ERROR] Ошибка поиска тега: " + e.toString());
			e.printStackTrace();
		}
		return result;

	}

	public static String addTag(String title) {
		String query = "INSERT INTO tags(id,title) VALUES (?,?)";
		while (true) {
			UUID id = UUID.randomUUID();
			try (PreparedStatement statement = connection.prepareStatement(query)) {
				statement.setObject(1, id);
				statement.setString(2, title);
				int affectedRows = statement.executeUpdate();
				if (affectedRows > 0) {
					System.out.println("Tag \"" + title + "\" added");
				}

			} catch (SQLException e) {
				if (e.getSQLState() != null && e.getSQLState().equals("23505")) {
					continue;
				} else {
					System.err.println("[ERROR] Ошибка добавления тега: " + e.getMessage());
					e.printStackTrace();
					return null;
				}
			}
			return id.toString();
		}
	}

	public static boolean changeTag(String id, String newTitle) {
		// Возвращает true если выполнен коректно
		String query1 = "SELECT title FROM tags WHERE id = ?";
		String query2 = "UPDATE tags SET title = ? WHERE id = ?";
		try (PreparedStatement statement1 = connection.prepareStatement(query1);
				PreparedStatement statement2 = connection.prepareStatement(query2)) {
			statement1.setObject(1, UUID.fromString(id));
			ResultSet resultSet = statement1.executeQuery();
			if (!resultSet.next()) {
				throw new SQLException("Запись с id \"" + id + "\" не найдена.");
			}
			statement2.setObject(2, UUID.fromString(id));
			statement2.setString(1, newTitle);
			int affectedRows = statement2.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Не удалось обновить тэг.");
			}
		} catch (SQLException e) {
			System.err.println("[ERROR] Ошибка обновления тега: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean deleteTag(String id) {
		// Возвращает true если выполнен коректно
		String query1 = "SELECT title FROM tags WHERE id = ?";
		String query2 = "DELETE FROM tags WHERE id = ?";
		try (PreparedStatement statement1 = connection.prepareStatement(query1);
				PreparedStatement statement2 = connection.prepareStatement(query2)) {
			statement1.setObject(1, UUID.fromString(id));
			ResultSet resultSet = statement1.executeQuery();
			if (!resultSet.next()) {
				throw new SQLException("Запись с id \"" + id + "\" не найдена.");
			}
			statement2.setObject(1, UUID.fromString(id));
			int affectedRows = statement2.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Не удалось удалить тэг.");
			}
		} catch (SQLException e) {
			System.err.println("[ERROR] Ошибка удаления тега: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static ArrayList<Map<String, String>> getAllProjectShort() {
		String query = "SELECT title, shortTitle, id FROM public.projects";
		ArrayList<Map<String, String>> result = new ArrayList<Map<String, String>>();
		try (Statement statement = connection.createStatement()) {
			ResultSet rSet = statement.executeQuery(query);
			while (rSet.next()) {
				String id = rSet.getString("id");
				String title = rSet.getString("title");
				String shortTitle = rSet.getString("shortTitle");
				result.add(Map.of("title", title, "id", id, "shortTitle", shortTitle));
			}

		} catch (Exception e) {
			System.err.println("[ERROR] Error while parsing the table shortTitle: " + e.toString());
			e.printStackTrace();
		}
		return result;

	}

	public static Map<String, String> getProject(String id) {
		String query = "SELECT title, shortTitle, description FROM projects WHERE id = ?";
		Map<String, String> result;
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setObject(1, UUID.fromString(id));
			ResultSet resultSet = statement.executeQuery();
			if (!resultSet.next()) {
				throw new SQLException("Не удалось найти проект.");
			}
			result = Map.of("title", resultSet.getString("title"), "shortTitle", resultSet.getString("shortTitle"),
					"description",
					resultSet.getString("description"));

		} catch (SQLException e) {
			System.err.println("[ERROR] Ошибка поиска проекта: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
		return result;

	}

	public static String addProject(String title, String shortTitle, String description) {
		String query = "INSERT INTO projects(id,title,shortTitle,description) VALUES (?,?,?,?)";
		while (true) {
			UUID id;
			try (PreparedStatement statement = connection.prepareStatement(query)) {
				id = UUID.randomUUID();
				statement.setObject(1, id);
				statement.setString(2, title);
				statement.setString(3, shortTitle);
				statement.setString(4, description);
				int affectedRows = statement.executeUpdate();
				if (affectedRows > 0) {
					System.out.println("Project \"" + title + "\" added");
				} else {
					throw new SQLException("Не удалось добавить проект");
				}

			} catch (SQLException e) {
				if (e.getSQLState() != null && e.getSQLState().equals("23505")) {
					continue;
				} else {
					System.err.println("[ERROR] Ошибка добавления проекта: " + e.getMessage());
					e.printStackTrace();
					return null;
				}
			}
			return id.toString();

		}
	}

	public static boolean changeProject(String id, String newTitle, String newShortTitle, String newDescription) {
		// Возвращает true если выполнен коректно
		String query1 = "SELECT title FROM projects WHERE id = ?";
		String query2 = "UPDATE projects SET title = ?, shortTitle = ?, description = ? WHERE id = ?";
		try (PreparedStatement statement1 = connection.prepareStatement(query1);
				PreparedStatement statement2 = connection.prepareStatement(query2)) {
			statement1.setObject(1, UUID.fromString(id));
			ResultSet resultSet = statement1.executeQuery();
			if (!resultSet.next()) {
				throw new SQLException("Запись с id \"" + id + "\" не найдена.");
			}
			statement2.setString(1, newTitle);
			statement2.setString(2, newShortTitle);
			statement2.setString(3, newDescription);
			statement2.setObject(4, UUID.fromString(id));
			int affectedRows = statement2.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Не удалось обновить проект.");
			}
		} catch (SQLException e) {
			System.err.println("[ERROR] Ошибка обновления проекта: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean deleteProject(String id) {
		// Возвращает true если выполнен коректно
		String query1 = "SELECT title FROM projects WHERE id = ?";
		String query2 = "DELETE FROM projects WHERE id = ?";
		try (PreparedStatement statement1 = connection.prepareStatement(query1);
				PreparedStatement statement2 = connection.prepareStatement(query2)) {
			statement1.setObject(1, UUID.fromString(id));
			ResultSet resultSet = statement1.executeQuery();
			if (!resultSet.next()) {
				throw new SQLException("Запись с id \"" + id + "\" не найдена.");
			}
			statement2.setObject(1, UUID.fromString(id));
			int affectedRows = statement2.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Не удалось удалить проект.");
			}
		} catch (SQLException e) {
			System.err.println("[ERROR] Ошибка удаления проекта: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static ArrayList<Map<String, String>> getAllPostShort(int userId, String projectId, int pageSize,
			int pageNumber) {
		String query = "SELECT title, id, date, author, tags public.projects";
		ArrayList<Map<String, String>> result = new ArrayList<Map<String, String>>();
		try (Statement statement = connection.createStatement()) {
			ResultSet rSet = statement.executeQuery(query);
			while (rSet.next()) {
				String id = rSet.getString("id");
				String title = rSet.getString("title");
				result.add(Map.of("title", title, "id", id));
			}

		} catch (Exception e) {
			System.err.println("[ERROR] Ошибка поиска тега: " + e.toString());
			e.printStackTrace();
		}
		return result;

		// TODO: Запрос PostShort[] из таблицы posts.
	}

	public static String addNewPost(String title, String content, String tags) {
		return null;
		// TODO: Создание записи в posts и возврат UUID записи.
	}

	// TODO: Удалить это в релизе.
	public static void test() {
		System.out.println("IM READY!");
	}

}
