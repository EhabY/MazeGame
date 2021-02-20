package website.payload.response;

import mazegame.events.State;

public class StateChangeMessage extends BasicMessage {

  private State state;

  public StateChangeMessage(String content, State state) {
    super("stateChange", content);
    this.state = state;
  }

  public State getState() {
    return state;
  }

  public void setState(State state) {
    this.state = state;
  }
}
