package com.github.ducoral.formula;

import java.util.Map;
import java.util.function.Supplier;

import static com.github.ducoral.formula.Expression.*;
import static com.github.ducoral.formula.FormulaUtils.*;

class Evaluator implements Visitor {

    private final Formula formula;

    private final Map<String, Object> scope;

    private Object result;

    Evaluator(Formula formula, Map<String, Object> scope) {
        this.formula = formula;
        this.scope = scope;
    }

    public Object evaluate(Expression expression) {
        expression.accept(this);
        return result;
    }

    @Override
    public void visit(Literal literal) {
        result = literal.value();
    }

    @Override
    public void visit(Identifier identifier) {
        result = scope.get(identifier.name());
    }

    @Override
    public void visit(UnaryOperation unaryOperation) {
       unaryOperation.right().accept(this);
        var operationResolver =
               new OperationResolver(formula.unaryOperations, getTypeOf(result), unaryOperation.operator());
       result = operationResolver.chain(unaryOperand(unaryOperation.operator(), result));
    }

    @Override
    public void visit(BinaryOperation binaryOperation) {
        binaryOperation.left().accept(this);
        var operationResolver =
                new OperationResolver(formula.binaryOperations, getTypeOf(result), binaryOperation.operator());
        var operands = binaryOperands(result, binaryOperation.operator(), () -> {
           binaryOperation.right().accept(this);
           return result;
        });
        result = operationResolver.chain(operands);
    }

    @Override
    public void visit(FunctionCall function) {
        if (!formula.functions.containsKey(function.name()))
            throw new FormulaException("Função não definida: " + function.name());
        var call = formula.functions.get(function.name());
        var parameters = new Parameters(function.parameters().size(), index -> {
            function.parameters().get(index).accept(this);
            return result;
        });
        result = call.apply(parameters);
    }

    private static Operands unaryOperand(String operator, Object value) {
        return new Operands() {
            @Override
            public Object left() {
                return null;
            }

            @Override
            public Object right() {
                return value;
            }

            @Override
            public String toString() {
                return "`" + operator + " " + getTypeNameOf(right()) + "`";
            }
        };
    }

    private static Operands binaryOperands(Object left, String operator, Supplier<Object> rightSupplier) {
        return new Operands() {
            @Override
            public Object left() {
                return left;
            }

            @Override
            public Object right() {
                return rightSupplier.get();
            }

            @Override
            public String toString() {
                return "`" + getTypeNameOf(left()) + " " + operator + " " + getTypeNameOf(right()) + "`";
            }
        };
    }
}