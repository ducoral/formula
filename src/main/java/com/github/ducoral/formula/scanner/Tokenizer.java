package com.github.ducoral.formula.scanner;

import com.github.ducoral.formula.FormulaException;

import java.util.Set;

import static com.github.ducoral.formula.scanner.TokenType.*;

public class Tokenizer {

    private final CharReader charReader;

    private final Operators operators;

    private final StringBuilder lexeme;

    private CharInfo current;

    private Token token;

    public Tokenizer(CharReader charReader, Operators operators) {
        this.charReader = charReader;
        this.operators = operators;
        lexeme = new StringBuilder();
        current = charReader.next();
        tokenize();
    }

    public Token token() {
        return token;
    }

    public boolean isType(TokenType type) {
        return token.type() == type;
    }

    public boolean isLexeme(String lexeme) {
        return token.lexeme().equals(lexeme);
    }

    public boolean isLexeme(Set<String> lexemes) {
        return lexemes.contains(token.lexeme());
    }

    public boolean isEOF() {
        return token.isEOF();
    }

    public void tokenize() {
        if (current.isEOF()) {
            token = Token.EOF;
            return;
        }

        operators.reset();
        lexeme.setLength(0);

        if (current.isWhitespace())
            tokenizeWhitespace();
        else if (current.isIdentifierStart())
            tokenizeIdentifier();
        else if (current.isDigit())
            tokenizeNumber();
        else if (current.isStringDelimiter())
            tokenizeString();
        else if (operators.isOperatorStart(current.value()))
            tokenizeOperator();
        else if (current.isSymbol()) {
            token = Token.fromCharInfo(SYMBOL, current);
            next();
        }
        else
            throw new FormulaException("Invalid character: %s", current);
    }

    private void tokenizeWhitespace() {
        var position = current.position();
        appendLexemeAndNext();
        while (current.isWhitespace())
            appendLexemeAndNext();
        token = new Token(WHITESPACE, lexeme.toString(), position);
    }

    private void tokenizeIdentifier() {
        var position = current.position();
        appendLexemeAndNext();
        while (current.isIdentifierPart())
            appendLexemeAndNext();
        token = new Token(IDENTIFIER, lexeme.toString(), position);
    }

    private void tokenizeNumber() {
        var position = current.position();
        appendLexemeAndNext();
        while (current.isDigit())
            appendLexemeAndNext();
        if (current.is('.')) {
            appendLexemeAndNext();
            while (current.isDigit())
                appendLexemeAndNext();
        }
        token = new Token(NUMBER, lexeme.toString(), position);
    }

    private void tokenizeString() {
        var position = current.position();
        next();
        while (!current.isEOF() && !current.isStringDelimiter()) {
            if (current.is('\\')) {
                next();
                if (!current.isOneOf('\\', '\"'))
                    throw new FormulaException("Invalid scape \\%s in position: %s", current.value(), position);
            }
            appendLexemeAndNext();
        }
        if (!current.isStringDelimiter())
            throw new FormulaException("String don't closed in position: %s", position);
        token = new Token(STRING, lexeme.toString(), position);
        next();
    }

    private void tokenizeOperator() {
        var position = current.position();
        operators.start(current.value());
        appendLexemeAndNext();
        while (operators.isOperatorPart(current.value())) {
            operators.acceptPart(current.value());
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