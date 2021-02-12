package website.message;

import mazegame.State;
import org.json.JSONObject;

public class StateChangeMessage extends BasicMessage {
    private final State state;

    public StateChangeMessage(String content, State state) {
        super("stateChange", content);
        this.state = state;
    }

    @Override
    public String getPayload() {
        JSONObject messageJson = new JSONObject(super.getPayload());
        messageJson.put("state", state);
        return messageJson.toString();
    }
}
