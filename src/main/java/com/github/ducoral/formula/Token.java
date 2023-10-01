package com.github.ducoral.formula;

record Token(TokenType type, String lexeme, Position position) {

    boolean isEOF() {
        return type == TokenType.EOF;
    }

    @Override
    public String toString() {
        return lexeme
                .replace("\n", "\\n")
                .replace("\t", "\\t")
                .replace("\"", "\\\"");
    }
}
