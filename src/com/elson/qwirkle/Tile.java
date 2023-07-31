package com.elson.qwirkle;

import java.io.Serializable;

public class Tile implements Serializable {
    private static final long serialVersionUID = 2L;
    public int color, shape, placement, row, column;

    public Tile(int color, int shape) {
        this.color = color;
        this.shape = shape;
        this.placement = 1;

    }

    public void setPlacement(int placement) {
        this.placement = placement;
    }

    public void setPosition(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public String strPos() {
        return "(" + row + ")" + "(" + column + ")";
    }

    @Override
    public String toString() {

        String colorT = "";

        switch (this.color) {
            case 1: {
                colorT = "Red";
                break;
            }
            case 2: {
                colorT = "Orange";
                break;
            }
            case 3: {
                colorT = "Yellow";
                break;
            }
            case 4: {
                colorT = "Green";
                break;
            }
            case 5: {
                colorT = "Blue";
                break;
            }
            case 6: {
                colorT = "Purple";
                break;
            }
        }

        String shapeT = "";

        switch (this.shape) {
            case 1: {
                shapeT = "Circle";
                break;
            }
            case 2: {
                shapeT = "Cross";
                break;
            }
            case 3: {
                shapeT = "Diamond";
                break;
            }
            case 4: {
                shapeT = "Square";
                break;
            }
            case 5: {
                shapeT = "Star";
                break;
            }
            case 6: {
                shapeT = "Clover";
                break;
            }
        }

        return colorT + "-" + shapeT;

    }
}
