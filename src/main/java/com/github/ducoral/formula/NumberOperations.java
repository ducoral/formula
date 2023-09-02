package com.github.ducoral.formula;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.github.ducoral.formula.Formula.*;
import static com.github.ducoral.formula.Operations.*;
import static com.github.ducoral.formula.FormulaUtils.*;

class NumberOperations implements Consumer<Builder> {

    @Override
    public void accept(Builder builder) {
        builder
                .binaryOperation(numberOperation(PLUS, operate(BigInteger::add, BigDecimal::add)))
                .binaryOperation(numberOperation(MINUS, operate(BigInteger::subtract, BigDecimal::subtract)))
                .binaryOperation(numberOperation(ASTERISK, operate(BigInteger::multiply, BigDecimal::multiply)))
                .binaryOperation(numberOperation(SLASH, operate(BigInteger::divide, BigDecimal::divide)))
                .binaryOperation(numberOperation(EQUAL, compare(result -> result == 0)))
                .binaryOperation(numberOperation(NOT_EQUAL, compare(result -> result != 0)))
                .binaryOperation(numberOperation(GREATER_THAN, compare(result -> result > 0)))
                .binaryOperation(numberOperation(GREATER_THAN_OR_EQUAL, compare(result -> result > 0 || result == 0)))
                .binaryOperation(numberOperation(LESS_THAN, compare(result -> result < 0)))
                .binaryOperation(numberOperation(LESS_THAN_OR_EQUAL, compare(result -> result < 0 || result == 0)));
    }

    private static Operation numberOperation(Operator operator, OperationAction action) {
        return new Operation(Number.class, operator, action);
    }

    private static OperationAction operate(
            BiFunction<BigInteger, BigInteger, Object> integerAction,
            BiFunction<BigDecimal, BigDecimal, Object> decimalAction) {

        return (operands, chainer) -> {
            var right = operands.right();
            if (!isNumber(right))
                return chainer.chain(operands);

            var left = operands.left();
            return isInteger(left) && isInteger(right)
                    ? integerAction.apply(asBigInteger(left), asBigInteger(right))
                    : decimalAction.apply(asBigDecimal(left), asBigDecimal(right));
        };
    }

    private static OperationAction compare(Predicate<Integer> predicate) {

        return (operands, chain) -> {
            if (!isNumber(operands.right()))
                return chain.chain(operands);

            return predicate.test(
                    asBigDecimal(operands.left())
                            .compareTo(asBigDecimal(operands.right())));
        };
    }
}