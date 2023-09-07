package com.github.ducoral.formula;

import java.util.List;

import static com.github.ducoral.formula.ExpressionAsStringVisitor.asString;

public interface Expression {

    interface Visitor {

        void visit(NumberLiteral numberLiteral);

        void visit(StringLiteral string);

        void visit(Identifier identifier);

        void visit(UnaryOperation unaryOperation);

        void visit(BinaryOperation binaryOperation);

        void visit(FunctionCall function);
    }

    void accept(Visitor visitor);

    class Empty implements Expression {

        @Override
        public void accept(Visitor visitor) {
        }

        @Override
        public String toString() {
            return "<EMPTY>";
        }
    }

    record NumberLiteral(Object value) implements Expression {

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return asString(this);
        }
    }

    record StringLiteral(String value) implements Expression {

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return asString(this);
        }
    }

    record Identifier(String name) implements Expression {

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return asString(this);
        }
    }

    record UnaryOperation(String operator, Expression right) implements Expression {

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return asString(this);
        }
    }

    record BinaryOperation(Expression left, String operator, Expression right) implements Expression {

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return asString(this);
        }
    }

    record FunctionCall(String name, List<Expression> parameters) implements Expression {

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return asString(this);
        }
    }
}