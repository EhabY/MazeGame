package mazegame.player;

import mazegame.item.Key;
import mazegame.mapsite.Hangable;
import mazegame.mapsite.Mirror;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CheckVisitorTest {

    @Test
    void shouldNotAddNoKeyInHangableToInventory() {
        Inventory inventory = new Inventory(0);
        CheckVisitor checkVisitor = new CheckVisitor(inventory);
        Hangable hangable = new Mirror(Key.NO_KEY);
        checkVisitor.visit(hangable);
        assertEquals(inventory.getInventoryAsLoot().getItems().size(), 0);
    }
}
