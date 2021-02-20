package website.services.fighting;

import mazegame.PlayerController;

public class RockPaperScissors implements TieBreaker {

  private static final String TIE_FIGHT_MESSAGE = "There was a tie, prepare for a rock-paper-scissors game!";

  @Override
  public PlayerController breakTie(PlayerController playerController1,
      PlayerController playerController2) {
    playerController1.tieFight(TIE_FIGHT_MESSAGE);
    playerController2.tieFight(TIE_FIGHT_MESSAGE);

    while (true) {
      playerController1.requestingInput("Rock, paper, or scissors?");
      playerController2.requestingInput("Rock, paper, or scissors?");

      String shape1 = getInput(playerController1);
      String shape2 = getInput(playerController2);

      if (firstBeatsSecond(shape1, shape2)) {
        return playerController1;
      } else if (firstBeatsSecond(shape2, shape1)) {
        return playerController2;
      }
    }
  }

  private boolean firstBeatsSecond(String shape1, String shape2) {
    return shape1.equalsIgnoreCase("Rock") && shape2.equalsIgnoreCase("Scissors")
        || shape1.equalsIgnoreCase("Paper") && shape2.equalsIgnoreCase("Rock")
        || shape1.equalsIgnoreCase("Scissors") && shape2.equalsIgnoreCase("Paper");
  }

  private String getInput(PlayerController playerController) {
    String message = null;
    while (message == null) {
      try {
        message = playerController.getNextCommand();
      } catch (InterruptedException e) {
        // interrupted, proceed normally
      }
    }

    return message;
  }
}
