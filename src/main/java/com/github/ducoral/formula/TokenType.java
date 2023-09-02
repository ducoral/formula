package com.github.ducoral.formula;

enum TokenType {
    // (só pra lembrar) boolean é um identificador que o provider retorna valor true/false
    IDENTIFIER,
    INTEGER,
    DECIMAL,
    STRING,
    OPERATOR,
    SYMBOL,
    EOF
}
