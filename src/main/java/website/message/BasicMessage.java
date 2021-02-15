package website.message;

import java.util.Objects;
import org.json.JSONObject;

public abstract class BasicMessage implements Message {

  private final String type;
  private final String content;

  public BasicMessage(String type, String content) {
    this.type = Objects.requireNonNull(type);
    this.content = Objects.requireNonNull(content);
  }

  @Override
  public String getPayload() {
    JSONObject messageJson = new JSONObject();
    messageJson.put("type", type);
    messageJson.put("content", content);
    return messageJson.toString();
  }
}
