package com.github.ducoral.formula.scanner;

import java.util.*;
import java.util.stream.Collectors;

public class Operators {

    public static final String SUM = "+";
    public static final String SUBTRACT = "-";
    public static final String MULTIPLY = "*";
    public static final String DIVIDE = "/";
    public static final String EXPONENTIAL = "^";
    public static final String EQUALS = "=";
    public static final String NOT_EQUALS = "!=";
    public static final String MORE_THAN = ">";
    public static final String MORE_THAN_OR_EQUALS = ">=";
    public static final String LESS_THAN = "<";
    public static final String LESS_THAN_OR_EQUALS = "<=";
    public static final String AND = "&&";
    public static final String OR = "||";

    private final List<Set<String>> operators;

    private Set<String> tokenizing = new HashSet<>();

    private int offset;

    public static class Builder {
        private final List<Set<String>> operators = new ArrayList<>();

        public Builder operators(String... operators) {
            this.operators.add(Set.of(operators));
            return this;
        }

        public Operators build() {
            return new Operators(this);
        }

        private Builder() {
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private Operators(Builder builder) {
        this.operators = builder.operators;
        this.offset = 0;
    }

    public Iterator<Set<String>> operators() {
        return operators.iterator();
    }

    public boolean hasOperatorsWithPrecedence(int precedence) {
        return precedence >= 0 && precedence < operators.size();
    }

    public Set<String> operatorOfPrecedence(int precedence) {
        return hasOperatorsWithPrecedence(precedence)
                ? operators.get(precedence)
                : Set.of();
    }

    public void reset() {
        offset = 0;
        tokenizing.clear();
    }

    public boolean isOperatorStart(char ch) {
        return operators
                .stream()
                .flatMap(Collection::stream)
                .anyMatch(operator -> operator.charAt(0) == ch);
    }

    public boolean isOperatorPart(char ch) {
        return tokenizing
                .stream()
                .anyMatch(op -> offset < op.length() && op.charAt(offset) == ch);
    }

    public void start(char ch) {
        tokenizing = operators
                .stream()
                .flatMap(Collection::stream)
                .filter(op -> op.charAt(0) == ch)
                .collect(Collectors.toSet());
        offset = 1;
    }

    public void acceptPart(char ch) {
        tokenizing = tokenizing
                .stream()
                .filter(op -> offset < op.length() && op.charAt(offset) == ch)
                .collect(Collectors.toSet());
        offset++;
    }
}
