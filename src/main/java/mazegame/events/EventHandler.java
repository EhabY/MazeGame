package mazegame.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import mazegame.room.Room;

public class EventHandler {

  private final List<MatchListener> matchListeners = new CopyOnWriteArrayList<>();
  private final List<StateListener> stateListeners = new CopyOnWriteArrayList<>();
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  public void addListener(MatchListener listener) {
    matchListeners.add(listener);
  }

  public void addListener(StateListener listener) {
    stateListeners.add(listener);
  }

  public void triggerMoveEvent(Room previousRoom) {
    for (MatchListener listener : matchListeners) {
      executor.execute(() -> listener.onMove(previousRoom));
    }
  }

  public void triggerQuitEvent() {
    for (MatchListener listener : matchListeners) {
      listener.onQuit();
    }
  }

  public void triggerGameEvent(GameEvent gameEvent, String message) {
    for (StateListener listener : stateListeners) {
      listener.onGameEvent(gameEvent, message);
    }
  }

  public void triggerStateChangeEvent(State state, String message) {
    for (StateListener listener : stateListeners) {
      listener.onStateChange(state, message);
    }
  }

  public void clearAllListeners() {
    matchListeners.clear();
    stateListeners.clear();
  }
}
