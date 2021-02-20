package website.payload.response;

import java.util.Objects;

public abstract class BasicMessage {

  private String type;
  private String content;

  public BasicMessage(String type, String content) {
    this.type = Objects.requireNonNull(type);
    this.content = Objects.requireNonNull(content);
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }
}
