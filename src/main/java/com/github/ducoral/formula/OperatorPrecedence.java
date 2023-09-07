package com.github.ducoral.formula;

import java.util.*;

class OperatorPrecedence {

    private final List<Set<String>> operators = new ArrayList<>();

    OperatorPrecedence(List<Operation> operations) {
        var operatorsMap = new HashMap<Precedence, Set<String>>();

        operations
                .forEach(operation -> operatorsMap
                        .computeIfAbsent(operation.operator().precedence(), precedence -> new HashSet<>())
                        .add(operation.operator().lexeme()));

        operatorsMap
                .keySet()
                .stream()
                .sorted(Precedence::compareTo)
                .forEach(precedence -> operators.add(operatorsMap.get(precedence)));
    }

    boolean hasOperatorsWithPrecedence(int precedence) {
        return precedence >= 0 && precedence < operators.size();
    }

    Set<String> getOperatorsOfPrecedence(int precedence) {
        return hasOperatorsWithPrecedence(precedence)
                ? operators.get(precedence)
                : Set.of();
    }
}
