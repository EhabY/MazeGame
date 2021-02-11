package website.match;

import mazegame.PlayerController;
import org.eclipse.jetty.websocket.api.Session;
import java.util.Map;

public interface MatchStartedListener {
    void matchStarted(Map<Session, PlayerController> playersMap);
}
