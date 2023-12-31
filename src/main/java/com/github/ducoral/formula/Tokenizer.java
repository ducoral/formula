package com.github.ducoral.formula;

import static com.github.ducoral.formula.FormulaExceptionType.INVALID_CHARACTER;
import static com.github.ducoral.formula.FormulaExceptionType.INVALID_DECIMAL_NUMBER;
import static com.github.ducoral.formula.FormulaExceptionType.INVALID_ESCAPE;
import static com.github.ducoral.formula.FormulaExceptionType.STRING_NOT_CLOSED_CORRECTLY;
import static com.github.ducoral.formula.TokenType.DECIMAL;
import static com.github.ducoral.formula.TokenType.EOF;
import static com.github.ducoral.formula.TokenType.IDENTIFIER;
import static com.github.ducoral.formula.TokenType.INTEGER;
import static com.github.ducoral.formula.TokenType.OPERATOR;
import static com.github.ducoral.formula.TokenType.STRING;
import static com.github.ducoral.formula.TokenType.SYMBOL;

class Tokenizer {

    private final CharReader charReader;

    private final OperatorParser operatorParser;

    private final OperatorPrecedence operatorPrecedence;

    private final StringBuilder lexeme;

    private CharInfo current;

    private Token token;

    private boolean flagDigits;

    Tokenizer(CharReader charReader, OperatorParser operatorParser, OperatorPrecedence operatorPrecedence) {
        this.charReader = charReader;
        this.operatorParser = operatorParser;
        this.operatorPrecedence = operatorPrecedence;
        lexeme = new StringBuilder();
        current = charReader.next();
        tokenize();
    }

    Token token() {
        return token;
    }

    TokenType type() {
        return token.type();
    }

    Position position() {
        return token.position();
    }

    String lexeme() {
        return token.lexeme();
    }

    boolean isType(TokenType... types) {
        for (var type : types)
            if (token.type() == type)
                return true;
        return false;
    }

    boolean isLexeme(String lexeme) {
        return token.lexeme().equals(lexeme);
    }

    boolean isOperatorOfPrecedence(int precedence) {
        return operatorPrecedence
                .getOperatorsOfPrecedence(precedence)
                .contains(token.lexeme());
    }

    boolean isEOF() {
        return token.isEOF();
    }

    void tokenize() {
        ignoreWhitespace();

        if (current.isEOF()) {
            token = new Token(EOF, "EOF", current.position());
            return;
        }

        operatorParser.reset();
        lexeme.setLength(0);

        if (current.isIdentifierStart())
            tokenizeIdentifier();
        else if (current.isDigit() || current.is('.'))
            tokenizeNumber();
        else if (current.isStringDelimiter())
            tokenizeString();
        else if (operatorParser.isOperatorStart(current.value()))
            tokenizeOperator();
        else if (current.isSymbol()) {
            token = new Token(SYMBOL, current.asString(), current.position());
            next();
        } else
            throw new FormulaException(INVALID_CHARACTER, current.position(), current.value());
    }

    private void ignoreWhitespace() {
        while (current.isWhitespace())
            next();
    }

    private void tokenizeIdentifier() {
        var position = current.position();
        appendLexemeAndNext();
        while (current.isIdentifierPart())
            appendLexemeAndNext();
        var tokenType = operatorParser.isOperator(lexeme.toString())
                ? OPERATOR
                : IDENTIFIER;
        token = new Token(tokenType, lexeme.toString(), position);
    }

    private void tokenizeNumber() {
        var position = current.position();
        flagDigits = false;
        appendDigits();
        if (current.isOneOf('e', 'E'))
            tokenizeCientificNotation(position);
        else if (current.is('.'))
            tokenizeDecimal(position);
        else
            token = new Token(INTEGER, lexeme.toString(), position);
    }

    private void tokenizeDecimal(Position position) {
        appendLexemeAndNext();
        appendDigits();
        if (current.isOneOf('e', 'E'))
            tokenizeCientificNotation(position);
        else if (flagDigits)
            token = new Token(DECIMAL, lexeme.toString(), position);
        else
            throw new FormulaException(INVALID_DECIMAL_NUMBER, position, lexeme);
    }

    private void tokenizeCientificNotation(Position position) {
        appendLexemeAndNext();
        if (current.isOneOf('+', '-'))
            appendLexemeAndNext();
        if (!current.isDigit())
            throw new FormulaException(INVALID_DECIMAL_NUMBER, position, lexeme);
        appendDigits();
        token = new Token(DECIMAL, lexeme.toString(), position);
    }

    private void appendDigits() {
        while (current.isDigit()) {
            flagDigits = true;
            appendLexemeAndNext();
        }
    }

    private void tokenizeString() {
        var position = current.position();
        var delimiter = current.value();
        next();
        while (!current.isEOF() && !current.is(delimiter)) {
            if (current.is('\\')) {
                next();
                if (!current.isOneOf('\\', delimiter))
                    throw new FormulaException(INVALID_ESCAPE, position, current.value());
            }
            appendLexemeAndNext();
        }
        if (!current.is(delimiter))
            throw new FormulaException(STRING_NOT_CLOSED_CORRECTLY, position, delimiter + lexeme.toString());
        token = new Token(STRING, lexeme.toString(), position);
        next();
    }

    private void tokenizeOperator() {
        var position = current.position();
        operatorParser.start(current.value());
        appendLexemeAndNext();
        while (operatorParser.isOperatorPart(current.value())) {
            operatorParser.acceptPart(current.value());
            appendLexemeAndNext();
        }
        token = new Token(OPERATOR, lexeme.toString(), position);
    }

    private void appendLexemeAndNext() {
        current.appendTo(lexeme);
        next();
    }

    private void next() {
        current = charReader.next();
    }
}