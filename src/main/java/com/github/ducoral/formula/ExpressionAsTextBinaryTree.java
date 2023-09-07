package com.github.ducoral.formula;

import org.w3c.dom.Text;

import static com.github.ducoral.formula.Expression.*;

class ExpressionAsTextBinaryTree implements Visitor {

    private TextBinaryTree tree;

    static TextBinaryTree asTextBinaryTree(Expression expression) {
        var visitor = new ExpressionAsTextBinaryTree();
        expression.accept(visitor);
        return visitor.tree;
    }

    @Override
    public void visit(NumberLiteral numberLiteral) {
        tree = new TextBinaryTree(null, String.valueOf(numberLiteral.value()), null);
    }

    @Override
    public void visit(StringLiteral string) {
        tree = new TextBinaryTree(null, "\"" + string.value() + "\"", null);
    }

    @Override
    public void visit(Identifier identifier) {
        tree = new TextBinaryTree(null, identifier.name(), null);
    }

    @Override
    public void visit(UnaryOperation unaryOperation) {
        unaryOperation.right().accept(this);
        tree = new TextBinaryTree(null, "[" + unaryOperation.operator() + "]", tree);
    }

    @Override
    public void visit(BinaryOperation binaryOperation) {
        binaryOperation.left().accept(this);
        var left = tree;
        binaryOperation.right().accept(this);
        tree = new TextBinaryTree(left, "[" + binaryOperation.operator() + "]", tree);
    }

    @Override
    public void visit(FunctionCall function) {
    }
}
