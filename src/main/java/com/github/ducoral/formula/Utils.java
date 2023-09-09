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

    static String rightAlign(String str, int length) {
        if (length <= str.length())
            return str;
        return fillSpaces(length - str.length()) + str;
    }

    static String centralize(String str, int length) {
        int diff = length - str.length();
        if (diff < 1)
            return str;
        var result = new StringBuilder(fillSpaces(diff));
        result.insert(diff / 2, str);
        return result.toString();
    }

    static String fillSpaces(int length) {
        return fill(' ', length);
    }

    static String fill(char ch, int length) {
        return new String(new char[length]).replace('\0', ch);
    }

    static String[] splitLines(String str) {
        return str.split("\\n");
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
