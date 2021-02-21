package mapgenerator;

import mapgenerator.mapsitegenerator.ChestGenerator;
import mapgenerator.mapsitegenerator.DoorGenerator;
import mapgenerator.mapsitegenerator.HangableGenerator;
import mapgenerator.mapsitegenerator.MapSiteGenerator;
import mapgenerator.mapsitegenerator.SellerGenerator;
import mapgenerator.mapsitegenerator.WallGenerator;
import org.json.JSONArray;
import org.json.JSONObject;

public class DefaultMapConfiguration implements MapConfiguration {

  private static final int STEPS_PER_LEVEL = 3;
  private static final int SECONDS_PER_ROOM = 90;
  private static final int MINIMUM_DIFFICULTY = 1;
  private static final int MEDIUM_DIFFICULTY = 5;
  private static final long STARTING_GOLD = 10;
  private final int levels;
  private final int stepsPerLevel;
  private final int difficulty;
  private int side;
  private int numberOfPlayers;

  private DefaultMapConfiguration(Builder builder) {
    this.levels = builder.levels;
    this.stepsPerLevel = builder.stepsPerLevel;
    this.difficulty = builder.difficulty;
  }

  @Override
  public int getNumberOfPlayers() {
    return numberOfPlayers;
  }

  @Override
  public void setNumberOfPlayers(int numberOfPlayers) {
    if (numberOfPlayers < 1) {
      throw new IllegalArgumentException("Number of players must be greater than or equal to 1");
    }

    this.numberOfPlayers = numberOfPlayers;
    this.side = stepsPerLevel * levels * intSqrt(numberOfPlayers);
  }

  @Override
  public int getSide() {
    return side;
  }

  @Override
  public int getLevels() {
    return levels;
  }

  @Override
  public int getStepsPerLevel() {
    return stepsPerLevel;
  }

  @Override
  public int getDifficulty() {
    return difficulty;
  }

  @Override
  public WeightedItemRandomizer<MapSiteGenerator> getMapSiteRandomizer(
      RandomNameGenerator randomNameGenerator) {
    WeightedItemRandomizer<MapSiteGenerator> mapSiteRandomizer = new WeightedItemRandomizer<>();
    mapSiteRandomizer.addEvent(new DoorGenerator(randomNameGenerator), 5);
    mapSiteRandomizer.addEvent(new HangableGenerator(randomNameGenerator), 2);
    mapSiteRandomizer.addEvent(new ChestGenerator(randomNameGenerator), 1);
    mapSiteRandomizer.addEvent(new SellerGenerator(randomNameGenerator), 1);
    mapSiteRandomizer.addEvent(new WallGenerator(), 1);
    return mapSiteRandomizer;
  }

  @Override
  public JSONObject getConfiguration() {
    JSONObject mapConfig = new JSONObject();
    mapConfig.put("endRoomID", side * side);
    mapConfig.put("time", 2 * side * SECONDS_PER_ROOM / this.difficulty);
    mapConfig.put("gold", STARTING_GOLD / this.difficulty);
    mapConfig.put("items", getStartingItemList());
    return mapConfig;
  }

  private int intSqrt(int number) {
    return (int) (Math.sqrt(number));
  }

  private JSONArray getStartingItemList() {
    JSONArray itemsJson = new JSONArray();
    if (difficulty <= MEDIUM_DIFFICULTY) {
      itemsJson.put(ItemGenerator.getFlashlightJson());
    }
    return itemsJson;
  }

  public static class Builder {

    private int levels = 1;
    private int stepsPerLevel = STEPS_PER_LEVEL;
    private int difficulty = MINIMUM_DIFFICULTY;

    public Builder levels(int levels) {
      if (levels < 1) {
        throw new IllegalArgumentException("Levels must be greater than or equal to 1");
      }

      this.levels = levels;
      return this;
    }

    public Builder stepsPerLevel(int stepsPerLevel) {
      if (stepsPerLevel < 1) {
        throw new IllegalArgumentException("Steps per level must be greater than or equal to 1");
      }

      this.stepsPerLevel = stepsPerLevel;
      return this;
    }

    public Builder difficulty(int difficulty) {
      if (difficulty < 1 || difficulty > 10) {
        throw new IllegalArgumentException(
            "Difficulty must be greater than or equal to 1 and less than 10");
      }

      this.difficulty = difficulty;
      return this;
    }

    public DefaultMapConfiguration build() {
      return new DefaultMapConfiguration(this);
    }
  }
}
