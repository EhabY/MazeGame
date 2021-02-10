package mazegame.trade;

import mazegame.item.Item;
import mazegame.player.Player;
import mazegame.mapsite.Seller;
import java.util.Objects;

public class TransactionHandler {
  private final Player player;
  private final Seller seller;

  private TransactionHandler(Player player, Seller seller) {
    this.player = Objects.requireNonNull(player);
    this.seller = Objects.requireNonNull(seller);
  }

  public static TransactionHandler startTransaction(Player player, Seller seller) {
    return new TransactionHandler(player, seller);
  }

  public void buy(String itemName) {
    long price = seller.getItemPrice(itemName);
    player.removeGoldFromInventory(price);
    Item itemBought = seller.takeItem(itemName);
    player.addItemToInventory(itemBought);
  }

  public void sell(String itemName) {
    Item itemSold = player.takeItemFromInventory(itemName);
    seller.addItem(itemSold);
    long price = seller.getItemPrice(itemName);
    player.addGoldToInventory(price);
  }

  public Seller getSeller() {
    return seller;
  }

  @Override
  public String toString() {
    return "TransactionHandler{" + "player=" + player + ", seller=" + seller + '}';
  }
}
