package com.github.ducoral.formula.parser;

import java.util.List;

public interface Expression {

    interface Visitor {

        void visit(Literal literal);

        void visit(Identifier identifier);

        void visit(UnaryOperation unaryOperation);

        void visit(BinaryOperation binaryOperation);

        void visit(Function function);
    }

    void accept(Visitor visitor);

    class Empty implements Expression {
        @Override
        public void accept(Visitor visitor) {
        }
    }

    record Literal(Object value) implements Expression {
        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }
    }

    record Identifier(String name) implements Expression {
        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }
    }

    record UnaryOperation(String operator, Expression right) implements Expression {
        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }
    }

    record BinaryOperation(Expression left, String operator, Expression right) implements Expression {
        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }
    }

    record Function(String name, List<Expression> parameters) implements Expression {
        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }
    }
}