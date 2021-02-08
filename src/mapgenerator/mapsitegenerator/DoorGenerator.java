package mapgenerator.mapsitegenerator;

import java.util.Objects;
import java.util.SplittableRandom;

import mapgenerator.RandomNameGenerator;
import org.json.JSONObject;

public class DoorGenerator implements MapSiteGenerator {
    private static final int PROBABILITY = 100;
    private static final int LOCKED_PROBABILITY = 25;
    private static final SplittableRandom random = new SplittableRandom();
    private final RandomNameGenerator randomNameGenerator;

    public DoorGenerator(RandomNameGenerator randomNameGenerator) {
        this.randomNameGenerator = Objects.requireNonNull(randomNameGenerator);
    }

    @Override
    public JSONObject generate() {
        JSONObject doorJson = getCustomJson(-1, -1, randomNameGenerator.getRandomName());
        doorJson.put("locked", isLocked());
        return doorJson;
    }

    private boolean isLocked() {
        int chance = random.nextInt(PROBABILITY);
        return chance < LOCKED_PROBABILITY;
    }

    public static JSONObject getCustomJson(int fromPosition, int toPosition, String keyName) {
        JSONObject doorJson = new JSONObject();
        doorJson.put("mapSite", "Door");
        doorJson.put("roomID", fromPosition);
        doorJson.put("otherRoomID", toPosition);
        doorJson.put("key", keyName);
        doorJson.put("locked", false);
        return doorJson;
    }

    public static JSONObject getOppositeDoor(JSONObject doorJson) {
        JSONObject oppositeDoor = new JSONObject(doorJson.toString());
        oppositeDoor.put("roomID", doorJson.get("otherRoomID"));
        oppositeDoor.put("otherRoomID", doorJson.get("roomID"));
        return oppositeDoor;
    }
}
