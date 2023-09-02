package com.github.ducoral.formula;

record Token(TokenType type, String lexeme, Position position) {

    static final Token EOF = new Token(TokenType.EOF, "", Position.NULL);

    static Token fromCharInfo(TokenType type, CharInfo charInfo) {
        return new Token(type, charInfo.asString(), charInfo.position());
    }

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
