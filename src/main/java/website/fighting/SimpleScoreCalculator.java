package website.fighting;

import java.util.List;
import mazegame.PlayerController;
import mazegame.item.Item;
import mazegame.mapsite.Loot;

public class SimpleScoreCalculator implements ScoreCalculator {

  private static final long FLASHLIGHT_PRICE = 2;
  private static final long KEY_PRICE = 10;

  @Override
  public long calculateScore(PlayerController playerController) {
    Loot loot = playerController.getLoot();
    long score = loot.getGold();
    List<Item> itemList = loot.getItems();
    for (Item item : itemList) {
      if ("Flashlight".equalsIgnoreCase(item.getType())) {
        score += FLASHLIGHT_PRICE;
      } else if ("Key".equalsIgnoreCase(item.getType())) {
        score += KEY_PRICE;
      }
    }

    return score;
  }
}
