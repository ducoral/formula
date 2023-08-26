package com.github.ducoral.formula.parser;

import com.github.ducoral.formula.FormulaException;
import com.github.ducoral.formula.scanner.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import static com.github.ducoral.formula.parser.Expression.*;

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
        ignoreWhitspace();

        if (tokenizer.isType(TokenType.OPERATOR))
            return parseUnaryOperation();
        else
            return parseBinaryOperation(operators.operators());
    }

    private Expression parseScope() {
        accept("(");
        var scope = parseExpression();
        accept(")");
        return scope;
    }

    private Expression parseUnaryOperation() {
        var operator = accept(TokenType.OPERATOR);
        return new UnaryOperation(operator.lexeme(), parseExpression());
    }

    private Expression parseBinaryOperation(Iterator<Set<String>> iterator) {
        if (!iterator.hasNext())
            return parseTerm();

        var operators = iterator.next();
        var operation = parseBinaryOperation(iterator);
        while (tokenizer.isLexeme(operators))
            operation = new BinaryOperation(operation, accept(TokenType.OPERATOR).lexeme(), parseExpression());
        return operation;
    }

    private Expression parseTerm() {
        if (tokenizer.isLexeme("("))
            return parseScope();
        else if (tokenizer.isType(TokenType.IDENTIFIER))
            return parseIdentifierOrFunction();
        else if (tokenizer.isType(TokenType.NUMBER)) {
            var value = new BigDecimal(accept(TokenType.NUMBER).lexeme());
            return new Literal(value);
        } else if (tokenizer.isType(TokenType.STRING)) {
            var value = accept(TokenType.STRING).lexeme();
            return new Literal(value);
        } else
            throw new FormulaException("Token não esperado: ", tokenizer.token());
    }

    private Expression parseIdentifierOrFunction() {
        var identifier = accept(TokenType.IDENTIFIER);
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
            ignoreWhitspace();
            return token;
        } else
            throw new FormulaException("Token inválido: %s. Era esperado %s no lugar", type, tokenizer.token().type());
    }

    private void accept(String lexeme) {
        if (tokenizer.isLexeme(lexeme)) {
            tokenizer.tokenize();
            ignoreWhitspace();
        } else
            throw new FormulaException(
                    "Token inválido: %s. Era esperado %s no lugar",
                    tokenizer.token().lexeme(), lexeme);
    }

    private void ignoreWhitspace() {
        if (tokenizer.isType(TokenType.WHITESPACE))
            tokenizer.tokenize();
    }
}
