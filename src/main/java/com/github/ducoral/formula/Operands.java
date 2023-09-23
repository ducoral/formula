package com.github.ducoral.formula;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.ducoral.formula.Utils.*;

public class Operands {

    private final Position position;

    private final Supplier<Object> left;

    private final Supplier<Object> right;

    private final Function<Operands, String> toString;

    Operands(Position position, Supplier<Object> left, Supplier<Object> right, Function<Operands, String> toString) {
        this.position = position;
        this.left = left;
        this.right = right;
        this.toString = toString;
    }

    public Object getLeft() {
        return left.get();
    }

    public String getLeftAsString() {
        return String.valueOf(getLeft());
    }

    public BigInteger getLeftAsBigInteger() {
        return asBigInteger(getLeft());
    }

    public BigDecimal getLeftAsBigDecimal() {
        return asBigDecimal(getLeft());
    }

    public boolean isLeftTruthful() {
        return isTruthful(getLeft());
    }

    public boolean isLeftNumber() {
        return isNumber(getLeft());
    }

    public boolean isLeftInteger() {
        return isInteger(getLeft());
    }

    public String getLeftType() {
        return getTypeNameOf(getLeft());
    }

    public Object getRight() {
        return right.get();
    }

    public String getRightAsString() {
        return String.valueOf(getRight());
    }

    public BigInteger getRightAsBigInteger() {
        return asBigInteger(getRight());
    }

    public BigDecimal getRightAsBigDecimal() {
        return asBigDecimal(getRight());
    }

    public boolean isRightTruthful() {
        return isTruthful(getRight());
    }

    public boolean isRightNumber() {
        return isNumber(getRight());
    }

    public boolean isRightInteger() {
        return isInteger(getRight());
    }
    public String getRightType() {
        return getTypeNameOf(getRight());
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return toString.apply(this);
    }
}
