package serialization;

import mazegame.PlayerController;
import mazegame.item.Flashlight;
import mazegame.item.Key;
import mazegame.mapsite.Chest;
import mazegame.mapsite.Door;
import mazegame.mapsite.Loot;
import mazegame.mapsite.Mirror;
import mazegame.mapsite.Painting;
import mazegame.mapsite.Seller;
import mazegame.mapsite.Wall;
import mazegame.player.Player;
import mazegame.room.Room;

public interface Encoder {

  String visit(Flashlight flashlight);

  String visit(Key key);

  String visit(Chest chest);

  String visit(Door door);

  String visit(Loot loot);

  String visit(Mirror mirror);

  String visit(Painting painting);

  String visit(Seller seller);

  String visit(Wall wall);

  String visit(Player player);

  String visit(PlayerController playerController);

  String visit(Room room);
}
