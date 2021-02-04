package website;

import mazegame.MazeMap;
import mazegame.parser.GameParser;
import spark.ModelAndView;
import spark.Spark;
import website.engine.ThymeleafTemplateEngine;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    static final MazeMap map;
    static {
        MazeMap gameMap = null;
        try {
            gameMap = GameParser.parseJsonFile("map.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
        map = gameMap;
    }

    public static void main(String[] args) {
        Spark.webSocket("/websocket/match", MatchWebSocketHandler.class);

        Spark.get("/", (request, response) -> {
            Map<String, Object> model = new ConcurrentHashMap<>();

            return new ThymeleafTemplateEngine().render(new ModelAndView(model, "matchview"));
        });

        Spark.init();
    }
}
