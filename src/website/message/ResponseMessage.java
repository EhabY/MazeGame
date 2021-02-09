package website.message;

public class ResponseMessage extends BasicMessage {
    public ResponseMessage(String content) {
        super("response", content);
    }
}
