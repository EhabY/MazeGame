package mapgenerator.mapsitegenerator;

import mapgenerator.ItemGenerator;
import mapgenerator.RandomNameGenerator;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

public class SellerGenerator implements MapSiteGenerator {
    private static final int PROBABILITY = 100;
    private static final int FLASHLIGHT_PROBABILITY = 75;
    private final SplittableRandom random = new SplittableRandom();
    private static final int PRICE_BOUND = 6;
    private final List<String> itemNames;

    public SellerGenerator(RandomNameGenerator randomNameGenerator) {
        this.itemNames = new ArrayList<>(randomNameGenerator.getHalfOfAllNames());
    }

    @Override
    public JSONObject generate() {
        JSONObject sellerJson = new JSONObject();
        sellerJson.put("mapSite", "Seller");
        sellerJson.put("items", getItemsList());
        sellerJson.put("priceList", getPriceList());
        return sellerJson;
    }

    private JSONArray getItemsList() {
        JSONArray itemsJson = new JSONArray();
        for(String name : itemNames) {
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
            priceListJson.put(getItemPrice(name));
        }
        return priceListJson;
    }

    private JSONObject getItemPrice(String name) {
        JSONObject priceListing = new JSONObject();
        priceListing.put("name", name);
        priceListing.put("price", random.nextInt(PRICE_BOUND) + 1);
        return priceListing;
    }
}
