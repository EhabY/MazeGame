package mazegame.events;

public interface StateListener {
    void onStateChange(GameEvent event, String message);
}
