package website.message;

public class InvalidMessage extends BasicMessage {

  public InvalidMessage(String errorMessage) {
    super("invalid", errorMessage);
  }
}
