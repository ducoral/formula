package com.github.ducoral.formula;

import java.util.function.Function;
import java.util.function.Supplier;

public class Operands {

    private final Position position;

    private final Supplier<Value> left;

    private final Supplier<Value> right;

    private final Function<Operands, String> toString;

    Operands(Position position, Supplier<Value> left, Supplier<Value> right, Function<Operands, String> toString) {
        this.position = position;
        this.left = left;
        this.right = right;
        this.toString = toString;
    }

    public Value left() {
        return left.get();
    }

    public Value right() {
        return right.get();
    }

    public Position position() {
        return position;
    }

    @Override
    public String toString() {
        return toString.apply(this);
    }
}
