package com.github.ducoral.formula;

import static com.github.ducoral.formula.Expression.BinaryOperation;
import static com.github.ducoral.formula.Expression.FunctionCall;
import static com.github.ducoral.formula.Expression.Identifier;
import static com.github.ducoral.formula.Expression.NumberLiteral;
import static com.github.ducoral.formula.Expression.StringLiteral;
import static com.github.ducoral.formula.Expression.UnaryOperation;
import static com.github.ducoral.formula.Expression.Visitor;
import static com.github.ducoral.formula.Utils.comma;

class ExpressionAsStringVisitor implements Visitor {

    final StringBuilder stringBuilder = new StringBuilder();

    int scopeLevel = 0;

    static String asString(Expression expression) {
        var asStringVisitor = new ExpressionAsStringVisitor();
        expression.accept(asStringVisitor);
        return asStringVisitor.stringBuilder.toString();
    }

    @Override
    public void visit(NumberLiteral number) {
        stringBuilder.append(number.value());
    }

    @Override
    public void visit(StringLiteral stringLiteral) {
        stringBuilder
                .append('"')
                .append(stringLiteral.value())
                .append('"');
    }

    @Override
    public void visit(Identifier identifier) {
        stringBuilder.append(identifier.name());
    }

    @Override
    public void visit(UnaryOperation unaryOperation) {
        openScope();
        stringBuilder
                .append(unaryOperation.operator())
                .append(' ');
        unaryOperation.right().accept(this);
        closeScope();
    }

    @Override
    public void visit(BinaryOperation binaryOperation) {
        openScope();
        binaryOperation.left().accept(this);
        stringBuilder
                .append(' ')
                .append(binaryOperation.operator())
                .append(' ');
        binaryOperation.right().accept(this);
        closeScope();
    }

    @Override
    public void visit(FunctionCall functionCall) {
        stringBuilder
                .append(functionCall.name())
                .append('(');
        var comma = comma();
        functionCall
                .parameters()
                .forEach(parameter -> {
                    stringBuilder.append(comma);
                    parameter.accept(this);
                });
        stringBuilder.append(')');
    }

    private void openScope() {
        if (scopeLevel > 0)
            stringBuilder.append('(');
        scopeLevel++;
    }

    private void closeScope() {
        scopeLevel--;
        if (scopeLevel > 0)
            stringBuilder.append(')');
    }
}
