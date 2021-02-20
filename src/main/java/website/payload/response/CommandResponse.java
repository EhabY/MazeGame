package website.payload.response;

import mazegame.Response;
import serialization.Encoder;
import serialization.JsonEncodable;
import serialization.JsonEncoder;

public class CommandResponse {

  private static final Encoder JSON_ENCODER = new JsonEncoder();
  private String command;
  private String data;
  private String message;

  public CommandResponse(String command, Response response) {
    this(command, getData(response.encodable), response.message);
  }

  public CommandResponse(String command, String data, String message) {
    this.command = command;
    this.data = data;
    this.message = message;
  }

  private static String getData(JsonEncodable encodable) {
    if (encodable == null) {
      return "";
    } else {
      return encodable.encodeUsing(JSON_ENCODER);
    }
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
