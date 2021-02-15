package website.match;

import java.util.Objects;
import mazegame.Response;

public class PlayerConfiguration {

  private final Interpreter interpreter;
  private final MatchCreator matchCreator;

  PlayerConfiguration(Interpreter interpreter, MatchCreator matchCreator) {
    this.interpreter = Objects.requireNonNull(interpreter);
    this.matchCreator = Objects.requireNonNull(matchCreator);
  }

  public Response executeCommand(String command) {
    return interpreter.execute(command);
  }

  public String getMazeMapJson() {
    return matchCreator.getMazeMapJson();
  }
}
