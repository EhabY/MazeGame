package mazegame;

public class Response {
  public static final Response VALID_RESPONSE = new Response(true, "");
  public final boolean valid;
  public final String message;

  public Response(boolean valid, String message) {
    this.valid = valid;
    this.message = message;
  }
}
