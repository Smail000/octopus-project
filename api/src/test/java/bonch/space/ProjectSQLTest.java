package bonch.space;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class ProjectSQLTest {
	// Тестирование SQL
	static int sizeBeforeTest;
	static String projectId;

	@Before
	public void initTestProjectData() {
		sizeBeforeTest = SQL.getAllProjectShort().size();
		projectId = SQL.addProject("TestTitle", "TestShortTitle", "TestDescription");
		System.out.println(projectId);
	}

	@Test
	public void addProject() {

		assertEquals(Map.of("title", "TestTitle", "shortTitle", "TestShortTitle", "description", "TestDescription"),
				SQL.getProject(projectId));
	}

	@Test
	public void updateProject() {
		assertEquals(SQL.changeProject(projectId, "TestTitle2", "TestShortTitle2", "TestDescription2"), true);
		assertEquals(Map.of("title", "TestTitle2", "shortTitle", "TestShortTitle2", "description", "TestDescription2"),
				SQL.getProject(projectId));
	}

	@Test
	public void deleteProject() {
		SQL.deleteProject(projectId);
		projectId = null;
		assertEquals(sizeBeforeTest, SQL.getAllProjectShort().size());
	}

	@After
	public void cleanDatabase() {
		if (projectId != null) {
			SQL.deleteProject(projectId);
		}
	}
}
