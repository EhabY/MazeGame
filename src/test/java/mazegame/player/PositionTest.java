package mazegame.player;

import mazegame.Direction;
import mazegame.item.Flashlight;
import mazegame.mapsite.DarkMapSite;
import mazegame.mapsite.SerializableMapSite;
import mazegame.mapsite.Wall;
import mazegame.room.LightSwitch;
import mazegame.room.NoLightSwitch;
import mazegame.room.Room;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PositionTest {
    private static Map<Direction, SerializableMapSite> map;

    @BeforeAll
    static void setUp() {
        map = new HashMap<>();
        map.put(Direction.NORTH, Wall.getInstance());
        map.put(Direction.EAST, Wall.getInstance());
        map.put(Direction.SOUTH, Wall.getInstance());
        map.put(Direction.WEST, Wall.getInstance());
    }

    @Test
    void shouldReturnMapSiteIfRoomIsLit() {
        Room room = new Room(1, map, new LightSwitch(true));
        Position position = new Position(room, Direction.EAST);
        assertEquals(position.lookAhead(), Wall.getInstance());
    }

    @Test
    void shouldReturnDarkMapSiteIfRoomIsNotLit() {
        Room room = new Room(1, map, new LightSwitch(false));
        Position position = new Position(room, Direction.EAST);
        assertEquals(position.lookAhead(), DarkMapSite.getInstance());
    }

    @Test
    void shouldReturnMapSiteRegardlessIfFlashlightOn() {
        Room room = new Room(1, map, NoLightSwitch.getInstance());
        Position position = new Position(room, Direction.EAST);
        Flashlight flashlight = new Flashlight(true);
        assertEquals(position.lookAheadWithFlashlight(flashlight), Wall.getInstance());
    }
}
