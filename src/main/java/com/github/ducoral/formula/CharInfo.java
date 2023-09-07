package com.github.ducoral.formula;

record CharInfo(char value, Position position) {

    static final CharInfo EOF = new CharInfo('\0', Position.NULL);

    boolean isEOF() {
        return this.equals(EOF);
    }

    boolean is(char value) {
        return this.value == value;
    }

    boolean isDigit() {
        return Character.isDigit(value);
    }

    boolean isWhitespace() {
        return Character.isWhitespace(value);
    }

    boolean isIdentifierStart() {
        return Character.isJavaIdentifierStart(value);
    }

    boolean isIdentifierPart() {
        return value != '\0' && Character.isJavaIdentifierPart(value);
    }

    boolean isStringDelimiter() {
        return value == '"'
                || value == '\''
                || value == '`';
    }

    boolean isSymbol() {
        return isOneOf('(', ')', ',');
    }

    boolean isOneOf(char... chars) {
        for (char c : chars)
            if (c == value)
                return true;
        return false;
    }

    String asString() {
        return String.valueOf(value);
    }

    void appendTo(StringBuilder builder) {
        builder.append(value);
    }
}