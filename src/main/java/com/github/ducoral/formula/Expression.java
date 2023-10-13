package com.github.ducoral.formula;

import java.util.List;

import static com.github.ducoral.formula.ExpressionAsStringVisitor.asString;

public interface Expression {

    interface Visitor {

        void visit(NumberLiteral numberLiteral);

        void visit(StringLiteral stringLiteral);

        void visit(Identifier identifier);

        void visit(UnaryOperation unaryOperation);

        void visit(BinaryOperation binaryOperation);

        void visit(FunctionCall functionCall);
    }

    Position position();

    void accept(Visitor visitor);

    record Empty(Position position) implements Expression {

        @Override
        public void accept(Visitor visitor) {
        }

        @Override
        public String toString() {
            return "<EMPTY>";
        }
    }

    record NumberLiteral(Position position, Object value) implements Expression {

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return asString(this);
        }
    }

    record StringLiteral(Position position, String value) implements Expression {

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return asString(this);
        }
    }

    record Identifier(Position position, String name) implements Expression {

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return asString(this);
        }
    }

    record UnaryOperation(Position position, String operator, Expression right) implements Expression {

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return asString(this);
        }
    }

    record BinaryOperation(Position position, Expression left, String operator, Expression right) implements Expression {

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return asString(this);
        }
    }

    record FunctionCall(Position position, String name, List<Expression> parameters) implements Expression {

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