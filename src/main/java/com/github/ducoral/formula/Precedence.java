package com.github.ducoral.formula;

public record Precedence(int order) implements Comparable<Precedence> {

    public Precedence before(int offset) {
        return new Precedence(order - offset);
    }

    public Precedence after(int offset) {
        return new Precedence(order + offset);
    }

    @Override
    public int compareTo(Precedence o) {
        return Integer.compare(order, o.order) * -1;
    }
}