package com.github.ducoral.formula;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.github.ducoral.formula.Expression.BinaryOperation;
import static com.github.ducoral.formula.Expression.FunctionCall;
import static com.github.ducoral.formula.Expression.Identifier;
import static com.github.ducoral.formula.Expression.NumberLiteral;
import static com.github.ducoral.formula.Expression.StringLiteral;
import static com.github.ducoral.formula.Expression.UnaryOperation;
import static com.github.ducoral.formula.Expression.Visitor;
import static com.github.ducoral.formula.Utils.centralize;
import static com.github.ducoral.formula.Utils.comma;
import static com.github.ducoral.formula.Utils.fillSpaces;
import static com.github.ducoral.formula.Utils.splitLines;

class ExpressionAsTextTreeVisitor implements Visitor {

    private String result;

    static String asTextTree(Expression expression) {
        var visitor = new ExpressionAsTextTreeVisitor();
        expression.accept(visitor);
        return visitor.result;
    }

    @Override
    public void visit(NumberLiteral numberLiteral) {
        result = String.valueOf(numberLiteral.value());
    }

    @Override
    public void visit(StringLiteral stringLiteral) {
        result = "\"" + stringLiteral.value() + "\"";
    }

    @Override
    public void visit(Identifier identifier) {
        result = identifier.name();
    }

    @Override
    public void visit(UnaryOperation unaryOperation) {
        unaryOperation.right().accept(this);
        var right = splitLines(result);
        var rightLen = right[0].length();
        var header = "[" + unaryOperation.operator() + "]";
        var builder = new StringBuilder(header + fillSpaces(rightLen) + '\n');
        builder
                .append(fillSpaces(header.length() - 1))
                .append('\\')
                .append(fillSpaces(rightLen));
        var left = fillSpaces(header.length());
        for (String rigthStr : right)
            builder.append('\n').append(left).append(rigthStr);
        result = builder.toString();
    }

    @Override
    public void visit(BinaryOperation binaryOperation) {
        binaryOperation.left().accept(this);
        var left = splitLines(result);
        binaryOperation.right().accept(this);
        var right = splitLines(result);

        var header = "[" + binaryOperation.operator() + "]";
        var content = concatenate(left, right, header.length());
        var conectors = buildConectors(content[0]);
        var builder = new StringBuilder(buildHeader(header, conectors));
        builder.append('\n').append(conectors);
        for (String line : content)
            builder.append('\n').append(line);
        result = builder.toString();
    }

    @Override
    public void visit(FunctionCall functionCall) {
        var content = splitLines(functionCall.name() + "(");
        var comma = comma();
        for (var parameter : functionCall.parameters()) {
            parameter.accept(this);
            content = concatenate(content, splitLines(comma.toString()), 0);
            content = concatenate(content, splitLines(result), 0);
        }
        content = concatenate(content, splitLines(")"), 0);
        var builder = new StringBuilder(content[0]);
        for (int index = 1; index < content.length; index++)
            builder.append('\n').append(content[index]);
        result = builder.toString();
    }

    private static String buildHeader(String header, String conectors) {
        int left = conectors.indexOf("/");
        int right = conectors.indexOf("\\");

        var centralized = centralize(header, right - left + 1);
        var builder = new StringBuilder();
        builder.append(centralized.charAt(0));
        for (int index = 1; index < centralized.length() - 1; index++)
            builder.append(centralized.charAt(index) == ' ' ? '_' : centralized.charAt(index));
        builder.append(centralized.charAt(centralized.length() - 1));

        return fillSpaces(left)
                + builder
                + fillSpaces(conectors.length() - right - 1);
    }

    private static String buildConectors(String str) {
        var index = 0;
        Predicate<Integer> isSpaceOrUnderscore = idx -> str.charAt(idx) == ' ' || str.charAt(idx) == '_';
        while (index < str.length() && isSpaceOrUnderscore.test(index))
            index++;
        var delimiterFlag = new AtomicBoolean(false);
        var delimiterChecker = getDelimiterChecker(delimiterFlag);
        while (index < str.length() && (delimiterFlag.get() || isSpaceOrUnderscore.negate().test(index))) {
            delimiterChecker.accept(str.charAt(index));
            index++;
        }
        var left = index;
        while (index < str.length() && isSpaceOrUnderscore.test(index))
            index++;
        var middle = index - left;
        return fillSpaces(left)
                + "/"
                + fillSpaces(middle - 2)
                + "\\"
                + fillSpaces(str.length() - left - middle);
    }

