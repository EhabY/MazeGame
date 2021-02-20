package website.services.match;

import java.util.Collection;
import mazegame.PlayerController;

public interface MatchStartListener {

  void onMatchStart(Collection<PlayerController> playerControllers);
}
