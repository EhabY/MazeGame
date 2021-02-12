package mazegame.events;

import mazegame.State;

public interface StateListener {
    void onStateChange(State state, String message);
    void onGameEvent(GameEvent event, String message);
}
