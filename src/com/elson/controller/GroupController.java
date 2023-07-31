package com.elson.controller;



import com.elson.client.Player;

import java.util.ArrayList;
import java.util.List;

public class GroupController {
    List<GameController> gameControllers; // groups controller
    int group; // number of groups

    public GroupController() {
        gameControllers = new ArrayList<>();
        group = 0;

    }

    /**
     * Add new player to a new group or a group that has not yet started playing
     * @param player player
     */
    public void addPlayer(Player player) {
        if (gameControllers.isEmpty()) {

            // Create a new gameController add player
            group++;
            GameController gameController = new GameController(group);
            player.setController(gameController);

            gameControllers.add(gameController);

        } else {

            GameController curGameController = gameControllers.get(gameControllers.size() - 1);
            // Check if the controller is still open
            if (curGameController.isOpen()) {
                player.setController(curGameController);

            } else {
                group++;
                GameController gameController = new GameController(group);
                player.setController(gameController);
                gameControllers.add(gameController);

            }
        }

    }

}
