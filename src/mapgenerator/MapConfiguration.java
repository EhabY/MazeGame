package mapgenerator;

import mapgenerator.mapsitegenerator.MapSiteGenerator;
import org.json.JSONObject;

public interface MapConfiguration {
    void setNumberOfPlayers(int numberOfPlayers);

    int getNumberOfPlayers();

    int getSide();

    int getLevels();

    int getStepsPerLevel();

    int getDifficulty();

    WeightedRandomizer<MapSiteGenerator> getMapSiteRandomizer(RandomNameGenerator randomNameGenerator);

    JSONObject getConfiguration();
}
