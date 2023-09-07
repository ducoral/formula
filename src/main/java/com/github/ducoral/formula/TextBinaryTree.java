package com.github.ducoral.formula;

record TextBinaryTree(TextBinaryTree left, String value, TextBinaryTree right) {

    @Override
    public String toString() {

        if (left == null || right == null)
            return value;

        var concatenated = concat(
                left.toString().split("\n"),
                right.toString().split("\n"),
                value.length());

        return spaces(concatenated.leftWidth)
                + centralize(value, concatenated.middleWidth)
                + spaces(concatenated.rightWidth)
                + '\n'
                + spaces(concatenated.leftWidth)
                + '/'
                + spaces(concatenated.middleWidth - 2)
                + '\\'
                + spaces(concatenated.rightWidth())
                + '\n'
                + concatenated.result;
    }

    private static ConcatResult concat(String[] left, String[] right, int minMiddleWidth) {
        if (left.length == 1 && right.length == 1)
            return new ConcatResult(
                    left[0] + spaces(minMiddleWidth) + right[0],
                    left[0].length(),
                    minMiddleWidth,
                    right[0].length());

        var result = new StringBuilder();
        var diff = countMinMiddleSpaces(left, right) - minMiddleWidth;

        if (diff > 0) {
            var side = left.length < right.length ? left : right;
            for (int index = 0; index < side.length; index++)
                side[index] = side == left
                        ? spaces(diff) + side[index]
                        : side[index] + spaces(diff);
        } else if (diff < 0) {
            var side = left.length < right.length ? left : right;
            for (int index = 0; index < side.length; index++)
                side[index] = side == left
                        ? side[index] + spaces(Math.abs(diff))
                        : spaces(Math.abs(diff)) + side[index];
            diff = 0;
        }

        left = fillSpaces(left, Math.max(left.length, right.length));
        right = fillSpaces(right, left.length);

        var leftWidth = left[0].length() - countRightSpaces(left[0]);
        var middleWidth = countRightSpaces(left[0]) + countLeftSpaces(right[0]) - diff;
        var rightWidth = right[0].length() - countLeftSpaces(right[0]);

        for (int index = 0; index < left.length; index++) {
            if (index > 0)
                result.append('\n');
            var leftStr = removeRightSpaces(left[index], diff);
            var rightStr = removeLeftSpaces(right[index], diff - (left[index].length() - leftStr.length()));
            result
                    .append(leftStr)
                    .append(rightStr);
        }

        return new ConcatResult(result.toString(), leftWidth, middleWidth, rightWidth);
    }

    private record ConcatResult(String result, int leftWidth, int middleWidth, int rightWidth) {
    }

    private static String[] fillSpaces(String[] strs, int newLength) {
        var newStrs = new String[newLength];
        for (int index = 0; index < newStrs.length; index++)
            newStrs[index] = index < strs.length
                    ? strs[index]
                    : spaces(strs[0].length());
        return newStrs;
    }

    private static String removeLeftSpaces(String str, int count) {
        var index = 0;
        while (index < count && index < str.length() && str.charAt(index) == ' ')
            index++;
        return str.substring(index);
    }

    private static String removeRightSpaces(String str, int count) {
        var index = str.length();
        while (index > 0 && str.length() - index < count && str.charAt(index - 1) == ' ')
            index--;
        return str.substring(0, index);
    }

    private static int countMinMiddleSpaces(String[] leftLines, String[] rightLines) {
        var count = 0;
        var index = 0;
        while (index < leftLines.length && index < rightLines.length) {
            var middle = countRightSpaces(leftLines[index]) + countLeftSpaces(rightLines[index]);
            if (count == 0 || count > middle)
                count = middle;
            index++;
        }
        return count;
    }

    private static int countRightSpaces(String str) {
        var index = str.length();
        while (index > 0 && str.charAt(index - 1) == ' ')
            index--;
        return str.length() - index;
    }

    private static int countLeftSpaces(String str) {
        int index = 0;
        while (index < str.length() && str.charAt(index) == ' ')
            index++;
        return index;
    }

    private static String centralize(String str, int width) {
        int diff = width - str.length();
        if (diff < 1)
            return str;
        var right = diff / 2;
        var left = width - str.length() - right;
        return spaces(left) + str + spaces(right);
    }

    private static String spaces(int length) {
        return new String(new char[length])
                .replace('\0', ' ');
    }
}
