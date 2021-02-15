package mazegame.mapsite;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mazegame.exceptions.InvalidUseOfItem;
import mazegame.item.Key;
import org.junit.jupiter.api.Test;

public class AbstractLockableTest {

  @Test
  void shouldBeUnlockedIfNoKey() {
    AbstractLockable abstractLockable = new Chest(Loot.EMPTY_LOOT, Key.NO_KEY, true);
    assertFalse(abstractLockable.isLocked());
  }

  @Test
  void shouldThrowInvalidUseOfItemWhenUsingWrongKey() {
    Key key = Key.fromString("Testing lock");
    AbstractLockable abstractLockable = new Chest(Loot.EMPTY_LOOT, key, true);
    Key wrongKey = Key.fromString("Test lock");
    assertThrows(InvalidUseOfItem.class, () -> abstractLockable.toggleLock(wrongKey));
  }

  @Test
  void shouldBeUnlockedAsDefinedInTheConstructor() {
    Key key = Key.fromString("Testing lock");
    AbstractLockable abstractLockable = new Chest(Loot.EMPTY_LOOT, key, false);
    assertFalse(abstractLockable.isLocked());
  }

  @Test
  public void shouldBeLockedAsDefinedInTheConstructor() {
    Key key = Key.fromString("Testing lock");
    AbstractLockable abstractLockable = new Chest(Loot.EMPTY_LOOT, key, true);
    assertTrue(abstractLockable.isLocked());
  }

}
