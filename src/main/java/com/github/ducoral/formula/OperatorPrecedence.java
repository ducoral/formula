package com.github.ducoral.formula;

import java.util.*;

class OperatorPrecedence {

    private final List<Set<String>> operators = new ArrayList<>();

    OperatorPrecedence(List<Operation> operations) {
        var operatorsMap = new HashMap<Integer, Set<String>>();

        for (var operation : operations) {
            var precedence = operation.operator().precedence();
            if (!operatorsMap.containsKey(precedence))
                operatorsMap.put(precedence, new HashSet<>());
            operatorsMap.get(precedence).add(operation.operator().lexeme());
        }

        operatorsMap
                .keySet()
                .stream()
                .sorted((a, b) -> a.compareTo(b) * -1)
                .forEach(precedence -> operators.add(operatorsMap.get(precedence)));
    }

    boolean hasOperatorsWithPrecedence(int precedence) {
        return precedence >= 0 && precedence < operators.size();
    }

    Set<String> operatorOfPrecedence(int precedence) {
        return hasOperatorsWithPrecedence(precedence)
                ? operators.get(precedence)
                : Set.of();
    }
}
