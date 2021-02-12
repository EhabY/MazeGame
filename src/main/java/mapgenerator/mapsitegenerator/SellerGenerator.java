package mapgenerator.mapsitegenerator;

import mapgenerator.ItemGenerator;
import mapgenerator.RandomNameGenerator;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Collections;
import java.util.List;
import java.util.SplittableRandom;

public class SellerGenerator implements MapSiteGenerator {
    private static final int PROBABILITY = 100;
    private static final int FLASHLIGHT_PROBABILITY = 50;
    private final SplittableRandom random = new SplittableRandom();
    private static final int PRICE_BOUND = 15;
    private final List<String> itemNames;

    public SellerGenerator(RandomNameGenerator randomNameGenerator) {
        this.itemNames = randomNameGenerator.getAllNames();
        Collections.shuffle(this.itemNames);
    }

    @Override
    public JSONObject generate() {
        JSONObject sellerJson = new JSONObject();
        sellerJson.put("mapSite", "Seller");
        sellerJson.put("items", getHalfItemsList());
        sellerJson.put("priceList", getPriceList());
        return sellerJson;
    }

    private JSONArray getHalfItemsList() {
        JSONArray itemsJson = new JSONArray();
        List<String> halfItemNames = itemNames.subList(0, (itemNames.size() + 1)/2);
        for(String name : halfItemNames) {
            itemsJson.put(ItemGenerator.getKeyJson(name));
        }
        addFlashlight(itemsJson);
        return itemsJson;
    }

    private void addFlashlight(JSONArray itemsJson) {
        int chance = random.nextInt(PROBABILITY);
        if(chance < FLASHLIGHT_PROBABILITY) {
            itemsJson.put(ItemGenerator.getFlashlightJson());
        }
    }

    private JSONArray getPriceList() {
        JSONArray priceListJson = new JSONArray();
        for(String name : itemNames) {
            priceListJson.put(getItemPrice(name, "Key"));
        }
        priceListJson.put(getItemPrice("", "Flashlight"));
        return priceListJson;
    }

    private JSONObject getItemPrice(String name, String type) {
        JSONObject priceListing = new JSONObject();
        String nameType = name + (name.equals("") ? "" : " ") + type;
        priceListing.put("name", nameType);
        priceListing.put("price", random.nextInt(PRICE_BOUND) + 1);
        return priceListing;
    }
}
