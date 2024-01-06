package com.github.ducoral.formula;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.ducoral.formula.Formula.Builder;
import static com.github.ducoral.formula.FormulaDefaults.ASTERISK;
import static com.github.ducoral.formula.FormulaDefaults.EQUAL;
import static com.github.ducoral.formula.FormulaDefaults.GREATER_THAN;
import static com.github.ducoral.formula.FormulaDefaults.GREATER_THAN_OR_EQUAL;
import static com.github.ducoral.formula.FormulaDefaults.LESS_THAN;
import static com.github.ducoral.formula.FormulaDefaults.LESS_THAN_OR_EQUAL;
import static com.github.ducoral.formula.FormulaDefaults.MINUS;
import static com.github.ducoral.formula.FormulaDefaults.NOT_EQUAL;
import static com.github.ducoral.formula.FormulaDefaults.PLUS;
import static com.github.ducoral.formula.FormulaDefaults.SLASH;
import static java.math.RoundingMode.UNNECESSARY;

class OperationsNumber implements Consumer<Builder> {

    @Override
    public void accept(Builder builder) {
        builder
                .unaryOperation(operation(MINUS, operateUnary(BigInteger::negate, BigDecimal::negate)))
                .binaryOperation(operation(PLUS, operateBinary(BigInteger::add, BigDecimal::add)))
                .binaryOperation(operation(MINUS, operateBinary(BigInteger::subtract, BigDecimal::subtract)))
                .binaryOperation(operation(ASTERISK, operateBinary(BigInteger::multiply, BigDecimal::multiply)))
                .binaryOperation(operation(SLASH,
                        operateBinary(BigInteger::divide, (left, right) ->
                                left.divide(right, builder.roundingModeReference.get()))))
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

        return (operands, chainer) ->
                operands.right().isInteger()
                        ? integerAction.apply(operands.right().asBigInteger())
                        : decimalAction.apply(operands.right().asBigDecimal());
    }

    private static OperationAction operateBinary(
            BiFunction<BigInteger, BigInteger, Object> integerAction,
            BiFunction<BigDecimal, BigDecimal, Object> decimalAction) {

        return (operands, chainer) -> {
            if (operands.right().isNumber()) {
                if (operands.left().isInteger() && operands.right().isInteger())
                    return integerAction.apply(operands.left().asBigInteger(), operands.right().asBigInteger());

                var left = operands.left().asBigDecimal();
                var right = operands.right().asBigDecimal();
                var scale = Math.max(left.scale(), right.scale());
                left = left.setScale(scale, UNNECESSARY);
                right = right.setScale(scale, UNNECESSARY);
                return decimalAction.apply(left, right);
            }
            return chainer.chain(operands);
        };
    }

    private static OperationAction operateCompareTo(Predicate<Integer> predicate) {

        return (operands, chainer) -> {
            if (!operands.right().isNumber())
                return chainer.chain(operands);

            var left = operands.left().asBigDecimal();
            var right = operands.right().asBigDecimal();
            return predicate.test(left.compareTo(right));
        };
    }
}