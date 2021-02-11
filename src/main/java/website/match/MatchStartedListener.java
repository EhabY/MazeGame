package website.match;

import mazegame.PlayerController;
import org.eclipse.jetty.websocket.api.Session;
import java.util.Map;

public interface MatchStartedListener {
    void onMatchStart(Map<Session, PlayerController> playersMap);
}
