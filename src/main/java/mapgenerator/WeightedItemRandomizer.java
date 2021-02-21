package mapgenerator;

import java.util.ArrayList;
import java.util.List;

public class WeightedItemRandomizer<T> {

  private final WeightedRandom randomizer = new WeightedRandom();
  private final List<T> items = new ArrayList<>();

  public void addEvent(T item, int weight) {
    items.add(item);
    randomizer.addWeight(weight);
  }

  T nextElement() {
    if (items.isEmpty()) {
      throw new IllegalStateException("No items to return!");
    }
    return items.get(randomizer.nextInt());
  }


}
