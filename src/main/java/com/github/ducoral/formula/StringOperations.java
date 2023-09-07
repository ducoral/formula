package com.github.ducoral.formula;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.ducoral.formula.Formula.*;
import static com.github.ducoral.formula.FormulaDefaults.*;

class StringOperations implements Consumer<Builder> {

    @Override
    public void accept(Builder builder) {
        builder
                .binaryOperation(operation(PLUS, (left, right) -> left + right))
                .binaryOperation(operation(MINUS, (left, right) -> left.replace(right, "")))
                .binaryOperation(operation(EQUAL, compare(result -> result == 0)))
                .binaryOperation(operation(NOT_EQUAL, compare(result -> result != 0)))
                .binaryOperation(operation(GREATER_THAN, compare(result -> result > 0)))
                .binaryOperation(operation(GREATER_THAN_OR_EQUAL, compare(result -> result > 0 || result == 0)))
                .binaryOperation(operation(LESS_THAN, compare(result -> result < 0)))
                .binaryOperation(operation(LESS_THAN_OR_EQUAL, compare(result -> result < 0 || result == 0)));
    }

    private static Operation operation(Operator operator, BiFunction<String, String, Object> action) {
        return new Operation(
                String.class,
                operator,
                (operands, chain) -> action.apply(operands.getLeftAsString(), operands.getRightAsString()));
    }

    private static BiFunction<String, String, Object> compare(Function<Integer, Boolean> resultFunction) {
        return (left, right) -> resultFunction.apply(left.compareTo(right));
    }
}
