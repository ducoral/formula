package com.github.ducoral.formula;

import java.util.function.Function;

public class Parameters {

    private final int count;

    private final Function<Integer, Value> valueFunction;

    Parameters(int count, Function<Integer, Value> valueFunction) {
        this.count = count;
        this.valueFunction = valueFunction;
    }

    public int count() {
        return count;
    }

    public Value get(int index) {
        if (index < 0 || index >= count)
            throw new IndexOutOfBoundsException(index);
        return valueFunction.apply(index);
    }
}
