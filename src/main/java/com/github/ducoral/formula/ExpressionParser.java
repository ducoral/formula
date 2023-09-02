package com.github.ducoral.formula;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

import static com.github.ducoral.formula.Expression.*;
import static com.github.ducoral.formula.TokenType.*;

class ExpressionParser {

    private final Tokenizer tokenizer;

    private final OperatorPrecedence operatorPrecedence;

    ExpressionParser(Tokenizer tokenizer, OperatorPrecedence operatorPrecedence) {
        this.tokenizer = tokenizer;
        this.operatorPrecedence = operatorPrecedence;
    }

    Expression parse() {
        var expression = parseExpression();
        accept(EOF, OPERATOR);
        return expression;
    }

    private Expression parseExpression() {
        return tokenizer.isEOF()
                ? new Empty()
                : parseBinaryOperation(0);
    }

    private Expression parseScope() {
        accept("(");
        var scope = parseExpression();
        accept(")");
        return scope;
    }

    private Expression parseUnaryOperation() {
        var operator = accept(OPERATOR);
        return new UnaryOperation(operator.lexeme(), parseTerm());
    }

    private Expression parseBinaryOperation(int precedence) {
        if (!operatorPrecedence.hasOperatorsWithPrecedence(precedence))
            return parseTerm();

        var operation = parseBinaryOperation(precedence + 1);
        while (tokenizer.isOperatorOfPrecedence(precedence))
            operation = new BinaryOperation(
                    operation,
                    accept(OPERATOR).lexeme(),
                    parseBinaryOperation(precedence + 1));
        return operation;
    }

    private Expression parseTerm() {
        if (tokenizer.isLexeme("("))
            return parseScope();
        else if (tokenizer.isType(IDENTIFIER))
            return parseIdentifierOrFunction();
        else if (tokenizer.isType(INTEGER))
            return new Literal(new BigInteger(accept(INTEGER).lexeme()));
        else if (tokenizer.isType(DECIMAL))
            return new Literal(new BigDecimal(accept(DECIMAL).lexeme()));
        else if (tokenizer.isType(STRING)) {
            return new Literal(accept(STRING).lexeme());
        } else if (tokenizer.isType(OPERATOR)) {
            return parseUnaryOperation();
        } else
            throw new FormulaException("Token não esperado: ", tokenizer.token());
    }

    private Expression parseIdentifierOrFunction() {
        var identifier = accept(IDENTIFIER);
        return tokenizer.isLexeme("(")
                ? parseFunction(identifier)
                : new Identifier(identifier.lexeme());
    }

    private Expression parseFunction(Token identifier) {
        var parameters = new ArrayList<Expression>();
        accept("(");
        while (!tokenizer.isLexeme(")") && !tokenizer.isEOF()) {
            parameters.add(parseExpression());
            while (tokenizer.isLexeme(",")) {
                accept(",");
                parameters.add(parseExpression());
            }
        }
        accept(")");
        return new FunctionCall(identifier.lexeme(), parameters);
    }

    private Token accept(TokenType... types) {
        if (tokenizer.isType(types)) {
            var token = tokenizer.token();
            tokenizer.tokenize();
            return token;
        } else
            throw new FormulaException("Token inválido: %s. Era esperado %s no lugar", tokenizer.token().type(), Arrays.toString(types));
    }

    private void accept(String lexeme) {
        if (tokenizer.isLexeme(lexeme)) {
            tokenizer.tokenize();
        } else
            throw new FormulaException(
                    "Token inválido: %s. Era esperado %s no lugar",
                    tokenizer.token().lexeme(), lexeme);
    }
}