package com.github.ducoral.formula;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Value {

    private final Object value;

    Value(Object value) {
        this.value = value;
    }

    public boolean isNull() {
        return value == null;
    }

    public boolean isTruthful() {
        if (value instanceof Boolean booleanValue)
            return booleanValue;
        else if (value instanceof String stringValue)
            return !stringValue.isEmpty();
        else if (value instanceof Number numberValue)
            return numberValue.doubleValue() != 0.0;
        else
            return value != null;
    }

    public boolean isNumber() {
        return value instanceof Number;
    }

    public boolean isInteger() {
        return (value instanceof Number number)
                && number.doubleValue() == (int) number.doubleValue();
    }

    public boolean isString() {
        return value instanceof String;
    }

    public <T> boolean isType(Class<T> type) {
        return type.isInstance(value);
    }

    public BigInteger asBigInteger() {
        if (value instanceof BigInteger)
            return (BigInteger) value;

        if (value instanceof Number number)
            return BigInteger.valueOf(number.longValue());

        return new BigInteger(String.valueOf(value));
    }

    public BigDecimal asBigDecimal() {
        if (value instanceof BigDecimal)
            return (BigDecimal) value;

        if (value instanceof Number number)
            return BigDecimal.valueOf(number.doubleValue());

        return new BigDecimal(String.valueOf(value));
    }

    public String asString() {
        return value instanceof String string ? string : String.valueOf(value);
    }

    public <T> T asType(Class<T> type) {
        return type.cast(value);
    }

    public Class<?> getType() {
        return value == null ? null : value.getClass();
    }

    public String getTypeName() {
        return value == null ? null : value.getClass().getName();
    }

    @Override
    public String toString() {
        return asString();
    }
}
