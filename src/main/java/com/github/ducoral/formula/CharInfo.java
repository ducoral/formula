package com.github.ducoral.formula;

record CharInfo(char value, Position position) {

    public static final CharInfo EOF = new CharInfo('\0', Position.NULL);

    public boolean isEOF() {
        return this.equals(EOF);
    }

    public boolean is(char value) {
        return this.value == value;
    }

    public boolean isDigit() {
        return Character.isDigit(value);
    }

    public boolean isWhitespace() {
        return Character.isWhitespace(value);
    }

    public boolean isIdentifierStart() {
        return Character.isJavaIdentifierStart(value);
    }

    public boolean isIdentifierPart() {
        return value != '\0' && Character.isJavaIdentifierPart(value);
    }

    public boolean isStringDelimiter() {
        return value == '"'
                || value == '\''
                || value == '`';
    }

    public boolean isSymbol() {
        return isOneOf('(', ')', ',');
    }

    public boolean isOneOf(char... chars) {
        for (char c : chars)
            if (c == value)
                return true;
        return false;
    }

    public String asString() {
        return String.valueOf(value);
    }

    public void appendTo(StringBuilder builder) {
        builder.append(value);
    }
}