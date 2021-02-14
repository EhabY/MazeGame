package mapgenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SplittableRandom;

public class RandomNameGenerator {

  private final SplittableRandom random = new SplittableRandom();
  private final List<String> names;

  public RandomNameGenerator(int numberOfNames) {
    List<String> names = Arrays
        .asList("Fury", "Sunshard", "Sleepwalker", "Trinity", "Deserted Seal",
            "Dire Baton", "Ghostly Idol", "Doombinder", "Frostguard", "Doomshadow", "Flamestone",
            "Nightkiss", "Lament", "Silverlight", "Spellbinder", "Soulshadow", "Echo", "Blazewing",
            "Dawnlight", "Lich Ornament", "Twinkle", "Flameward", "Suspension", "Desolation Harp",
            "Warrior Orb", "Malevolent Crux", "Splinter", "Spiteful Baton", "Eclipse");
    Collections.shuffle(names);
    this.names = names.subList(0, Math.min(numberOfNames, names.size()));
  }

  public RandomNameGenerator(Collection<String> names) {
    this.names = new ArrayList<>(names);
  }

  public String getRandomName() {
    int numberOfNames = names.size();
    return names.get(random.nextInt(numberOfNames));
  }

  public List<String> getAllNames() {
    return new ArrayList<>(this.names);
  }
}
