package com.github.ducoral.formula;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Value {

    private final Object value;

    Value(Object value) {
        this.value = value;
    }

    public boolean isNull() {
        return value == null;
    }

    public boolean isTrue() {
        if (value instanceof Boolean booleanValue)
            return booleanValue;
        return false;
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
        return isType(Byte.class)
                || isType(Short.class)
                || isType(Integer.class)
                || isType(Long.class)
                || isType(BigInteger.class)
                || isType(AtomicInteger.class)
                || isType(AtomicLong.class);
    }

    public boolean isDecimal() {
        return isNumber() && !isInteger();
    }

    public boolean isString() {
        return value instanceof String;
    }

    public <T> boolean isType(Class<T> type) {
        return type.isInstance(value);
    }

    public BigInteger asBigInteger() {
        return value instanceof BigInteger bigInteger
                ? bigInteger
                : new BigInteger(String.valueOf(value));
    }

    public BigDecimal asBigDecimal() {
        return value instanceof BigDecimal bigDecimal
                ? bigDecimal
                : new BigDecimal(String.valueOf(value));
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

    public Object asObject() {
        return value;
    }

    @Override
    public String toString() {
        return asString();
    }
}
