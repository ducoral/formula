package com.github.ducoral.formula;

public record Position(int index, int line, int column) {
    public static final Position NULL = new Position(-1, -1, -1);
    public static final Position ZERO = new Position(0, 0, 0);
}
