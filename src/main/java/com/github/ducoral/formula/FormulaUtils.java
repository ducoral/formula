package com.github.ducoral.formula;

import java.math.BigDecimal;
import java.math.BigInteger;

public class FormulaUtils {

    public static boolean isNumber(Object value) {
        return value instanceof Number;
    }

    public static boolean isInteger(Object value) {
        return (value instanceof Number number)
                && number.doubleValue() == (int) number.doubleValue();
    }

    public static BigInteger asBigInteger(Object value) {
        if (value instanceof BigInteger)
            return (BigInteger) value;

        if (value instanceof Number number)
            return BigInteger.valueOf(number.longValue());

        throw new FormulaException("Object " + value + " isn' a number");
    }

    public static BigDecimal asBigDecimal(Object value) {
        if (value instanceof BigDecimal)
            return (BigDecimal) value;

        if (value instanceof Number number)
            return BigDecimal.valueOf(number.doubleValue());

        throw new FormulaException("Object " + value + " isn' a number");
    }
}
