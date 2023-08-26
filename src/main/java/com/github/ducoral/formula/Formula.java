package com.github.ducoral.formula;

import com.github.ducoral.formula.parser.Expression;
import com.github.ducoral.formula.parser.Parser;
import com.github.ducoral.formula.scanner.CharReader;
import com.github.ducoral.formula.scanner.Operators;

public class Formula {

    public static void main(String[] args) {
        var operatorInfo = Operators
                .builder()
                .operators("&&", "||")
                .operators("=", "!=", ">", ">=", "<", "<=")
                .operators("+", "-", "*", "/")
                .operators("^")
                .build();
        var input = "2+3^2>1+2&&2+1<=4*3";
        var charReader = new CharReader(input);
        var expression = Parser.parse(charReader, operatorInfo);

        var builder = new StringBuilder();
        expression.accept(toString(builder));

        System.out.println(builder);
    }

    static Expression.Visitor toString(StringBuilder builder) {
        return new Expression.Visitor() {
            @Override
            public void visit(Expression.Literal literal) {
                builder.append(literal.value());
            }

            @Override
            public void visit(Expression.Identifier identifier) {
                builder.append(identifier.name());
            }

            @Override
            public void visit(Expression.UnaryOperation unaryOperation) {
                builder
                        .append('[')
                        .append(unaryOperation.operator())
                        .append(' ');
                unaryOperation.right().accept(this);
                builder.append(']');
            }

            @Override
            public void visit(Expression.BinaryOperation binaryOperation) {
                builder.append('[');
                binaryOperation.left().accept(this);
                builder
                        .append(' ')
                        .append(binaryOperation.operator())
                        .append(' ');
                binaryOperation.right().accept(this);
                builder.append(']');
            }

            @Override
            public void visit(Expression.Function function) {
                var comma = new Object() {
                    int count = 0;

                    @Override
                    public String toString() {
                        return count++ == 0 ? "" : ", ";
                    }
                };

                builder
                        .append(function.name())
                        .append('(');
                for (var parameter : function.parameters()) {
                    builder.append(comma);
                    parameter.accept(this);
                }
                builder.append(')');
            }
        };
    }
}
