import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import mazegame.Direction;
import mazegame.MazeMap;
import mazegame.PlayerController;
import mazegame.room.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import parser.GameParser;
import serialization.JsonSerializer;
import website.match.Interpreter;

public class GameParserSerializerTest {

  private PlayerController playerController;
  private Interpreter interpreter;

  @BeforeEach
  void setUp() throws IOException {
    MazeMap mazeMap = GameParser.parseJsonFile("src/test/resources/map.json");
    setUpPlayer(mazeMap);
  }

  private void setUpPlayer(MazeMap mazeMap) {
    Room startingRoom = mazeMap.getStartingRooms().get(0);
    playerController = new PlayerController("", mazeMap, startingRoom);
    interpreter = new Interpreter(playerController);
  }

  @Test
  void testGameParser() {
    playerFirstHalf();
    playSecondHalf();
    assertEquals(playerController.getCurrentRoom().getId(), 3);
  }

  @Test
  void testSerializer() {
    playerFirstHalf();
    serializeThenParseMap();
    playSecondHalf();
    assertEquals(playerController.getCurrentRoom().getId(), 3);
  }

  private void serializeThenParseMap() {
    String mapJson = JsonSerializer.serializeGameState(playerController);
    MazeMap mazeMap = GameParser.parseJson(mapJson);
    setUpPlayer(mazeMap);
  }

  private void playerFirstHalf() {
    turnUntil(Direction.EAST);
    interpreter.execute("check");
    interpreter.execute("left");
    interpreter.execute("use dragon glass key");
    interpreter.execute("forward");
    interpreter.execute("left");
  }

  private void playSecondHalf() {
    turnUntil(Direction.WEST);
    interpreter.execute("trade");
    interpreter.execute("sell dragon glass key");
    interpreter.execute("buy monkee key");
    interpreter.execute("finish trade");
    interpreter.execute("right");
    interpreter.execute("use monkee key");
    interpreter.execute("forward");
  }

  private void turnUntil(Direction direction) {
    Direction orientation = getOrientation(playerController);
    while (orientation != direction) {
      interpreter.execute("left");
      orientation = getOrientation(playerController);
    }
  }

  private Direction getOrientation(PlayerController playerController) {
    return playerController.getPlayer().getDirection();
  }
}
