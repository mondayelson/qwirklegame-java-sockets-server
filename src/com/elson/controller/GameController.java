package com.elson.controller;


import com.elson.client.Player;
import com.elson.client.PlayerData;
import com.elson.messages.Package;
import com.elson.qwirkle.Position;
import com.elson.qwirkle.Qwirkle;
import com.elson.qwirkle.Tile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameController {

    private List<Player> players;
    private final Qwirkle qwirkle;
    private StartTimer startTimer;
    private int turn;
    private int group;
    private int playerID;


    private boolean isOpen;

    public GameController(int group) {
        playerID = 0;
        this.group = group;
        turn = 1;
        players = new ArrayList<>();
        qwirkle = new Qwirkle();
        isOpen = true;

    }
    /**
     * Receive package from a player
     *
     * @param player player
     * @param p      package
     */
    public void receive(Player player, Package p) {

        switch (p.command) {

            case "player-name": {
                player.name = (String) p.data.get("name");
                addPlayer(player);
                displayNumberInBag();
                break;
            }
            case "selected-tile": {

                Tile tile = (Tile) p.data.get("selected-tile");

                display(player.name, "Selected a " + tile);

                // Send valid positions to player
                List<Position> positions = qwirkle.validPositions(tile);

                String command = "valid-positions";
                Map<String, Object> data = new HashMap<>();
                data.put("valid-positions", positions);

                player.send(new Package(command, data));

                break;
            }
            case "placing-tile": {

                // Place tile on board
                Tile t = (Tile) p.data.get("selected-tile");

                t.setPlacement(3);
                qwirkle.place(t);

                List<Tile> board = qwirkle.getBoardTiles();
                List<Tile> temp = new ArrayList<>();

                for (Tile tile : board) {
                    Tile cur = new Tile(tile.color, tile.shape);
                    cur.placement = tile.placement;
                    cur.setPosition(tile.row, tile.column);
                    temp.add(cur);
                }

                // Send updated board to players
                Map<String, Object> data = new HashMap<>();
                data.put("tile", t);
                data.put("board", temp);
                sendToAll(new Package("on-board", data));

                display(player.name, "is placing a tile at position " + t.strPos());
                break;
            }
            case "place-tiles": {

                String command = "player-score";
                Map<String, Object> data = new HashMap<>();

                // Determine scores and send to all players
                List<Tile> list = qwirkle.placingList();
                data.put("placing-list", list);
                data.put("player-number", player.playerNo);
                int gain = qwirkle.calculateScore();
                player.score += gain;
                data.put("gain", gain);
                data.put("player-score", player.score);
                data.put("name", player.name);

                sendToAll(new Package(command, data));
                // Display scores gained
                display(player.name, "placed tile(s)," +
                        " and gained "
                        + gain + " points");


                // Check if game over, else send new hand tiles to player
                int count = (int) p.data.get("num-new");
                if (qwirkle.isBagEmpty() && count == 6) {

                    display("", "game has ended");

                    player.score+=6;
                    gameOver();
                } else {
                    Map<String, Object> playerData = new HashMap<>();
                    List<Tile> newT = qwirkle.newNumTiles(count);
                    playerData.put("new-tiles", newT);

                    // Send to player
                    send(player, new Package("hand-update", playerData));
                    displayNumberInBag();

                    notifyTurnAll();
                }
                break;
            }
            case "trade-tiles": {

                String command = "trade-new";
                Map<String, Object> data = new HashMap<>();

                // Trade tiles
                data.put("new", qwirkle.trade((List<Tile>) p.data.get("tiles")));

                send(player, new Package(command, data));

                display(player.name, "traded tile(s)");

                displayNumberInBag();
                break;
            }
        }

    }

    public boolean isOpen() {
        return isOpen;
    }

    public void runTimer() {

        startTimer = null;

        startTimer = new StartTimer(30);
        startTimer.start();
    }

    /**
     * Add new player
     *
     * @param p player
     */
    public void addPlayer(Player p) {

        // Add player
        playerID++;
        p.setPlayerNo(playerID);
        p.setGroupNo(group);
        players.add(p);

        // run timer if at least two have joined
        if (players.size() == 2)
            runTimer();

        // closed group if 4 players have joined
        if (players.size() == 4)
            isOpen = false;

        // Server display message
        display(p.name, "joined game");

        // Notify other player that a new player has been added
        players.forEach(player -> {

            for (Player ply : players) {
                String command = "player-joined";
                Map<String, Object> data = new HashMap<>();
                data.put("player-number", ply.playerNo);
                data.put("name", ply.name);

                send(player, new Package(command, data));
            }
        });

        // Send player initial data to the player
        Map<String, Object> playerData = new HashMap<>();
        playerData.put("score", 0);
        p.hand = qwirkle.createNewHand();
        playerData.put("hand", p.hand);

        playerData.put("player-number", p.playerNo);
        send(p, new Package("player-data", playerData));
    }


    /**
     * Send package to player
     *
     * @param player player
     * @param p      package
     */
    public void send(Player player, Package p) {

        try {
            player.outMessages.put(p);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Send package to all players in the group
     *
     * @param p package
     */
    private void sendToAll(Package p) {
        for (Player player : players)
            player.send(p);
    }

    /**
     * Display number of tiles in bag
     */
    public void displayNumberInBag() {
        System.out.println(">> Tiles in bag = " + qwirkle.numberInBag());

        Map<String, Object> data = new HashMap<>();
        data.put("number-bag", qwirkle.numberInBag());

        sendToAll(new Package("number-bag", data));

    }

    /**
     * Update turn and notify players
     */
    private void notifyTurnAll() {
        if (turn == players.size())
            turn = 1;
        else turn += 1;

        Map<String, Object> data = new HashMap<>();
        data.put("turn", turn);

        sendToAll(new Package("turn", data));

    }

    /**
     * Send package with player data once the game is over
     */
    private void gameOver() {
        List<PlayerData> list = new ArrayList<>();
        for (Player p : players)
            list.add(new PlayerData(p.name, p.score));

        Map<String, Object> data = new HashMap<>();
        data.put("players", list);

        sendToAll(new Package("game-over", data));
    }

    /**
     * Determines who starts tha game
     *
     * @return player number
     */
    public int whoStarts() {
        int index = 0;
        int max = -1;

        for (Player player : players) {

            List<Tile> hand = player.hand;
            int colorCount = 0;
            int shapeCount = 0;

            for (Tile tile : hand) {
                int colors = (int) hand.stream()
                        .filter(tile1 -> tile.color == tile1.color)
                        .count();

                if (colors > colorCount)
                    colorCount = colors;

                int shapes = (int) hand.stream()
                        .filter(tile1 -> tile.shape == tile1.shape)
                        .count();

                if (shapes > shapeCount)
                    shapeCount = shapes;
            }

            int countMax = Math.max(colorCount, shapeCount);

            if (countMax > max) {
                max = countMax;
                index = players.indexOf(player);
            }

        }

        turn=players.get(index).playerNo;
        return turn;

    }

    private void display(String name, String msg) {
        System.out.println(">> Group " + group + " " + name + " " + msg);
    }

    /**
     * Start game timer thread
     */
    private class StartTimer extends Thread {
        private int seconds, counter;

        public StartTimer(int seconds) {
            this.seconds = seconds;
            counter = 0;
        }

        @Override

        public void run() {

            while (true) {
                if (seconds != counter) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    counter += 1;

                    Map<String, Object> data = new HashMap<>();
                    data.put("time", counter);
                    GameController.this.sendToAll(new Package("time", data));
                } else {


                    isOpen = false;
                    Map<String, Object> data = new HashMap<>();
                    data.put("turn", whoStarts());

                    sendToAll(new Package("turn", data));

                    System.out.println(">> Group " + group + " game has started");

                    break;
                }
            }

        }
    }


}
