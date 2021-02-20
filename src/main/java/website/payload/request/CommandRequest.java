package website.payload.request;

import javax.validation.constraints.NotBlank;

public class CommandRequest {

  @NotBlank
  private String command;

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }
}
