package com.github.ducoral.formula;

public enum FormulaExceptionType {

    FUNCTION_NOT_DEFINED,
    INVALID_CHARACTER,
    INVALID_DECIMAL_NUMBER,
    INVALID_ESCAPE,
    INVALID_TOKEN,
    OPERATION_NOT_SUPPORTED,
    STRING_NOT_CLOSED_CORRECTLY,
    UNEXPECTED_TOKEN;

    String asMessageKey() {
        return name().toLowerCase().replace('_', '.');
    }
}
