package com.github.ducoral.formula;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class Formula {

    final Map<String, Function<Parameters, Object>> functions;

    final List<Operation> unaryOperations;

    final List<Operation> binaryOperations;

    final OperatorParser operatorParser;

    final OperatorPrecedence operatorPrecedence;

    public static Builder builder() {
        return new Builder();
    }

    Formula(Builder builder) {
        functions = builder.functions;
        unaryOperations = builder.unaryOperations;
        binaryOperations = builder.binaryOperations;

        var operators = new HashSet<String>();
        unaryOperations.forEach(operation -> operators.add(operation.operator().lexeme()));
        binaryOperations.forEach(operation -> operators.add(operation.operator().lexeme()));
        operatorParser = new OperatorParser(operators);

        operatorPrecedence = new OperatorPrecedence(binaryOperations);
    }

    public Expression parse(String input) {
        var tokenizer = new Tokenizer(new CharReader(input), operatorParser, operatorPrecedence);
        return new ExpressionParser(tokenizer, operatorPrecedence)
                .parse();
    }

    public Object evaluate(String input, Map<String, Object> scope) {
        var expression = parse(input);
        var evaluator = new Evaluator(this, scope);
        return evaluator.evaluate(expression);
    }

    public String explain(String input) {
        return ExpressionAsTextTreeVisitor.explain(parse(input));
    }

    public static class Builder {

        final Map<String, Function<Parameters, Object>> functions = new HashMap<>();

        final List<Operation> unaryOperations = new ArrayList<>();

        final List<Operation> binaryOperations = new ArrayList<>();


        public Builder unaryOperation(Operation operation) {
            unaryOperations.add(operation);
            return this;
        }

        public Builder binaryOperation(Operation operation) {
            binaryOperations.add(operation);
            return this;
        }

        public Builder function(FunctionDefinition function) {
            functions.put(function.name(), function.function());
            return this;
        }

        public Builder configure(Consumer<Builder> consumer) {
            consumer.accept(this);
            return this;
        }

        public Formula build() {
            return new Formula(this);
        }

        Builder() {
        }
    }
}
