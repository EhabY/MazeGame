package mazegame.trade;

import mazegame.player.Player;
import mazegame.mapsite.Seller;

public class TradeHandler {
  private final TransactionHandler transactionHandler;

  public TradeHandler(Player player, Seller seller) {
    transactionHandler = TransactionHandler.startTransaction(player, seller);
  }

  public String list() {
    return transactionHandler.listSellerItems() + "\n" + transactionHandler.listSellerPriceList();
  }

  public String buy(String itemName) {
    transactionHandler.buy(itemName);
    return itemName + " bought and acquired";
  }

  public String sell(String itemName) {
    transactionHandler.sell(itemName);
    return itemName + " sold";
  }

  @Override
  public String toString() {
    return "TradeHandler{" + "transactionHandler=" + transactionHandler + '}';
  }
}
