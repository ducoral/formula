package com.github.ducoral.formula;

import java.util.function.Function;

public class Parameters {

    private final int count;

    private final Function<Integer, Object> valueFunction;

    Parameters(int count, Function<Integer, Object> valueFunction) {
        this.count = count;
        this.valueFunction = valueFunction;
    }

    public int count() {
        return count;
    }

    public Object getValue(int index) {
        if (index < 0 || index >= count)
            throw new FormulaException("Índice de parâmetro inválido: " + index);
        return valueFunction.apply(index);
    }
}
