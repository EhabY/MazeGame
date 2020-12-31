package mazegame.room;

import mazegame.JsonSerializable;

public class LightSwitch implements JsonSerializable {
  private boolean lightsOn;

  public LightSwitch(boolean lightsOn) {
    this.lightsOn = lightsOn;
  }

  public void toggleLights() {
    this.lightsOn = !this.lightsOn;
  }

  public boolean isOn() {
    return lightsOn;
  }

  @Override
  public String toJson() {
    return "{" + "\"hasLights\": true," + "\"lightsOn\": " + lightsOn + "}";
  }

  @Override
  public String toString() {
    return "LightSwitch{" + "lightsOn=" + lightsOn + '}';
  }
}
