package com.github.ducoral.formula;

import java.math.BigDecimal;
import java.math.BigInteger;

class Utils {

    static boolean isTruthful(Object value) {
        if (value instanceof Boolean booleanValue)
            return booleanValue;
        else if (value instanceof String stringValue)
            return !stringValue.isEmpty();
        else if (value instanceof Number numberValue)
            return numberValue.doubleValue() != 0.0;
        else
            return value != null;
    }

    static boolean isNumber(Object value) {
        return value instanceof Number;
    }

    static boolean isInteger(Object value) {
        return (value instanceof Number number)
                && number.doubleValue() == (int) number.doubleValue();
    }

    static boolean isString(Object value) {
        return value instanceof String;
    }

    static BigInteger asBigInteger(Object value) {
        if (value instanceof BigInteger)
            return (BigInteger) value;

        if (value instanceof Number number)
            return BigInteger.valueOf(number.longValue());

        throw new FormulaException("Object " + value + " isn' a number");
    }

    static BigDecimal asBigDecimal(Object value) {
        if (value instanceof BigDecimal)
            return (BigDecimal) value;

        if (value instanceof Number number)
            return BigDecimal.valueOf(number.doubleValue());

        throw new FormulaException("Object " + value + " isn't a number");
    }

    static Class<?> getTypeOf(Object object) {
        return object == null
                ? null
                : object.getClass();
    }

    static String getTypeNameOf(Object object) {
        return object == null
                ? null
                : object.getClass().getName();
    }

    static Object comma() {
        return new Object() {
            int count = 0;
            @Override
            public String toString() {
                return count++ == 0 ? "" : ", ";
            }
        };
    }
}
