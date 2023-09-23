package com.github.ducoral.formula;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

class OperatorParser {

    private final Set<String> operators;

    private Set<String> tokenizing = new HashSet<>();

    private int offset;

    OperatorParser(Set<String> operators) {
        this.operators = operators;
        this.offset = 0;
    }

    void reset() {
        offset = 0;
        tokenizing.clear();
    }

    boolean isOperatorStart(char ch) {
        return operators
                .stream()
                .anyMatch(op -> op.charAt(0) == ch);
    }

    boolean isOperatorPart(char ch) {
        return tokenizing
                .stream()
                .anyMatch(op -> offset < op.length() && op.charAt(offset) == ch);
    }

    boolean isOperator(String lexeme) {
        return operators
                .stream()
                .anyMatch(op -> op.equals(lexeme));
    }

    void start(char ch) {
        tokenizing = operators
                .stream()
                .filter(op -> op.charAt(0) == ch)
                .collect(Collectors.toSet());
        offset = 1;
    }

    void acceptPart(char ch) {
        tokenizing = tokenizing
                .stream()
                .filter(op -> offset < op.length() && op.charAt(offset) == ch)
                .collect(Collectors.toSet());
        offset++;
    }
}
