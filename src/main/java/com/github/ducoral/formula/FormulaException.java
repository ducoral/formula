package com.github.ducoral.formula;

public class FormulaException extends RuntimeException {

    public final Position position;

    public final FormulaExceptionType exceptionType;

    FormulaException(FormulaExceptionType exceptionType, Position position, Object... args) {
        super(getMessage(exceptionType, args));
        this.exceptionType = exceptionType;
        this.position = position;
    }

    private static String getMessage(FormulaExceptionType exceptionType, Object... args) {
        var messageKey = exceptionType.name().toLowerCase().replace('_', '.');
        return Strings.get(messageKey, args);
    }
}