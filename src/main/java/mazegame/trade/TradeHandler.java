package mazegame.trade;

import java.util.Objects;
import mazegame.item.Item;
import mazegame.mapsite.Seller;
import mazegame.player.Player;

public class TradeHandler {

  private final Player player;
  private final Seller seller;
  private String reason;

  private TradeHandler(Player player, Seller seller) {
    this.player = Objects.requireNonNull(player);
    this.seller = Objects.requireNonNull(seller);
  }

  public static TradeHandler startTransaction(Player player, Seller seller) {
    return new TradeHandler(player, seller);
  }

  public Seller getSeller() {
    return seller;
  }

  public String getReason() {
    return reason;
  }

  public boolean buy(String itemName) {
    boolean bought = canBuy(itemName);
    if (bought) {
      long price = seller.getItemPrice(itemName);
      player.removeGoldFromInventory(price);
      Item itemBought = seller.takeItem(itemName);
      player.addItemToInventory(itemBought);
    }
    return bought;
  }

  private boolean canBuy(String itemName) {
    boolean itemExists = seller.itemExists(itemName);
    boolean sellerHasItem = seller.hasItem(itemName);
    long playerGold = player.getGold();

    if (!itemExists) {
      reason = itemName + " does not exist!";
    } else if (!sellerHasItem) {
      reason = "Seller does not have " + itemName + "!";
    } else if (playerGold < seller.getItemPrice(itemName)) {
      long itemPrice = seller.getItemPrice(itemName);
      reason = "Not enough gold (have " + playerGold + ", but need " + itemPrice + ")!";
    } else {
      reason = "";
      return true;
    }

    return false;
  }

  public boolean sell(String itemName) {
    boolean sold = canSell(itemName);
    if (sold) {
      Item itemSold = player.takeItemFromInventory(itemName);
      seller.addItem(itemSold);
      long price = seller.getItemPrice(itemName);
      player.addGoldToInventory(price);
    }
    return sold;
  }

  private boolean canSell(String itemName) {
    boolean itemExists = seller.itemExists(itemName);
    boolean playerHasItem = player.hasItem(itemName);

    if (!itemExists) {
      reason = itemName + " does not exist!";
    } else if (!playerHasItem) {
      reason = "Player does not have " + itemName + "!";
    } else {
      reason = "";
      return true;
    }

    return false;
  }

  @Override
  public String toString() {
    return "TradeHandler{" + "player=" + player + ", seller=" + seller + '}';
  }
}
