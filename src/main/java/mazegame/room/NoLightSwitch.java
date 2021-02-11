package mazegame.room;

import mazegame.exceptions.NoLightsException;

public class NoLightSwitch extends LightSwitch {
  private static final NoLightSwitch INSTANCE = new NoLightSwitch();

  private NoLightSwitch() {
    super(false);
  }

  public static NoLightSwitch getInstance() {
    return INSTANCE;
  }

  @Override
  public void toggleLights() {
    throw new NoLightsException("No lights to switch");
  }
}
