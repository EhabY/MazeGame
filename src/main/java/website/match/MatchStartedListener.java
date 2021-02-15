package website.match;

import java.util.Map;
import mazegame.PlayerController;
import org.eclipse.jetty.websocket.api.Session;

public interface MatchStartedListener {

  void onMatchStart(Map<Session, PlayerController> playersMap);
}
