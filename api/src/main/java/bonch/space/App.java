package bonch.space;

import static spark.Spark.*;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		get("/:name", (req, src) -> {
			return "Hello World, " + req.params(":name");
		});
	}
}
