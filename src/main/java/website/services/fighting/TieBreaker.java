package website.services.fighting;

import mazegame.PlayerController;

public interface TieBreaker {

  PlayerController breakTie(PlayerController playerController1, PlayerController playerController2);
}
