package website;

import spark.ModelAndView;
import spark.Spark;
import website.engine.ThymeleafTemplateEngine;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    public static void main(String[] args) {
        Spark.webSocket("/websocket/match", MatchWebSocketHandler.class);

        Spark.get("/", (request, response) -> {
            Map<String, Object> model = new ConcurrentHashMap<>();

            return new ThymeleafTemplateEngine().render(new ModelAndView(model, "matchview"));
        });

        Spark.init();
    }
}
