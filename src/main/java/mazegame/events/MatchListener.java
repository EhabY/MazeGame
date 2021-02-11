package mazegame.events;

import mazegame.room.Room;

public interface MatchListener {
    void onMove(Room fromRoom);
    void onQuit();
}
