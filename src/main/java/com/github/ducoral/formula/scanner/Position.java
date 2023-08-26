package com.github.ducoral.formula.scanner;

public record Position(int index, int line, int column) {

    public static final Position NULL = new Position(-1, -1, -1);
}
