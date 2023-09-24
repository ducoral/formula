package com.github.ducoral.formula;

import java.util.Map;
import java.util.function.Supplier;

import static com.github.ducoral.formula.Expression.*;

class Evaluator implements Visitor {

    private final Formula formula;

    private final Map<String, Object> scope;

    private Object result;

    Evaluator(Formula formula, Map<String, Object> scope) {
        this.formula = formula;
        this.scope = scope;
    }

    public Value evaluate(Expression expression) {
        expression.accept(this);
        return new Value(result);
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
        var value = new Value(result);
        var operationResolver =
                new OperationResolver(formula.unaryOperations, value.getType(), unaryOperation.operator());
        result = operationResolver.chain(operandsOfUnary(unaryOperation, value));
    }

    @Override
    public void visit(BinaryOperation binaryOperation) {
        binaryOperation.left().accept(this);
        var value = new Value(result);
        var operationResolver =
                new OperationResolver(formula.binaryOperations, value.getType(), binaryOperation.operator());
        var operands = operandsOfBinary(binaryOperation.position(), value, binaryOperation.operator(), () -> {
            binaryOperation.right().accept(this);
            return new Value(result);
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
            return new Value(result);
        });
        result = call.apply(parameters);
    }

    private static Operands operandsOfUnary(UnaryOperation operation, Value value) {
        return new Operands(
                operation.position(),
                () -> null,
                () -> value,
                operands -> String.format("%s %s", operation.operator(), operands.right().getType()));
    }

    private static Operands operandsOfBinary(Position position, Value left, String operator, Supplier<Value> rightSupplier) {
        return new Operands(
                position,
                () -> left,
                rightSupplier,
                operands -> String.format("%s %s %s", operands.left().getType(), operator, operands.right().getType()));
    }
}