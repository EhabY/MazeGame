package mazegame.events;

import mazegame.room.Room;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventHandler {
    private final List<MatchListener> matchListeners = new ArrayList<>();
    private final List<StateListener> stateListeners = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void addListener(MatchListener listener) {
        matchListeners.add(listener);
    }

    public void addListener(StateListener listener) {
        stateListeners.add(listener);
    }

    public void triggerMoveEvent(Room previousRoom) {
        for(MatchListener listener : matchListeners) {
            executor.execute(() -> listener.onMove(previousRoom));
        }
    }

    public void triggerQuitEvent() {
        for(MatchListener listener : matchListeners) {
            listener.onQuit();
        }
    }

    public void triggerGameEvent(GameEvent gameEvent, String message) {
        for(StateListener listener : stateListeners) {
            listener.onStateChange(gameEvent, message);
        }
    }
}
