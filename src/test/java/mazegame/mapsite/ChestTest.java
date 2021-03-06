package mazegame.mapsite;

import static org.junit.jupiter.api.Assertions.assertEquals;

import mazegame.item.Key;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ChestTest {

  private Chest chest;

  @BeforeEach
  void setUp() {
    Loot loot = new Loot(10);
    chest = new Chest(loot, Key.NO_KEY, false);
  }

  @Test
  void shouldClearLootAfterAcquiringIt() {
    chest.acquireLoot();
    Loot empty = chest.acquireLoot();
    assertEquals(empty, Loot.EMPTY_LOOT);
  }
}
