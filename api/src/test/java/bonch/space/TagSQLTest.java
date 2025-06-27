package bonch.space;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * TagSQLTest
 */
public class TagSQLTest {
	static String tagId;

	public static class Tag {
		public Tag() {
		}

		public String title;
		public UUID id;
	}

	@Test
	public void getTag() {
		System.out.println("Тест на ТЭГИ");
		ArrayList<Tag> tag = SQL.Select("tags", Tag.class, null);
		for (Tag tag2 : tag) {
			System.out.println(tag2.id);
			System.out.println(tag2.title);

		}
	}

}
