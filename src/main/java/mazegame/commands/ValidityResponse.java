package mazegame.commands;

public class ValidityResponse {
  public static final ValidityResponse VALID_RESPONSE = new ValidityResponse(true, "");
  public final boolean valid;
  public final String message;

  public ValidityResponse(boolean valid, String message) {
    this.valid = valid;
    this.message = message;
  }
}
