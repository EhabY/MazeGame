package mapgenerator;

import mazegame.Direction;

public class MovementManager {
    private final int side;

    MovementManager(int side) {
        this.side = side;
    }

    public int getSide() {
        return side;
    }

    public boolean isDirectionValid(int position, Direction direction) {
        int newPosition = getPositionAfterMoving(position, direction);
        return isPositionValid(newPosition) && isHorizontalMoveValid(position, newPosition);
    }

    private boolean isPositionValid(int position) {
        return position >= 0 && position < side*side;
    }

    private boolean isHorizontalMoveValid(int oldPosition, int newPosition) {
        return Math.abs(newPosition - oldPosition) != 1 || getRow(oldPosition) == getRow(newPosition);
    }

    private int getRow(int position) {
        return position/side;
    }

    public int getPositionAfterMoving(int position, Direction direction) {
        switch (direction) {
            case EAST:
                return position + 1;
            case SOUTH:
                return position + side;
            case WEST:
                return position - 1;
            case NORTH:
                return position - side;
        }

        throw new IllegalStateException("direction: " + direction + " is unknown");
    }
}
