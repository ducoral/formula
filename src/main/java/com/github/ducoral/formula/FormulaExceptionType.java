package com.github.ducoral.formula;

import java.text.MessageFormat;

public enum FormulaExceptionType {

    FUNCTION_NOT_DEFINED("function.not.defined"),
    INVALID_CHARACTER("invalid.character"),
    INVALID_DECIMAL_NUMBER("invalid.decimal.number"),
    INVALID_ESCAPE("invalid.escape"),
    INVALID_TOKEN("invalid.token"),
    OPERATION_NOT_SUPPORTED("operation.not.supported"),
    STRING_NOT_CLOSED_CORRECTLY("string.not.closed.correctly"),
    UNEXPECTED_TOKEN("unexpected.token");

    private final String key;

    String formatMessage(Object... arguments) {
        return MessageFormat.format(Strings.get(key), arguments);
    }

    FormulaExceptionType(String key) {
        this.key = key;
    }
}
