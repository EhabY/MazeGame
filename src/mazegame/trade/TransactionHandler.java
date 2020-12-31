package mazegame.trade;

import mazegame.item.Item;
import mazegame.player.Player;
import mazegame.mapsite.Seller;
import java.util.Objects;

class TransactionHandler {
  private final Player player;
  private final Seller seller;

  private TransactionHandler(Player player, Seller seller) {
    this.player = Objects.requireNonNull(player);
    this.seller = Objects.requireNonNull(seller);
  }

  static TransactionHandler startTransaction(Player player, Seller seller) {
    return new TransactionHandler(player, seller);
  }

  void buy(String itemName) {
    long price = seller.getItemPrice(itemName);
    player.removeGoldFromInventory(price);
    Item itemBought = seller.takeItem(itemName);
    player.addItemToInventory(itemBought);
  }

  void sell(String itemName) {
    Item itemSold = player.takeItemFromInventory(itemName);
    seller.addItem(itemSold);
    long price = seller.getItemPrice(itemName);
    player.addGoldToInventory(price);
  }

  String listSellerItems() {
    return seller.getItemList();
  }

  String listSellerPriceList() {
    return seller.getPriceList();
  }

  @Override
  public String toString() {
    return "TransactionHandler{" + "player=" + player + ", seller=" + seller + '}';
  }
}
