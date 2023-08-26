package com.github.ducoral.formula.scanner;

public record Token(TokenType type, String lexeme, Position position) {

    public static final Token EOF = new Token(TokenType.EOF, "", Position.NULL);

    public static Token fromCharInfo(TokenType type, CharInfo charInfo) {
        return new Token(type, charInfo.asString(), charInfo.position());
    }

    public boolean isEOF() {
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
