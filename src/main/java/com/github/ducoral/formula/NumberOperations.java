package com.github.ducoral.formula;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.ducoral.formula.Formula.*;
import static com.github.ducoral.formula.Operations.*;
import static com.github.ducoral.formula.FormulaUtils.*;

class NumberOperations implements Consumer<Builder> {

    @Override
    public void accept(Builder builder) {
        builder
                .unaryOperation(numberOperation(MINUS, operateUnary(BigInteger::negate, BigDecimal::negate)))
                .binaryOperation(numberOperation(PLUS, operateBinary(BigInteger::add, BigDecimal::add)))
                .binaryOperation(numberOperation(MINUS, operateBinary(BigInteger::subtract, BigDecimal::subtract)))
                .binaryOperation(numberOperation(ASTERISK, operateBinary(BigInteger::multiply, BigDecimal::multiply)))
                .binaryOperation(numberOperation(SLASH, operateBinary(BigInteger::divide, BigDecimal::divide)))
                .binaryOperation(numberOperation(EQUAL, operateCompareTo(result -> result == 0)))
                .binaryOperation(numberOperation(NOT_EQUAL, operateCompareTo(result -> result != 0)))
                .binaryOperation(numberOperation(GREATER_THAN, operateCompareTo(result -> result > 0)))
                .binaryOperation(numberOperation(GREATER_THAN_OR_EQUAL, operateCompareTo(result -> result > 0 || result == 0)))
                .binaryOperation(numberOperation(LESS_THAN, operateCompareTo(result -> result < 0)))
                .binaryOperation(numberOperation(LESS_THAN_OR_EQUAL, operateCompareTo(result -> result < 0 || result == 0)));
    }

    private static Operation numberOperation(Operator operator, OperationAction action) {
        return new Operation(Number.class, operator, action);
    }

    private static OperationAction operateUnary(
            Function<BigInteger, Object> integerAction,
            Function<BigDecimal, Object> decimalAction) {

        return (operands, chainer) -> {
            var right = operands.right();
            if (!isNumber(right))
                return chainer.chain(operands);

            return isInteger(right)
                    ? integerAction.apply(asBigInteger(right))
                    : decimalAction.apply(asBigDecimal(right));
        };
    }

    private static OperationAction operateBinary(
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

    private static OperationAction operateCompareTo(Predicate<Integer> predicate) {

        return (operands, chainer) -> {
            if (!isNumber(operands.right()))
                return chainer.chain(operands);

            var left = asBigDecimal(operands.left());
            var right = asBigDecimal(operands.right());
            return predicate.test(left.compareTo(right));
        };
    }
}