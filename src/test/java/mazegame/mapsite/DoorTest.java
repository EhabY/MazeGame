package mazegame.mapsite;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import mazegame.Direction;
import mazegame.exceptions.MapSiteLockedException;
import mazegame.item.Key;
import mazegame.room.LightSwitch;
import mazegame.room.Room;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class DoorTest {

  private static final Map<Direction, SerializableMapSite> map = new HashMap<>();
  private static Room room1;
  private static Room room2;
  private static Door door;

  @BeforeAll
  static void setUp() {
    map.put(Direction.EAST, Wall.getInstance());
    room1 = new Room(1, map, new LightSwitch(true));
    room2 = new Room(2, map, new LightSwitch(true));
    door = new Door.Builder(Key.NO_KEY, false).setRoom(room1).setOtherRoom(room2).getDoor();
  }

  @Test
  void shouldThrowMapSiteLockedExceptionIfOpeningWhenLocked() {
    Key key = Key.fromString("Testing door");
    Door door = new Door.Builder(key, true).setRoom(room1).setOtherRoom(room2).getDoor();
    assertThrows(MapSiteLockedException.class, () -> door.getNextRoom(room1));
  }

  @Test
  void shouldThrowIllegalArgumentExceptionIfWrongRoomProvided() {
    Room room3 = new Room(3, map, new LightSwitch(true));
    assertThrows(IllegalArgumentException.class, () -> door.getNextRoom(room3));
  }

  @Test
  void shouldNotThrowIllegalArgumentExceptionIfSameId() {
    Room room3 = new Room(2, map, new LightSwitch(true));
    assertDoesNotThrow(() -> door.getNextRoom(room3));
  }

}
