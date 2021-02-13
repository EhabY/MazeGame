package mazegame.item;

import mazegame.exceptions.ItemNotFoundException;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ItemManagerTest {
    private final ItemManager itemManager = new ItemManager();

    @Test
    void shouldAllowItemsOfSameNameButDifferentType() {
        String name = "Some Name";
        itemManager.add(Key.fromString(name));
        itemManager.add(new Flashlight(false, name));
        assertEquals(itemManager.getItemList().size(), 2);
    }

    @Test
    void shouldAllowMultipleIndependentCopies() {
        String name = "Some Name";
        itemManager.add(new Flashlight(false, name));
        itemManager.add(new Flashlight(false, name));
        List<Item> itemList = itemManager.getItemList();
        assertEquals(itemList.size(), 2);
        assertNotSame(itemList.get(0), itemList.get(1));
    }

    @Test
    void shouldThrowItemNotFoundExceptionIfItemNotInItems() {
        assertThrows(ItemNotFoundException.class, () -> itemManager.get("Whatever"));
    }

    @Test
    void takingAnItemShouldReduceTheCountOfItemByOne() {
        Key key = Key.fromString("Some Name");
        itemManager.add(key);
        itemManager.add(key);
        itemManager.takeFromItems(key.getUniqueName());
        assertEquals(itemManager.getItemList().size(), 1);
    }
}