    private static Consumer<Character> getDelimiterChecker(AtomicBoolean delimiterFlag) {
        var parentesisCount = new AtomicInteger();
        var doubleQuoteFlag = new AtomicBoolean(false);
        var singleQuoteFlag = new AtomicBoolean(false);
        var crasisFlag = new AtomicBoolean(false);

        var stringDelimiter = new AtomicReference<Character>('.');
        BiConsumer<AtomicBoolean, Character> stringChecker = (flag, character) -> {
            if (!delimiterFlag.get()) {
                flag.set(true);
                stringDelimiter.set(character);
            } else if (stringDelimiter.get() == character)
                flag.set(false);
        };

        return character -> {
            if (character == '(')
                parentesisCount.incrementAndGet();
            else if (character == ')')
                parentesisCount.decrementAndGet();
            else if (character == '"')
                stringChecker.accept(doubleQuoteFlag, character);
            else if (character == '\'')
                stringChecker.accept(singleQuoteFlag, character);
            else if (character == '`')
                stringChecker.accept(crasisFlag, character);
            delimiterFlag.set(
                    parentesisCount.get() > 0
                            || doubleQuoteFlag.get()
                            || singleQuoteFlag.get()
                            || crasisFlag.get());
        };
    }

    private static String[] concatenate(String[] left, String[] right, int middleWidth) {
        int leftWidth = left[0].length();
        int rightWidth = right[0].length();

        if (left.length == 1 && right.length == 1)
            return new String[]{left[0] + fillSpaces(middleWidth) + right[0]};

        int minMiddle = countMinMiddleSpaces(left, right);

        int lines = Math.max(left.length, right.length);
        left = expandToSizeAndFillWithSpaces(left, lines);
        right = expandToSizeAndFillWithSpaces(right, lines);

        int firstMiddle = countMiddleSpaces(left[0], right[0]);
        if (firstMiddle <= middleWidth)
            return join(left, right, firstMiddle - middleWidth);

        int offset = Math.min(firstMiddle - middleWidth, minMiddle - 1);
        if (offset > leftWidth)
            for (int index = 0; index < left.length; index++)
                left[index] = fillSpaces(offset - leftWidth) + left[index];

        if (offset > rightWidth)
            for (int index = 0; index < right.length; index++)
                right[index] += fillSpaces(offset - rightWidth);

        return join(left, right, offset);
    }

    private static String[] join(String[] left, String[] right, int offset) {
        var result = new String[left.length];
        if (offset < 1) {
            var spaces = fillSpaces(Math.abs(offset));
            for (int index = 0; index < left.length; index++)
                result[index] = left[index] + spaces + right[index];
        } else for (int index = 0; index < left.length; index++) {
            var leftStr = removeSpaces(left[index], left[index].length(), offset);
            var rightStr = removeSpaces(right[index], 0, offset - leftStr.removed);
            result[index] = leftStr.result + rightStr.result;
        }
        return result;
    }

    private static int countMinMiddleSpaces(String[] left, String[] right) {
        int leftWidth = left[0].length();
        int index = 0;
        int count = countSpaces(left[index], leftWidth) + countSpaces(right[index], 0);
        while (index < left.length && index < right.length) {
            count = Math.min(count, countSpaces(left[index], leftWidth) + countSpaces(right[index], 0));
            index++;
        }
        return count;
    }

    private static int countMiddleSpaces(String left, String right) {
        return countSpaces(left, left.length()) + countSpaces(right, 0);
    }

    private static RemoveResult removeSpaces(String str, int from, int count) {
        var removeCount = Math.min(countSpaces(str, from), count);
        var result = from == 0
                ? str.substring(removeCount)
                : str.substring(0, str.length() - removeCount);
        return new RemoveResult(result, removeCount);
    }

    private record RemoveResult(String result, int removed) {
    }

    private static int countSpaces(String str, int from) {
        int count = 0;
        if (from == 0)
            while (from + count < str.length() && str.charAt(from + count) == ' ') {
                count++;
            }
        else if (--from < str.length())
            while (from - count >= 0 && str.charAt(from - count) == ' ') {
                count++;
            }
        return count;
    }

    private static String[] expandToSizeAndFillWithSpaces(String[] strs, int newSize) {
        var spaces = fillSpaces(strs[0].length());
        var newStrs = new String[newSize];
        System.arraycopy(strs, 0, newStrs, 0, strs.length);
        for (int index = strs.length; index < newSize; index++)
            newStrs[index] = spaces;
        return newStrs;
    }


}