package mazegame.util;

import mazegame.ValidityResponse;
import mazegame.State;
import mazegame.mapsite.Checkable;
import mazegame.mapsite.Door;
import mazegame.mapsite.Lockable;
import mazegame.mapsite.MapSite;
import mazegame.mapsite.Seller;

public class ActionValidityChecker {
  private ActionValidityChecker() {}

  public static ValidityResponse canOpenDoor(MapSite mapSite, State state) {
    if (state != State.EXPLORE) {
      return new ValidityResponse(false, invalidStateMessage(state));
    }

    if (isNotDoor(mapSite)) {
      return new ValidityResponse(false, "Not a door!");
    }

    Door door = (Door) mapSite;
    return checkIfLocked(door);
  }

  private static boolean isNotDoor(MapSite mapSite) {
    return !(mapSite instanceof Door);
  }

  private static ValidityResponse checkIfLocked(Lockable lockable) {
    if(lockable.isLocked()) {
      String mapSiteType = lockable.getClass().getSimpleName();
      return new ValidityResponse(false,  mapSiteType + " is locked, " + lockable.getKeyName() + " key is needed to unlock");
    } else {
      return ValidityResponse.VALID_RESPONSE;
    }
  }

  public static ValidityResponse canCheck(MapSite mapSite, State state) {
    if (state != State.EXPLORE) {
      return new ValidityResponse(false, invalidStateMessage(state));
    }

    if (isNotCheckable(mapSite)) {
      return new ValidityResponse(false, "Nothing to check");
    }

    if(isLockable(mapSite)) {
      return checkIfLocked((Lockable) mapSite);
    }

    return ValidityResponse.VALID_RESPONSE;
  }

  private static boolean isNotCheckable(MapSite mapSite) {
    return !(mapSite instanceof Checkable);
  }

  private static boolean isLockable(MapSite mapSite) {
    return mapSite instanceof Lockable;
  }

  public static ValidityResponse canStartTrade(MapSite mapSite, State state) {
    if (state != State.EXPLORE) {
      return new ValidityResponse(false, invalidStateMessage(state));
    }

    if (isNotSeller(mapSite)) {
      return new ValidityResponse(false, "Not facing a seller!");
    }

    return ValidityResponse.VALID_RESPONSE;
  }

  private static boolean isNotSeller(MapSite mapSite) {
    return !(mapSite instanceof Seller);
  }

  public static ValidityResponse inTradeMode(State state) {
    if (state == State.TRADE) {
      return ValidityResponse.VALID_RESPONSE;
    } else {
      return new ValidityResponse(false, invalidStateMessage(state));
    }
  }

  public static ValidityResponse inExploreMode(State state) {
    if (state == State.EXPLORE) {
      return ValidityResponse.VALID_RESPONSE;
    } else {
      return new ValidityResponse(false, invalidStateMessage(state));
    }
  }

  private static String invalidStateMessage(State state) {
    return "Cannot perform action while in " + state.toString().toLowerCase() + " state";
  }
}
