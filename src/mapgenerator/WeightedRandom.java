package mapgenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SplittableRandom;

public class WeightedRandom {
    private final SplittableRandom random = new SplittableRandom();
    private final List<Integer> lookup = new ArrayList<>();
    private int eventNumber = 0;

    public WeightedRandom() {}

    public WeightedRandom(List<Integer> weights) {
        for(Integer weight : weights) {
            addWeight(weight);
        }
    }

    public void addWeight(int weight) {
        lookup.addAll(Collections.nCopies(weight, eventNumber));
        eventNumber++;
    }

    public int nextInt() {
        int bound = lookup.size();
        return lookup.get(random.nextInt(bound));
    }
}
