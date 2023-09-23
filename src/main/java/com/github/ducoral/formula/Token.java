package com.github.ducoral.formula;

record Token(TokenType type, String lexeme, Position position) {

    boolean isEOF() {
        return type == TokenType.EOF;
    }

    @Override
    public String toString() {
        var escaped = lexeme
                .replace("\n", "\\n")
                .replace("\t", "\\t")
                .replace("\"", "\\\"");

        return String.format(
                "%s[type=%s, lexeme=\"%s\", position=%s]",
                getClass().getSimpleName(), type, escaped, position);
    }
}
