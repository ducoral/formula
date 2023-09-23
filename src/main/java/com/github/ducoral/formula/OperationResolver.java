package com.github.ducoral.formula;

import java.util.List;
import java.util.stream.Collectors;

import static com.github.ducoral.formula.FormulaExceptionType.OPERATION_NOT_SUPPORTED;

class OperationResolver implements OperationChain {

    private final List<Operation> operations;

    private int chainIndex;

    OperationResolver(List<Operation> operations, Class<?> type, String operator) {
        this.operations = operations
                .stream()
                .filter(operation -> operation.supports(type, operator))
                .sorted(Operation::compareTo)
                .collect(Collectors.toList());
        this.chainIndex = 0;
    }

    @Override
    public Object chain(Operands operands) {
        if (chainIndex == operations.size())
            throw new FormulaException(OPERATION_NOT_SUPPORTED, operands.getPosition(), operands);

        var operation = operations.get(chainIndex++);

        return operation.action().apply(operands, this);
    }
}
