package mazegame.util;

import mazegame.Response;
import mazegame.State;
import mazegame.mapsite.Checkable;
import mazegame.mapsite.Door;
import mazegame.mapsite.Lockable;
import mazegame.mapsite.MapSite;
import mazegame.mapsite.Seller;

public class ActionValidityChecker {
  private ActionValidityChecker() {}

  public static Response canOpenDoor(MapSite mapSite, State state) {
    if (state != State.EXPLORE) {
      return new Response(false, invalidStateMessage(state));
    }

    if (isNotDoor(mapSite)) {
      return new Response(false, "Not a door!");
    }

    Door door = (Door) mapSite;
    return checkIfLocked(door);
  }

  private static boolean isNotDoor(MapSite mapSite) {
    return !(mapSite instanceof Door);
  }

  private static Response checkIfLocked(Lockable lockable) {
    if(lockable.isLocked()) {
      String mapSiteType = lockable.getClass().getSimpleName();
      return new Response(false,  mapSiteType + " is locked, " + lockable.getKeyName() + " key is needed to unlock");
    } else {
      return Response.VALID_RESPONSE;
    }
  }

  public static Response canCheck(MapSite mapSite, State state) {
    if (state != State.EXPLORE) {
      return new Response(false, invalidStateMessage(state));
    }

    if (isNotCheckable(mapSite)) {
      return new Response(false, "Nothing to check");
    }

    if(isLockable(mapSite)) {
      return checkIfLocked((Lockable) mapSite);
    }

    return Response.VALID_RESPONSE;
  }

  private static boolean isNotCheckable(MapSite mapSite) {
    return !(mapSite instanceof Checkable);
  }

  private static boolean isLockable(MapSite mapSite) {
    return mapSite instanceof Lockable;
  }

  public static Response canStartTrade(MapSite mapSite, State state) {
    if (state != State.EXPLORE) {
      return new Response(false, invalidStateMessage(state));
    }

    if (isNotSeller(mapSite)) {
      return new Response(false, "Not facing a seller!");
    }

    return Response.VALID_RESPONSE;
  }

  private static boolean isNotSeller(MapSite mapSite) {
    return !(mapSite instanceof Seller);
  }

  public static Response inTradeMode(State state) {
    if (state == State.TRADE) {
      return Response.VALID_RESPONSE;
    } else {
      return new Response(false, invalidStateMessage(state));
    }
  }

  public static Response inExploreMode(State state) {
    if (state == State.EXPLORE) {
      return Response.VALID_RESPONSE;
    } else {
      return new Response(false, invalidStateMessage(state));
    }
  }

  private static String invalidStateMessage(State state) {
    return "Cannot perform action while in " + state.toString().toLowerCase() + " state";
  }
}
