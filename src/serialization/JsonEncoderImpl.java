package serialization;

import mazegame.Direction;
import mazegame.MazeMap;
import mazegame.PlayerController;
import mazegame.item.Flashlight;
import mazegame.item.Item;
import mazegame.item.ItemManager;
import mazegame.item.Key;
import mazegame.mapsite.Chest;
import mazegame.mapsite.Door;
import mazegame.mapsite.Lockable;
import mazegame.mapsite.Loot;
import mazegame.mapsite.Mirror;
import mazegame.mapsite.Painting;
import mazegame.mapsite.Seller;
import mazegame.mapsite.Wall;
import mazegame.mapsite.SerializableMapSite;
import mazegame.player.Player;
import mazegame.room.Room;
import org.json.JSONArray;
import org.json.JSONObject;
import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class JsonEncoderImpl implements JsonEncoder {

    @Override
    public String visit(Flashlight flashlight) {
        JSONObject flashlightJson = getItemJson(flashlight);
        flashlightJson.put("turnedOn", flashlight.isTurnedOn());
        return flashlightJson.toString();
    }

    @Override
    public String visit(Key key) {
        return getItemJson(key).toString();
    }

    private JSONObject getItemJson(Item item) {
        JSONObject itemJson = new JSONObject();
        itemJson.put("name", item.getName());
        itemJson.put("type", item.getType());
        return itemJson;
    }

    @Override
    public String visit(Chest chest) {
        JSONObject chestJson = getLockableJson(chest);
        chestJson.put("mapSite", "Chest");
        Loot loot = (Loot) getPrivateField(chest, "loot");
        chestJson.put("loot", getLootJson(loot));
        return chestJson.toString();
    }

    @Override
    public String visit(Loot loot) {
        return getLootJson(loot).toString();
    }

    private JSONObject getLootJson(Loot loot) {
        JSONObject lootJson = new JSONObject();
        lootJson.put("gold", loot.getGold());
        lootJson.put("items", getItemListJson(loot.getItems()));
        return lootJson;
    }

    private JSONArray getItemListJson(List<Item> items) {
        JSONArray itemManagerJson = new JSONArray();
        for(Item item : items) {
            itemManagerJson.put(getItemJson(item));
        }
        return itemManagerJson;
    }

    @Override
    public String visit(Door door) {
        JSONObject doorJson = getLockableJson(door);
        doorJson.put("mapSite", "Door");
        Room room = (Room) getPrivateField(door, "room");
        doorJson.put("roomID", room.getId());
        Room otherRoom = (Room) getPrivateField(door, "otherRoom");
        doorJson.put("otherRoomID", otherRoom.getId());
        return doorJson.toString();
    }

    private JSONObject getLockableJson(Lockable lockable) {
        JSONObject lockableJson = new JSONObject();
        lockableJson.put("key", lockable.getKeyName());
        lockableJson.put("locked", lockable.isLocked());
        return lockableJson;
    }

    @Override
    public String visit(Mirror mirror) {
        JSONObject mirrorJson = new JSONObject();
        mirrorJson.put("mapSite", "Mirror");
        mirrorJson.put("hiddenKey", mirror.getKeyName());
        return mirrorJson.toString();
    }

    @Override
    public String visit(Painting painting) {
        JSONObject paintingJson = new JSONObject();
        paintingJson.put("mapSite", "Painting");
        paintingJson.put("hiddenKey", painting.getKeyName());
        return paintingJson.toString();
    }

    @Override
    public String visit(Seller seller) {
        JSONObject sellerJson = new JSONObject();
        sellerJson.put("mapSite", "Seller");
        ItemManager itemManager = (ItemManager) getPrivateField(seller, "itemManager");
        sellerJson.put("items", getItemListJson(itemManager.getItemList()));
        Map<String, Long> priceList = (Map<String, Long>) getPrivateField(seller, "priceList");
        sellerJson.put("priceList", getPriceListJson(priceList));
        return sellerJson.toString();
    }

    private JSONArray getPriceListJson(Map<String, Long> priceList) {
        JSONArray priceListJson = new JSONArray();
        for(Map.Entry<String, Long> listing : priceList.entrySet()) {
            priceListJson.put(getListingJson(listing.getKey(), listing.getValue()));
        }
        return priceListJson;
    }

    private JSONObject getListingJson(String name, Long price) {
        JSONObject listingJson = new JSONObject();
        listingJson.put("name", name);
        listingJson.put("price", price);
        return listingJson;
    }

    @Override
    public String visit(Wall wall) {
        JSONObject wallJson = new JSONObject();
        wallJson.put("mapSite", "Wall");
        return wallJson.toString();
    }

    @Override
    public String visit(PlayerController playerController) {
        JSONObject playerControllerJson = new JSONObject();
        playerControllerJson.put("mapConfiguration", getMapConfigurationJson(playerController));
        return playerControllerJson.toString();
    }

    private JSONObject getMapConfigurationJson(PlayerController playerController) {
        JSONObject mapConfigJson = new JSONObject();
        Player player = playerController.getPlayer();
        mapConfigJson.put("startRoomsID", new JSONArray().put(player.getCurrentRoom().getId()));
        MazeMap map = (MazeMap) getPrivateField(playerController, "map");
        mapConfigJson.put("endRoomID", map.getEndRoom().getId());
        mapConfigJson.put("time", map.getTimeInSeconds() - getElapsedTime(playerController));
        mapConfigJson.put("orientation", player.getDirection().toString().toLowerCase());
        Loot loot = player.getLoot();
        mapConfigJson.put("gold", loot.getGold());
        mapConfigJson.put("items", getItemListJson(loot.getItems()));
        return mapConfigJson;
    }

    private long getElapsedTime(PlayerController playerController) {
        Instant gameStart = (Instant) getPrivateField(playerController, "gameStart");
        return Duration.between(gameStart, Instant.now()).toMillis() / 1000;
    }

    @Override
    public String visit(Room room) {
        JSONObject roomJson = new JSONObject();
        roomJson.put("id", room.getId());
        roomJson.put("lightswitch", getLightSwitchJson(room));
        Loot loot = (Loot) getPrivateField(room, "loot");
        roomJson.put("loot", getLootJson(loot));
        for (Direction direction : Direction.values()) {
            SerializableMapSite serializableMapSite = room.getMapSite(direction);
            JSONObject mapSiteJson = new JSONObject(serializableMapSite.applyEncoder(this));
            roomJson.put(direction.toString().toLowerCase(), mapSiteJson);
        }
        return roomJson.toString();
    }

    private JSONObject getLightSwitchJson(Room room) {
        JSONObject lightSwitchJson = new JSONObject();
        lightSwitchJson.put("hasLights", room.hasLights());
        lightSwitchJson.put("lightsOn", room.isLit());
        return lightSwitchJson;
    }

    private Object getPrivateField(Object object, String fieldName) {
        Field field;
        try {
            field = object.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Field " + fieldName + " does not exist!");
        }

        field.setAccessible(true);

        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new IllegalStateException("Could not access " + fieldName);
        }
    }
}
