package com.github.ducoral.formula.scanner;

public enum TokenType {
    // (só pra lembrar) boolean é um identificador que o provider retorna valor true/false
    IDENTIFIER,
    NUMBER,
    STRING,
    OPERATOR,
    SYMBOL,
    WHITESPACE,
    EOF;
}
