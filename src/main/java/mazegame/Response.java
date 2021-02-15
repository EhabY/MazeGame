package mazegame;

import serialization.JsonEncodable;

public class Response {

  public final JsonEncodable encodable;
  public final String message;

  public Response(String message) {
    this(message, null);
  }

  public Response(String message, JsonEncodable encodable) {
    this.encodable = encodable;
    this.message = message;
  }
}
