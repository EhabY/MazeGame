package website.message;

import mazegame.Response;
import org.json.JSONObject;
import serialization.Encoder;
import serialization.JsonEncoder;

import java.util.Objects;

public class ResponseMessage implements Message {
    private final Response response;
    private final String command;

    public ResponseMessage(Response response, String command) {
        this.response = Objects.requireNonNull(response);
        this.command = Objects.requireNonNull(command);
    }

    @Override
    public String getPayload() {
        JSONObject payloadJson = new JSONObject();
        payloadJson.put("type", "response");
        payloadJson.put("command", command);
        payloadJson.put("content", getContentJson());
        return payloadJson.toString();
    }

    private JSONObject getContentJson() {
        Encoder encoder = new JsonEncoder();
        JSONObject contentJson = new JSONObject();
        contentJson.put("message", response.message);
        if(response.encodable != null) {
            contentJson.put("data", response.encodable.encodeUsing(encoder));
        }
        return contentJson;
    }
}
