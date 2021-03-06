package mapgenerator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import parser.GameParser;

public class MapGeneratorTest {

  private static MapConfiguration mapConfiguration;

  @BeforeAll
  static void setUp() {
    mapConfiguration = new DefaultMapConfiguration.Builder().build();
    mapConfiguration.setNumberOfPlayers(1);
  }

  @Test
  void mapGeneratorGeneratesValidMapJson() {
    String mapJson = MapGenerator.generateMap(mapConfiguration);
    assertDoesNotThrow(() -> GameParser.parseJson(mapJson));
  }
}
