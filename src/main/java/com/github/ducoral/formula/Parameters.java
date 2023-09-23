package com.github.ducoral.formula;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;

public class Parameters {

    private final int count;

    private final Function<Integer, Object> valueFunction;

    Parameters(int count, Function<Integer, Object> valueFunction) {
        this.count = count;
        this.valueFunction = valueFunction;
    }

    public int count() {
        return count;
    }

    public boolean isNumber(int index) {
        return Utils.isNumber(get(index));
    }

    public boolean isInteger(int index) {
        return Utils.isInteger(get(index));
    }

    public boolean isString(int index) {
        return Utils.isString(get(index));
    }

    public boolean isTruthful(int index) {
        return Utils.isTruthful(get(index));
    }

    public BigInteger getAsBigInteger(int index) {
        return Utils.asBigInteger(get(index));
    }

    public BigDecimal getAsBigDecimal(int index) {
        return Utils.asBigDecimal(get(index));
    }

    public Object get(int index) {
        if (index < 0 || index >= count)
            throw new IndexOutOfBoundsException(index);
        return valueFunction.apply(index);
    }
}
