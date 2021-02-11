package mazegame.events;

import mazegame.room.Room;

public interface MoveListener {
    void moved(Room fromRoom);
}
