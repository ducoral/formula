package com.github.ducoral.formula;

public record Operator(String lexeme, Precedence precedence) {

    @Override
    public String toString() {
        return lexeme;
    }
}