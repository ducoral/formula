package com.github.ducoral.formula;

public class FormulaException extends RuntimeException {

    public FormulaException(String message, Object... args) {
        super(String.format(message, args));
    }

    public FormulaException(String message, Throwable cause, Object... args) {
        super(String.format(message, args), cause);
    }
}

