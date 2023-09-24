package com.github.ducoral.formula;

public class FormulaException extends RuntimeException {

    public final Position position;

    public final FormulaExceptionType type;

    FormulaException(FormulaExceptionType type, Position position, Object... args) {
        super(Strings.get(type.asMessageKey(), args));
        this.type = type;
        this.position = position;
    }
}