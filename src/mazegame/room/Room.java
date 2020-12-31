package mazegame.room;

import mazegame.Direction;
import mazegame.JsonSerializable;
import mazegame.mapsite.MapSite;
import mazegame.mapsite.SerializableMapSite;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class Room implements JsonSerializable {
  private final int id;
  private final Map<Direction, SerializableMapSite> mapSites;
  private final LightSwitch lightSwitch;

  public Room(int id, Map<Direction, SerializableMapSite> mapSites, LightSwitch lightSwitch) {
    this.id = id;
    this.mapSites = Collections.unmodifiableMap(new EnumMap<>(mapSites));
    this.lightSwitch = Objects.requireNonNull(lightSwitch);
  }

  public MapSite getMapSite(Direction direction) {
    return mapSites.get(direction);
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
  public String toJson() {
    StringBuilder roomJson = new StringBuilder("{\"id\": " + id);
    roomJson.append(",\"lightswitch\": ").append(lightSwitch.toJson());
    for (Direction direction : Direction.values()) {
      roomJson.append(",\"").append(direction.toString().toLowerCase()).append("\": ");
      roomJson.append(mapSites.get(direction).toJson());
    }

    return roomJson.toString() + "}";
  }

  @Override
  public String toString() {
    return "Room{" + "id=" + id + ", lightSwitch=" + lightSwitch + '}';
  }
}
