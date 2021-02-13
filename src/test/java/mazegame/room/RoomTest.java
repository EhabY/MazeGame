package mazegame.room;

import mazegame.Direction;
import mazegame.exceptions.NoLightsException;
import mazegame.mapsite.Loot;
import mazegame.mapsite.SerializableMapSite;
import mazegame.mapsite.Wall;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RoomTest {
    private static Map<Direction, SerializableMapSite> map;
    private static Room room;

    @BeforeAll
    static void setUp() {
        map = new HashMap<>();
        map.put(Direction.NORTH, Wall.getInstance());
        map.put(Direction.EAST, Wall.getInstance());
        map.put(Direction.SOUTH, Wall.getInstance());
        map.put(Direction.WEST, Wall.getInstance());
        room = new Room(1, map, NoLightSwitch.getInstance());
    }

    @Test
    void shouldAlwaysHaveLightsOffIfNoLights() {
        assertFalse(room.isLit());
    }

    @Test
    void shouldThrowExceptionIfTogglingNoLightSwitch() {
        assertThrows(NoLightsException.class, room::toggleLights);
    }

    @Test
    void shouldClearLootAfterAcquiringIt() {
        room.acquireLoot();
        Loot empty = room.acquireLoot();
        assertEquals(empty, Loot.EMPTY_LOOT);
    }
}
