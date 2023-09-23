package com.github.ducoral.formula;

import java.util.Map;
import java.util.function.Supplier;

import static com.github.ducoral.formula.Expression.*;
import static com.github.ducoral.formula.Utils.getTypeOf;

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
    public void visit(NumberLiteral number) {
        result = number.value();
    }

    @Override
    public void visit(StringLiteral string) {
        result = string.value();
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
       result = operationResolver.chain(operandsOfUnary(unaryOperation, result));
    }

    @Override
    public void visit(BinaryOperation binaryOperation) {
        binaryOperation.left().accept(this);
        var operationResolver =
                new OperationResolver(formula.binaryOperations, getTypeOf(result), binaryOperation.operator());
        var operands = operandsOfBinary(binaryOperation.position(), result, binaryOperation.operator(), () -> {
           binaryOperation.right().accept(this);
           return result;
        });
        result = operationResolver.chain(operands);
    }

    @Override
    public void visit(FunctionCall function) {
        if (!formula.functions.containsKey(function.name()))
            throw new FormulaException(FormulaExceptionType.FUNCTION_NOT_DEFINED, function.position(), function);
        var call = formula.functions.get(function.name());
        var parameters = new Parameters(function.parameters().size(), index -> {
            function.parameters().get(index).accept(this);
            return result;
        });
        result = call.apply(parameters);
    }

    private static Operands operandsOfUnary(UnaryOperation operation, Object value) {
        return new Operands(
                operation.position(),
                () -> null,
                () -> value,
                operands -> String.format("%s %s", operation.operator(), operands.getRightType()));
    }

    private static Operands operandsOfBinary(Position position, Object left, String operator, Supplier<Object> rightSupplier) {
        return new Operands(
                position,
                () -> left,
                rightSupplier,
                operands -> String.format("%s %s %s", operands.getLeftType(), operator, operands.getRightType()));
    }
}