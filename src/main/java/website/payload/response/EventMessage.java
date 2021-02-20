package website.payload.response;

import mazegame.events.GameEvent;

public class EventMessage extends BasicMessage {

  private GameEvent eventType;

  public EventMessage(String content, GameEvent eventType) {
    super("event", content);
    this.eventType = eventType;
  }

  public GameEvent getEventType() {
    return eventType;
  }

  public void setEventType(GameEvent eventType) {
    this.eventType = eventType;
  }
}
