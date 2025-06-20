package bonch.space;

import static spark.Spark.*;

import com.google.gson.Gson;

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
	}

}
