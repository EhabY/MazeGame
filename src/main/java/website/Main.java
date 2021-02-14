package website;

import spark.ModelAndView;
import spark.Spark;
import website.engine.ThymeleafTemplateEngine;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    public static final int TIMEOUT = 30 * 60 * 1000;
    public static final String HOME = "/";
    public static final String STATIC_FILES = "/public";
    public static final String MATCH_WEBSOCKET = "/websocket/match";

    public static void main(String[] args) {
        Spark.staticFileLocation(STATIC_FILES);
        Spark.webSocketIdleTimeoutMillis(TIMEOUT);
        Spark.webSocket(MATCH_WEBSOCKET, MatchWebSocketHandler.class);

        Spark.get(HOME, (request, response) -> {
            Map<String, Object> model = new ConcurrentHashMap<>();
            return new ThymeleafTemplateEngine().render(new ModelAndView(model, "matchview"));
        });

        Spark.init();
    }
}
