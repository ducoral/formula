package com.github.ducoral.formula;

import static com.github.ducoral.formula.Expression.*;

class ExpressionExplainsVisitor implements Visitor {

    private String result;

    static String explain(Expression expression) {
        var visitor = new ExpressionExplainsVisitor();
        expression.accept(visitor);
        return visitor.result;
    }

    @Override
    public void visit(NumberLiteral numberLiteral) {
        result = String.valueOf(numberLiteral.value());
    }

    @Override
    public void visit(StringLiteral string) {
        result = "\"" + string.value() + "\"";
    }

    @Override
    public void visit(Identifier identifier) {
        result = identifier.name();
    }

    @Override
    public void visit(UnaryOperation unaryOperation) {

    }

    @Override
    public void visit(BinaryOperation binaryOperation) {
        var op = "[" + binaryOperation.operator() + "]";
        var opLen = op.length();
        binaryOperation.left().accept(this);
        var left = result;
        var leftLen = left.length();
        binaryOperation.right().accept(this);
        var right = result;
        var rightLen = right.length();
        var length = opLen + leftLen + rightLen;
        result = center(op, length)
                + "\n"
                + fillSpace(leftLen) + "/" + fillSpace(length - leftLen - rightLen - 2) + "\\" + fillSpace(rightLen) + "\n"
                + left + fillSpace(length - leftLen - rightLen) + right;
    }

    @Override
    public void visit(FunctionCall function) {

    }

    private static String center(String value, int length) {
        if (length <= value.length())
            return value;
        var diff = (length - value.length()) / 2;
        return fillSpace(length - value.length() - diff)
                + value
                + fillSpace(diff);
    }

    private static String fillSpace(int length) {
        return new String(new char[length]).replace('\0', ' ');
    }
}
