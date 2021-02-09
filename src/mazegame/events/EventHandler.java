package mazegame.events;

import mazegame.room.Room;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventHandler {
    private final List<MoveListener> moveListeners = new ArrayList<>();
    private final List<StateListener> stateListeners = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void addListener(MoveListener listener) {
        moveListeners.add(listener);
    }

    public void addListener(StateListener listener) {
        stateListeners.add(listener);
    }

    public void triggerMoveEvent(Room previousRoom) {
        for(MoveListener listener : moveListeners) {
            executor.execute(() -> listener.moved(previousRoom));
        }
    }

    public void triggerGameEvent(GameEvent gameEvent, String message) {
        for(StateListener listener : stateListeners) {
            listener.stateChanged(gameEvent, message);
        }
    }
}
