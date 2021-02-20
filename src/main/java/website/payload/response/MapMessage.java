package website.payload.response;

public class MapMessage extends BasicMessage {

  public MapMessage(String mapJson) {
    super("map", mapJson);
  }
}
