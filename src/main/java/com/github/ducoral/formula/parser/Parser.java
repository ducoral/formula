package com.github.ducoral.formula.parser;

import com.github.ducoral.formula.FormulaException;
import com.github.ducoral.formula.scanner.*;

import java.math.BigDecimal;
import java.util.ArrayList;

import static com.github.ducoral.formula.parser.Expression.*;
import static com.github.ducoral.formula.scanner.TokenType.*;

public class Parser {

    private final Tokenizer tokenizer;

    private final Operators operators;

    public static Expression parse(CharReader charReader, Operators operators) {
        return new Parser(charReader, operators)
                .parseExpression();
    }

    private Parser(CharReader charReader, Operators operators) {
        this.tokenizer = new Tokenizer(charReader, operators);
        this.operators = operators;
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
        if (!operators.hasOperatorsWithPrecedence(precedence))
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
        else if (tokenizer.isType(NUMBER)) {
            var value = new BigDecimal(accept(NUMBER).lexeme());
            return new Literal(value);
        } else if (tokenizer.isType(STRING)) {
            var value = accept(STRING).lexeme();
            return new Literal(value);
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
        return new Function(identifier.lexeme(), parameters);
    }

    private Token accept(TokenType type) {
        if (tokenizer.isType(type)) {
            var token = tokenizer.token();
            tokenizer.tokenize();
            return token;
        } else
            throw new FormulaException("Token inválido: %s. Era esperado %s no lugar", type, tokenizer.token().type());
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