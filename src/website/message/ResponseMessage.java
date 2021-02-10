package website.message;

import org.json.JSONObject;

import java.util.Objects;

public class ResponseMessage extends BasicMessage {
    private final String command;
    public ResponseMessage(String content, String command) {
        super("response", content);
        this.command = Objects.requireNonNull(command);
    }

    @Override
    public String getPayload() {
        JSONObject payloadJson = new JSONObject(super.getPayload());
        payloadJson.put("command", command);
        return payloadJson.toString();
    }
}
