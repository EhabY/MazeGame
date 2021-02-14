package mazegame.player;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import mazegame.exceptions.NotEnoughGoldException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InventoryTest {

  Inventory inventory;

  @BeforeEach
  void setUp() {
    inventory = new Inventory(5);
  }

  @Test
  void shouldThrowIllegalArgumentExceptionIfGoldIsNegative() {
    assertThrows(IllegalArgumentException.class, () -> inventory.addGold(-1));
    assertDoesNotThrow(() -> inventory.addGold(0));
    assertDoesNotThrow(() -> inventory.addGold(1));
  }

  @Test
  void shouldThrowNotEnoughGoldExceptionIfSubtractingMore() {
    assertThrows(NotEnoughGoldException.class, () -> inventory.subtractGold(6));
    assertEquals(inventory.getGold(), 5);
  }

  @Test
  void shouldNotThrowExceptionIfSubtractingExactly() {
    assertDoesNotThrow(() -> inventory.subtractGold(5));
  }
}
