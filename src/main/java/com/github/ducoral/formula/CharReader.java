package com.github.ducoral.formula;

class CharReader {

    private final char[] input;

    private int index;

    private int line;

    private int column;

    CharReader(String input) {
        this.input = input.toCharArray();
        index = -1;
        line = 0;
        column = -1;
    }

    boolean hasNext() {
        return (index + 1) < input.length;
    }

    CharInfo next() {
        if (!hasNext())
            return new CharInfo('\0', new Position(index + 1, line, column + 1));

        index++;
        if (input[index] == '\n') {
            line++;
            column = -1;
        }
        column++;

        return new CharInfo(input[index], new Position(index, line, column));
    }
}
