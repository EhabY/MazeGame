package website.message;

import mazegame.events.GameEvent;
import org.json.JSONObject;

public class EventMessage extends BasicMessage {

  private final GameEvent gameEvent;

  public EventMessage(String content, GameEvent gameEvent) {
    super("event", content);
    this.gameEvent = gameEvent;
  }

  @Override
  public String getPayload() {
    JSONObject messageJson = new JSONObject(super.getPayload());
    messageJson.put("eventType", gameEvent);
    return messageJson.toString();
  }
}
