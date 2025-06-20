package bonch.space;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		// Пример построения роута
		// get("/example/route", (req, res) -> {
		// // 1. Получение параметров
		// req.queryParams("param_name");
		// // 2. Получение Заголовка
		// req.headers("User-Agent");
		// // 3. Получение тела
		// req.body();
		// // =============================================================
		//
		// // 1. Изменение статус кода.
		// res.status(200);
		// return body
		// });

		get("/tags/usual", (req, res) -> {
			res.status(200);
			return new Gson().toJson(SQL.getAllTags());
		});
		post("/tags/usual", (req, res) -> {
			// TODO: Добавить обработку cookie.
			try {
				JsonObject body = new Gson().fromJson(req.body(), JsonObject.class);

				if (body.has("title")) {
					String title = body.get("title").getAsString();
					SQL.addTag(title);
				} else {
					res.status(400);
					return "Invalid JSON";
				}
			} catch (Exception e) {
				res.status(400);
				return "Invalid JSON";
			}
			res.status(200);
			return "";

		});

		put("/tags/usual", (req, res) -> {
			try {
				// TODO: Добавить обработку cookie.
				JsonObject body = new Gson().fromJson(req.body(), JsonObject.class);

				if (body.has("id") && body.has("title")) {
					String title = body.get("title").getAsString();
					String id = body.get("id").getAsString();
					if (SQL.changeTag(id, title)) {
						res.status(200);
					}
				} else {
					res.status(400);
				}
			} catch (Exception e) {
				res.status(400);
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
			return "";
		});
		delete("/tags/usual", (req, res) -> {
			try {
				// TODO: Добавить обработку cookie.
				JsonObject body = new Gson().fromJson(req.body(), JsonObject.class);

				if (body.has("id") && body.has("title")) {
					String id = body.get("id").getAsString();
					if (SQL.deleteTag(id)) {
						res.status(200);
					}
				} else {
					res.status(400);
				}
			} catch (Exception e) {
				res.status(400);
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
			return "";
		});

	}

}
