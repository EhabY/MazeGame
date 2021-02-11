package mazegame.room;

import mazegame.Direction;
import mazegame.item.Item;
import mazegame.mapsite.Loot;
import mazegame.mapsite.Lootable;
import mazegame.mapsite.SerializableMapSite;
import serialization.JsonEncodable;
import serialization.Encoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Room implements JsonEncodable, Lootable {
  private final int id;
  private final Map<Direction, SerializableMapSite> mapSites;
  private final LightSwitch lightSwitch;
  private Loot loot;

  public Room(int id, Map<Direction, SerializableMapSite> mapSites, LightSwitch lightSwitch) {
    this(id, mapSites, lightSwitch, Loot.EMPTY_LOOT);
  }

  public Room(int id, Map<Direction, SerializableMapSite> mapSites, LightSwitch lightSwitch, Loot loot) {
    this.id = id;
    this.mapSites = Collections.unmodifiableMap(new EnumMap<>(mapSites));
    this.lightSwitch = Objects.requireNonNull(lightSwitch);
    this.loot = Objects.requireNonNull(loot);
  }

  public SerializableMapSite getMapSite(Direction direction) {
    return mapSites.get(direction);
  }

  public boolean hasLights() {
    return !lightSwitch.equals(NoLightSwitch.getInstance());
  }

  public boolean isLit() {
    return lightSwitch.isOn();
  }

  public void toggleLights() {
    lightSwitch.toggleLights();
  }

  public int getId() {
    return id;
  }

  public void addLoot(Loot newLoot) {
    long totalGold = loot.getGold() + newLoot.getGold();
    List<Item> totalItems = new ArrayList<>(loot.getItems());
    totalItems.addAll(newLoot.getItems());
    this.loot = new Loot(totalGold, totalItems);
  }

  @Override
  public synchronized Loot acquireLoot() {
    Loot loot = this.loot;
    this.loot = Loot.EMPTY_LOOT;
    return loot;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Room room = (Room) o;
    return id == room.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String encodeUsing(Encoder encoder) {
    return encoder.visit(this);
  }

  @Override
  public String toString() {
      return "Room{" + "id=" + id + ", lightSwitch=" + lightSwitch + ", loot=" + loot +  '}';
  }
}
