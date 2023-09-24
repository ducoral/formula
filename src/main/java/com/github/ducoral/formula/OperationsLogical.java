package com.github.ducoral.formula;

import java.util.function.BiFunction;
import java.util.function.Consumer;

import static com.github.ducoral.formula.Formula.Builder;
import static com.github.ducoral.formula.FormulaDefaults.AND;
import static com.github.ducoral.formula.FormulaDefaults.OR;

class OperationsLogical implements Consumer<Builder> {

    @Override
    public void accept(Builder builder) {
        builder
                .binaryOperation(operation(AND, (left, right) -> left && right))
                .binaryOperation(operation(OR, (left, right) -> left || right));
    }

    private static Operation operation(Operator operator, BiFunction<Boolean, Boolean, Boolean> action) {
        return new Operation(
                Object.class,
                operator,
                (operands, chain) -> action.apply(operands.left().isTruthful(), operands.right().isTruthful()));
    }
}