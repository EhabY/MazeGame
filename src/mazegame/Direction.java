package mazegame;

public enum Direction {
  NORTH("North") {
    public Direction left() {
      return WEST;
    }

    public Direction right() {
      return EAST;
    }
  },
  EAST("East") {
    public Direction left() {
      return NORTH;
    }

    public Direction right() {
      return SOUTH;
    }
  },
  SOUTH("South") {
    public Direction left() {
      return EAST;
    }

    public Direction right() {
      return WEST;
    }
  },
  WEST("West") {
    public Direction left() {
      return SOUTH;
    }

    public Direction right() {
      return NORTH;
    }
  };

  private final String direction;

  Direction(String direction) {
    this.direction = direction;
  }

  public abstract Direction left();

  public abstract Direction right();

  @Override
  public String toString() {
    return direction;
  }
}
