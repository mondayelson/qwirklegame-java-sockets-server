package com.elson.qwirkle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Qwirkle {
    private List<Tile> tiles; // all tiles
    private List<Tile> firstTwo; // first two tiles to be placed per turn

    public Qwirkle() {
        createTiles();

        firstTwo = new ArrayList<>();
    }

    /**
     * Generate tiles
     */
    private void createTiles() {
        tiles = new ArrayList<>();

        for (int x = 1; x <=3 ; x++) {
            for (int color = 1; color <= 6; color++)
                for (int shape = 1; shape <= 6; shape++)
                    tiles.add(new Tile(color, shape));
        }

        Collections.shuffle(tiles);

    }

    /**
     * Trade tiles
     * returns new tiles
     *
     * @param tradeList player tiles to trade
     * @return new tiles
     */
    public List<Tile> trade(List<Tile> tradeList) {
        List<Tile> newTiles=new ArrayList<>();

        // ***********
        if(isBagEmpty())
            return newTiles;


        if (tradeList.size() == 6) {

            // Replace
            tradeList.forEach(tile -> {

                for (Tile t : tiles) {
                    if (t.color == tile.color
                            && t.shape == tile.shape
                            && t.placement == 2) {
                        t.setPlacement(1);
                        break;
                    }
                }

            });

            // Get new
            newTiles = createNewHand();

        } else {


            // Get New
            newTiles = new ArrayList<>();

            Tile traded = getBagTiles().get(0);
            traded.setPlacement(2);
            tiles.set(tiles.indexOf(traded), traded);
            newTiles.add(traded);

            // Replace
            Tile tile = tradeList.get(0);

            for (Tile cur : tiles) {
                if (cur.color == tile.color
                        && cur.shape == tile.shape
                        && cur.placement == 2) {
                    cur.setPlacement(1);
                    break;
                }
            }

        }

        return newTiles;
    }

    /**
     * Get tiles in bag
     *
     * @return list
     */
    private List<Tile> getBagTiles() {

        List<Tile> bag = tiles.stream()
                .filter(tile -> tile.placement == 1)
                .collect(Collectors.toList());
        Collections.shuffle(bag);

        return bag;
    }

    /**
     * Get tiles on the board or grid
     *
     * @return list
     */
    public List<Tile> getBoardTiles() {

        List<Tile> boardTiles = new ArrayList<>();

        for (Tile tile : tiles) {
            if (tile.placement == 3 || tile.placement == 4)
                boardTiles.add(tile);
        }

        return boardTiles;

    }

    /**
     * Get tile a tile at specified position
     *
     * @param row tile row
     * @param col tile col
     * @return tile , null if not found
     */
    private Tile getTile(int row, int col) {
        List<Tile> list = tiles.stream()
                .filter(tile -> tile.row == row
                        && tile.column == col)
                .collect(Collectors.toList());

        if (list.isEmpty())
            return null;
        else return list.get(0);

    }

    /**
     * Checks if the bag is empty
     *
     * @return true if empty, false otherwise
     */
    public boolean isBagEmpty() {
        return tiles.stream().noneMatch(tile -> tile.placement == 1);
    }

    /**
     * @return number of tiles in bag
     */
    public int numberInBag() {
        return (int) tiles.stream()
                .filter(tile -> tile.placement == 1)
                .count();
    }

    /**
     * Get the list of  tiles that are currently being placed
     *
     * @return list
     */
    public List<Tile> placingList() {

        List<Tile> list = tiles.stream().
                filter(tile -> tile.placement == 3)
                .collect(Collectors.toList());

      /*  for (Tile tile : list) {
            tile.setPlacement(3);
            tiles.set(tiles.indexOf(tile), tile);
        }*/

        return list;
    }

    /**
     * Calculate player score
     *
     * @return score
     */
    public int calculateScore() {

        int score = 0;

        List<Integer> visitedColumns = new ArrayList<>();
        List<Integer> visitedRows = new ArrayList<>();

        List<Tile> placing = tiles.stream()
                .filter(tile -> tile.placement == 3)
                .collect(Collectors.toList());
        List<Tile> board = getBoardTiles();

        List<Tile> sameColumn, sameRow;

        for (Tile cur : placing) {

            sameColumn = connectedColumn(cur, board);
            sameRow = connectedRow(cur, board);

            //  cur.setPlacement(4);

            if (!visitedColumns.contains(cur.column)) {
                visitedColumns.add(cur.column);

                long sameColorColumn = sameColumn.stream()
                        .filter(tile -> tile.color == cur.color)
                        .count() + 1;

                if (sameColorColumn == 1) {
                    long sameShapeColumn = sameColumn.stream()
                            .filter(tile -> tile.shape == cur.shape)
                            .count() + 1;


                    if (sameShapeColumn != 1) {
                        if (sameShapeColumn == 6)
                            score += 12;
                        else score += sameShapeColumn;

                    }

                } else {

                    if (sameColorColumn == 6)
                        score += 12;
                    else score += sameColorColumn;

                }

            }
            if (!visitedRows.contains(cur.row)) {
                visitedRows.add(cur.row);

                long sameColorRow = sameRow.stream()
                        .filter(tile -> tile.color == cur.color)
                        .count() + 1;
                if (sameColorRow == 1) {
                    long sameShapeRow = sameRow.stream()
                            .filter(tile -> tile.shape == cur.shape)
                            .count() + 1;

                    if (sameShapeRow != 1) {
                        if (sameShapeRow == 6)
                            score += 12;
                        else score += sameShapeRow;
                    }

                } else {

                    if (sameColorRow == 6)
                        score += 12;
                    else score += sameColorRow;

                }

            }
        }


        firstTwo.clear();

        for (Tile tile : placing) {
            tile.placement = 4;
            tiles.set(tiles.indexOf(tile), tile);
        }

        return score;
    }

    /**
     * Place a tile on the grid
     *
     * @param selectedTile selected tile
     */
    public void place(Tile selectedTile) {

        for (Tile tile : tiles) {
            if (tile.color == selectedTile.color
                    && tile.shape == selectedTile.shape
                    && tile.placement == 2) {
                tiles.set(tiles.indexOf(tile), selectedTile);
                break;
            }
        }


        if (firstTwo.size() < 2)
            firstTwo.add(selectedTile);

    }

    /**
     * Get a list of valid positions
     *
     * @param selectedTile tile to compare
     * @return positions
     */
    public List<Position> validPositions(Tile selectedTile) {

        List<Position> positions = new ArrayList<>();

        List<Tile> onBoardTiles;


        if (firstTwo.isEmpty())
            onBoardTiles = tiles.stream()
                    .filter(tile -> tile.placement == 4)
                    .collect(Collectors.toList());

        else {

            onBoardTiles = connectedTiles(firstTwo.get(0));
            onBoardTiles.add(firstTwo.get(0));
        }

        if (onBoardTiles.isEmpty())
            positions = null;
        else {

            List<Tile> list = tiles.stream()
                    .filter(tile -> tile.placement == 3
                            || tile.placement == 4)
                    .collect(Collectors.toList());

            for (Tile tile : onBoardTiles) {

                int row = tile.row;
                int column = tile.column;

                int topRow, botRow, leftCol, rightCol;

                topRow = row - 1;
                botRow = row + 1;
                leftCol = column - 1;
                rightCol = column + 1;

                if (getTile(topRow, column) == null) {
                    Tile tempTile = new Tile(selectedTile.color, selectedTile.shape);
                    tempTile.setPosition(topRow, column);
                    if (validColumn(tempTile, list) && validRow(tempTile, list))
                        positions.add(new Position(topRow, column));
                }
                if (getTile(botRow, column) == null) {
                    Tile tempTile = new Tile(selectedTile.color, selectedTile.shape);
                    tempTile.setPosition(botRow, column);
                    if (validColumn(tempTile, list) && validRow(tempTile, list))
                        positions.add(new Position(botRow, column));
                }
                if (getTile(row, leftCol) == null) {
                    Tile tempTile = new Tile(selectedTile.color, selectedTile.shape);
                    tempTile.setPosition(row, leftCol);
                    if (validColumn(tempTile, list) && validRow(tempTile, list))
                        positions.add(new Position(row, leftCol));
                }
                if (getTile(row, rightCol) == null) {
                    Tile tempTile = new Tile(selectedTile.color, selectedTile.shape);
                    tempTile.setPosition(row, rightCol);
                    if (validColumn(tempTile, list) && validRow(tempTile, list))
                        positions.add(new Position(row, rightCol));
                }

            }


        }


        if (positions == null)
            return positions;
        else if (positions.isEmpty())
            return positions;

        if (firstTwo.isEmpty())
            return positions;

        else if (firstTwo.size() == 1) {
            List<Position> list = new ArrayList<>();

            Tile first = firstTwo.get(0);

            for (Position position : positions) {
                if (position.row == first.row || position.column == first.column)
                    list.add(position);
            }

            return list;
        } else {

            if (firstTwo.get(0).row == firstTwo.get(1).row)
                return filterRowPositions(positions, firstTwo.get(0).row);
            else return filterColPositions(positions, firstTwo.get(0).column);
        }


    }

    private List<Position> filterRowPositions(List<Position> positions, int row) {
        List<Position> list = new ArrayList<>();

        for (Position position : positions)
            if (position.row == row)
                list.add(position);

        return list;
    }

    private List<Position> filterColPositions(List<Position> positions, int col) {
        List<Position> list = new ArrayList<>();

        for (Position position : positions)
            if (position.column == col)
                list.add(position);

        return list;
    }

    private boolean validColumn(Tile tempTile, List<Tile> list) {


        long sameColor = 0;

        List<Tile> sameList;

        sameList = connectedColumn(tempTile, list);

        if (sameList.isEmpty())
            return true;


        for (Tile tile : sameList) {
            if (tile.color == tempTile.color && tile.shape != tempTile.shape)
                sameColor++;
            else {
                sameColor = 0;
                break;
            }
        }


        long sameShape = 0;

        for (Tile tile : sameList) {
            if (tile.color != tempTile.color && tile.shape == tempTile.shape)
                sameShape++;
            else {
                sameShape = 0;
                break;
            }
        }


        if (sameShape != 0 && sameColor == 0)
            return true;
        else if (sameShape == 0 && sameColor != 0)
            return true;


        return false;
    }


    private boolean validRow(Tile tempTile, List<Tile> list) {

        List<Tile> sameList;
        long sameColor = 0;

        sameList = connectedRow(tempTile, list);
        if (sameList.isEmpty())
            return true;

        for (Tile tile : sameList) {
            if (tile.color == tempTile.color && tile.shape != tempTile.shape)
                sameColor++;
            else {
                sameColor = 0;
                break;
            }
        }

        long sameShape = 0;

        for (Tile tile : sameList) {
            if (tile.color != tempTile.color && tile.shape == tempTile.shape)
                sameShape++;
            else {
                sameShape = 0;
                break;
            }
        }

        if (sameShape != 0 && sameColor == 0)
            return true;
        else if (sameShape == 0 && sameColor != 0)
            return true;

        return false;
    }

    public List<Tile> newNumTiles(int num) {
        List<Tile> bag = getBagTiles();


        List<Tile> list = new ArrayList<>();

        if (bag.isEmpty())
            return list;
        else if (bag.size() <= num)

            for (Tile tile : bag) {
                tile.placement = 2;
                list.add(tile);
                tiles.set(tiles.indexOf(tile), tile);
            }

        else {

            for (int x = 1; x <= num; x++) {
                Tile tile = bag.get(x - 1);
                tile.placement = 2;
                list.add(tile);
                tiles.set(tiles.indexOf(tile), tile);
            }
        }

        return list;
    }

    /**
     * Get connected tiles
     *
     * @param tile tile
     * @return connected tiles
     */
    private List<Tile> connectedTiles(Tile tile) {
        List<Tile> list = new ArrayList<>(connectedColumn(tile, tiles));
        list.addAll(connectedRow(tile, tiles));

        return list;
    }

    private List<Tile> connectedColumn(Tile tile, List<Tile> list) {
        int row = tile.row;
        int column = tile.column;
        List<Tile> connectedList = new ArrayList<>();


        if (getTile(row - 1, column) == null && getTile(row + 1, column) == null)
            return connectedList;
        else {

            List<Tile> same = list.stream()
                    .filter(t -> t.column == column)
                    .collect(Collectors.toList());

            if (getTile(row - 1, column) != null) {

                int minRow=getTile(row - 1, column).row;
                try {
                    if (same.size() == 1)
                        minRow = getTile(row - 1, column).row;
                    else minRow = same.stream()
                            .min(Comparator.comparing(t -> t.row))
                            .get()
                            .row;
                }
                catch (Exception e)
                {

                }


                for (int r = row - 1; r >= minRow; r--) {

                    Tile cur = getTile(r, column);

                    if (cur == null)
                        break;

                    connectedList.add(cur);
                }
            }

            if (getTile(row + 1, column) != null) {

                int maxRow = same.stream()
                        .max(Comparator.comparing(t -> t.row))
                        .get()
                        .row;

                for (int r = row + 1; r <= maxRow; r++) {

                    Tile cur = getTile(r, column);

                    if (cur == null)
                        break;

                    connectedList.add(cur);

                }
            }

            int v = 0;

        }

        return connectedList;
    }

    private List<Tile> connectedRow(Tile tile, List<Tile> list) {
        int row = tile.row;
        int column = tile.column;
        List<Tile> connectedList = new ArrayList<>();


        if (getTile(row, column - 1) == null && getTile(row, column + 1) == null)
            return connectedList;
        else {

            List<Tile> same = list.stream()
                    .filter(t -> t.row == row)
                    .collect(Collectors.toList());

            if (getTile(row, column - 1) != null) {


                int minCol = getTile(row, column - 1).column;

                try {
                    if (same.size() == 1)
                        minCol = getTile(row, column - 1).column;
                    else
                        minCol = same.stream()
                                .min(Comparator.comparing(t -> t.column))
                                .get()
                                .column;
                }
                catch (Exception e)
                {
                    System.out.println(row +"--"+column);
                }



                for (int c = column - 1; c >= minCol; c--) {

                    Tile cur = getTile(row, c);

                    if (cur == null)
                        break;

                    connectedList.add(cur);
                }
            }

            if (getTile(row, column + 1) != null) {

                int maxCol = same.stream()
                        .max(Comparator.comparing(t -> t.column))
                        .get()
                        .column;

                for (int c = column + 1; c <= maxCol; c++) {

                    Tile cur = getTile(row, c);

                    if (cur == null)
                        break;

                    connectedList.add(cur);

                }
            }

        }

        return connectedList;
    }

    public List<Tile> createNewHand() {
        List<Tile> hand = new ArrayList<>();

        List<Tile> bag = getBagTiles();

        Collections.shuffle(bag);

        for (int x = 1; x <= 6; x++) {

            Tile tile = bag.get(x - 1);
            tile.setPlacement(2);
            // 13:39
            tiles.set(tiles.indexOf(tile), tile);
            hand.add(tile);
        }

        return hand;
    }


}
