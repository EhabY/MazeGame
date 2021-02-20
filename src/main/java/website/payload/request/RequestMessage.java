package website.payload.request;

import javax.validation.constraints.NotBlank;

public class RequestMessage {

  @NotBlank
  private String type;

  private String content;

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
