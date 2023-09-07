package com.github.ducoral.formula;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.ducoral.formula.Formula.Builder;
import static com.github.ducoral.formula.FormulaDefaults.*;

class NumberOperations implements Consumer<Builder> {

    @Override
    public void accept(Builder builder) {
        builder
                .unaryOperation(operation(MINUS, operateUnary(BigInteger::negate, BigDecimal::negate)))
                .binaryOperation(operation(PLUS, operateBinary(BigInteger::add, BigDecimal::add)))
                .binaryOperation(operation(MINUS, operateBinary(BigInteger::subtract, BigDecimal::subtract)))
                .binaryOperation(operation(ASTERISK, operateBinary(BigInteger::multiply, BigDecimal::multiply)))
                .binaryOperation(operation(SLASH, operateBinary(BigInteger::divide, BigDecimal::divide)))
                .binaryOperation(operation(EQUAL, operateCompareTo(result -> result == 0)))
                .binaryOperation(operation(NOT_EQUAL, operateCompareTo(result -> result != 0)))
                .binaryOperation(operation(GREATER_THAN, operateCompareTo(result -> result > 0)))
                .binaryOperation(operation(GREATER_THAN_OR_EQUAL, operateCompareTo(result -> result > 0 || result == 0)))
                .binaryOperation(operation(LESS_THAN, operateCompareTo(result -> result < 0)))
                .binaryOperation(operation(LESS_THAN_OR_EQUAL, operateCompareTo(result -> result < 0 || result == 0)));
    }

    private static Operation operation(Operator operator, OperationAction action) {
        return new Operation(Number.class, operator, action);
    }

    private static OperationAction operateUnary(
            Function<BigInteger, Object> integerAction,
            Function<BigDecimal, Object> decimalAction) {

        return (operands, chainer) -> {
            if (!operands.isRightNumber())
                return chainer.chain(operands);

            return operands.isRightInteger()
                    ? integerAction.apply(operands.getRightAsBigInteger())
                    : decimalAction.apply(operands.getRightAsBigDecimal());
        };
    }

    private static OperationAction operateBinary(
            BiFunction<BigInteger, BigInteger, Object> integerAction,
            BiFunction<BigDecimal, BigDecimal, Object> decimalAction) {

        return (operands, chainer) -> {
            if (!operands.isRightNumber())
                return chainer.chain(operands);

            return operands.isLeftInteger() && operands.isRightInteger()
                    ? integerAction.apply(operands.getLeftAsBigInteger(), operands.getRightAsBigInteger())
                    : decimalAction.apply(operands.getLeftAsBigDecimal(), operands.getRightAsBigDecimal());
        };
    }

    private static OperationAction operateCompareTo(Predicate<Integer> predicate) {

        return (operands, chainer) -> {
            if (!operands.isRightNumber())
                return chainer.chain(operands);

            var left = operands.getLeftAsBigDecimal();
            var right = operands.getRightAsBigDecimal();
            return predicate.test(left.compareTo(right));
        };
    }
}