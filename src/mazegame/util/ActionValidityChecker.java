package mazegame.util;

import mazegame.Response;
import mazegame.State;
import mazegame.mapsite.Checkable;
import mazegame.mapsite.Door;
import mazegame.mapsite.MapSite;
import mazegame.mapsite.Seller;

public class ActionValidityChecker {
  private ActionValidityChecker() {}

  public static Response canMove(MapSite mapSite, State state) {
    Response response = canOpenDoor(mapSite, state);
    if (!response.valid) {
      return response;
    }

    Door door = (Door) mapSite;
    if (door.isLocked()) {
      return new Response(false, "Door is locked");
    }

    return Response.VALID_RESPONSE;
  }

  public static Response canOpenDoor(MapSite mapSite, State state) {
    if (state != State.EXPLORE) {
      return new Response(
          false, "Cannot perform action while in " + state.toString().toLowerCase() + " state");
    }

    if (isNotDoor(mapSite)) {
      return new Response(false, "Not a door!");
    }

    return Response.VALID_RESPONSE;
  }

  private static boolean isNotDoor(MapSite mapSite) {
    return !(mapSite instanceof Door);
  }

  public static Response canCheck(MapSite mapSite, State state) {
    if (state != State.EXPLORE) {
      return new Response(
          false, "Cannot perform action while in " + state.toString().toLowerCase() + " state");
    }

    if (isNotCheckable(mapSite)) {
      return new Response(false, "Nothing to check");
    }

    return Response.VALID_RESPONSE;
  }

  private static boolean isNotCheckable(MapSite mapSite) {
    return !(mapSite instanceof Checkable);
  }

  public static Response canStartTrade(MapSite mapSite, State state) {
    if (state != State.EXPLORE) {
      return new Response(
          false, "Cannot perform action while in " + state.toString().toLowerCase() + " state");
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
      return new Response(false, "Not in trade mode");
    }
  }
}
