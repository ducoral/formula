package com.github.ducoral.formula;

public record Operation(Class<?> type, Operator operator, OperationAction action) implements Comparable<Operation> {

    boolean supports(Class<?> type, String operator) {
        return this.operator.lexeme().equals(operator)
                && type != null
                && this.type.isAssignableFrom(type);
    }

    @Override
    public int compareTo(Operation other) {
        if (operator != other.operator)
            throw new FormulaException("Operadores diferentes");

        if (type == other.type)
            return 0;

        return type.isAssignableFrom(other.type) ? 1 : -1;
    }
}
