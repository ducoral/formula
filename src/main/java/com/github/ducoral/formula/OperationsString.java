package com.github.ducoral.formula;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.ducoral.formula.Formula.Builder;
import static com.github.ducoral.formula.FormulaDefaults.EQUAL;
import static com.github.ducoral.formula.FormulaDefaults.GREATER_THAN;
import static com.github.ducoral.formula.FormulaDefaults.GREATER_THAN_OR_EQUAL;
import static com.github.ducoral.formula.FormulaDefaults.LESS_THAN;
import static com.github.ducoral.formula.FormulaDefaults.LESS_THAN_OR_EQUAL;
import static com.github.ducoral.formula.FormulaDefaults.MINUS;
import static com.github.ducoral.formula.FormulaDefaults.NOT_EQUAL;
import static com.github.ducoral.formula.FormulaDefaults.PLUS;

class OperationsString implements Consumer<Builder> {

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
                (operands, chain) -> action.apply(operands.left().asString(), operands.right().asString()));
    }

    private static BiFunction<String, String, Object> compare(Function<Integer, Boolean> resultFunction) {
        return (left, right) -> resultFunction.apply(left.compareTo(right));
    }
}
