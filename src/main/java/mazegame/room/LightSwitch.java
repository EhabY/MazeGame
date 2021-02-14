package mazegame.room;

public class LightSwitch {

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
  public String toString() {
    return "LightSwitch{" + "lightsOn=" + lightsOn + '}';
  }
}
