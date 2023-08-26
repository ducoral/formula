package com.github.ducoral.formula.scanner;

public class CharReader {

    private final char[] input;

    private int index;

    private int line;

    private int column;

    public CharReader(String input) {
        this.input = input.toCharArray();
        index = -1;
        line = 0;
        column = -1;
    }

    public boolean hasNext() {
        return (index + 1) < input.length;
    }

    public CharInfo next() {
        if (!hasNext())
            return CharInfo.EOF;

        index++;
        if (input[index] == '\n') {
            line++;
            column = -1;
        }
        column++;

        return new CharInfo(input[index], new Position(index, line, column));
    }
}
