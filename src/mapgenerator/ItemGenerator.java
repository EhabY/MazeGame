package mapgenerator;

import org.json.JSONObject;

public class ItemGenerator {

    public static JSONObject getFlashlightJson() {
        JSONObject flashlightJson = new JSONObject();
        flashlightJson.put("name", "Flashlight");
        flashlightJson.put("type", "Flashlight");
        return flashlightJson;
    }

    public static JSONObject getKeyJson(String name) {
        JSONObject keyJson = new JSONObject();
        keyJson.put("name", name);
        keyJson.put("type", "Key");
        return keyJson;
    }
}
