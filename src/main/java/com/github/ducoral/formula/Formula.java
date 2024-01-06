package com.github.ducoral.formula;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.ducoral.formula.Utils.fill;
import static com.github.ducoral.formula.Utils.fillSpaces;
import static com.github.ducoral.formula.Utils.rightAlign;

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

    public Result<Expression> parse(String input) {
        assert input != null;

        if (input.isEmpty())
            return Result.ofValue(new Expression.Empty(Position.NULL));

        try {
            var tokenizer = new Tokenizer(new CharReader(input), operatorParser, operatorPrecedence);
            var expression = new ExpressionParser(tokenizer, operatorPrecedence)
                    .parse();
            return Result.ofValue(expression);
        } catch (FormulaException exception) {
            return Result.ofException(input, exception);
        }
    }

    public Result<Value> evaluate(String input) {
        return evaluate(input, Map.of());
    }

    public Result<Value> evaluate(String input, Map<String, Object> scope) {
        assert input != null;

        if (input.isEmpty())
            return Result.ofValue(new Value(null));

        try {
            var parseResult = parse(input);
            if (!parseResult.isOK())
                return Result.ofInvalid(parseResult);
            var value = new Evaluator(this, scope)
                    .evaluate(parseResult.value());
            return Result.ofValue(value);
        } catch (FormulaException exception) {
            return Result.ofException(input, exception);
        }
    }

    public Result<String> explain(String input) {
        assert input != null;

        if (input.isEmpty())
            return Result.ofValue("");

        var parseResult = parse(input);
        if (!parseResult.isOK())
            return Result.ofInvalid(parseResult);

        var expression = parseResult.value();
        var asStr = ExpressionAsStringVisitor.asString(expression);
        var asTextTree = ExpressionAsTextTreeVisitor.asTextTree(expression).split("\\n");
        int linesWidth = String.valueOf(asTextTree.length).length();
        var builder = new StringBuilder();
        builder
                .append(fillSpaces(linesWidth))
                .append("| ")
                .append(asStr)
                .append('\n')
                .append(fill('-', linesWidth))
                .append('+')
                .append(fill('-', asStr.length() + 1));
        for (int line = 0; line < asTextTree.length; line++)
            builder
                    .append('\n')
                    .append(rightAlign(String.valueOf(line + 1), linesWidth))
                    .append("| ")
                    .append(asTextTree[line]);

        return Result.ofValue(builder.toString());
    }

    public static class Builder {

        final Map<String, Function<Parameters, Object>> functions = new HashMap<>();

        final List<Operation> unaryOperations = new ArrayList<>();

        final List<Operation> binaryOperations = new ArrayList<>();

        final AtomicReference<RoundingMode> roundingModeReference = new AtomicReference<>(RoundingMode.HALF_UP);

        public Builder roudingMode(RoundingMode roundingMode) {
            roundingModeReference.set(roundingMode);
            return this;
        }

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
