package com.github.ducoral.formula;

class Utils {

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
