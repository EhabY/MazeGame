package website.services.player;

import java.util.Objects;
import website.services.match.MatchCreator;

public class PlayerConfig {

  private final String username;
  private final MatchCreator matchCreator;

  public PlayerConfig(String username, MatchCreator matchCreator) {
    this.username = Objects.requireNonNull(username);
    this.matchCreator = Objects.requireNonNull(matchCreator);
  }

  public void makePlayerReady() {
    matchCreator.makeReady(username);
  }

  public void removePlayer() {
    matchCreator.removePlayer(username);
  }

  public String getMazeMapJson() {
    return matchCreator.getMazeMapJson();
  }

}
