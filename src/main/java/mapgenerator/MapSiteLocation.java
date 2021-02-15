package mapgenerator;

import mazegame.Direction;

public class MapSiteLocation {

    int roomID;
    Direction direction;

    MapSiteLocation(int roomID, Direction direction) {
      this.roomID = roomID;
      this.direction = direction;
    }
}
