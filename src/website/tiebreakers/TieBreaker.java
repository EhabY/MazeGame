package website.tiebreakers;

import mazegame.PlayerController;

public interface TieBreaker {
    PlayerController breakTie(PlayerController playerController1, PlayerController playerController2);
}
